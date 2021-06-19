from flask import Flask, jsonify, request
from flask_httpauth import HTTPBasicAuth
import ssl
import hashlib
from PIL import Image
import base64
from io import BytesIO
import datetime
import os
import csv
import time
import json
import threading
import traceback

import database
import mail
import random
import string

app = Flask(__name__)
auth = HTTPBasicAuth()

PATH = '/mnt/gpid08/users/aleix.clemens/results/'

def encrypt_password(password):
	return hashlib.sha256(password.encode()).hexdigest()

def verify_hash(password, password_hash):
	password2 = hashlib.sha256(password.encode()).hexdigest()
	return hashlib.sha256(password.encode()).hexdigest() == password_hash


@auth.verify_password
def verify_password(username, password):
	password2 = hashlib.sha256(password.encode()).hexdigest()
	db = database.Database()
	db_password = db.selectUser(username)
	if db_password and verify_hash(password, db_password):
		return username
	return False


@app.route('/greetings', methods=['GET'])
@auth.login_required
def greetings():
	return jsonify({'data':'Hello friend '+auth.current_user()})

@app.route('/prueba', methods=['GET'])
def prueba():
	return jsonify({'data':'prueba'})
#status: 0 => OK, 1 => Username does not exist, 2 => Wrong password
@app.route('/login', methods = ['POST'])
def login():
	json = request.json
	username = json['username']
	password = json['password']
	try:
		db = database.Database()
		db_password = db.selectUser(username)
		db.close()

		if not db_password:
			return jsonify({'method':'login','status':1})

		if not verify_hash(password, db_password):
			return jsonify({'method':'login','status':2}) 
		
		return jsonify({'method':'login','status':0})
	except Exception:
		return jsonify({'method':'login', 'status':500, 'error':'Internal error'}), 500


@app.route('/verify',methods = ['GET'])
def verify():
	username = request.args.get('username')
	try:
		db = database.Database()
		output = db.selectUnverified(username)
		print(output)
		db.close()
		return '<p> The user '+ username + ' has been verified successfully</p>'
	except Exception:
		return jsonify({'verification':'user not verified'})

@app.route('/register', methods = ['POST'])
def register():
	json = request.json
	username = json['username']
	password = encrypt_password(json['password'])
	email = json['mail']
	db = database.Database()
	if db.selectUser(username):
		return jsonify({'status': 1, 'error':'Username already in use'}), 400
	db.registerUnverified(username, password, email)
	db.close()
	mailSender = mail.Mail('roomgan2@gmail.com','Roomgan-1',email,password)
	mailSender.writeVerification(username)
	mailSender.send()
	return jsonify({'status':0, 'message':'user registered successfully'})

@app.route('/update/password', methods = ['POST'])
@auth.login_required
def updatePassword():
	json = request.json
	username = json['username']
	password = json['password']
	try:
		db = database.Database()
		db.updatePassword(username,encrypt_password(password))
		db.close()
		return jsonify({'method':'updatePassword','status':0, 'message':'Password successfully updated'})
	except Exception as ex:
		return jsonify({'method':'updatePassword', 'status':500, 'error': 'Internal error'}), 500
	
	
@app.route('/recover', methods = ['GET'])
def recoverPassword():
	username = request.args.get('username')
	print(username)
	password = getRandomPassword()
	print(password)
	try:
		db =  database.Database()
		current_pw = db.selectUser(username)
		if not current_pw:
			return jsonify({'method':'recover', 'status':1, 'error':'Username does not exist'})
		db.updatePassword(username, encrypt_password(password))
		email = db.recover(username)[0]
		print(mail)
		db.close()
		mailSender = mail.Mail('roomgan2@gmail.com','Roomgan-1',email,password)
		mailSender.writeRecovery()
		mailSender.sendMessage()
		return jsonify({'method': 'recover','status':0, 'message':'Password sent to email'})
	except Exception as ex:
		print(ex)
		return  jsonify({'method': 'recover', 'status':500, 'error':'Internal error'})

def getRandomPassword():
	password_characters = string.ascii_letters + string.digits
	password = ''.join(random.choice(password_characters) for i in range(14))
	password = password[0:6]+random.choice(string.punctuation)+password[6:]
	return password

@app.route('/profile',methods = ['GET'])
@auth.login_required
def getProfileInfo():
	username = auth.current_user()
	try:
		db =  database.Database()
		data = db.getProfileInfo(username)
		print(data)
		db.close()
		return jsonify({'method':'profileInfo', 'status':0, 'data':data})
	except Exception as ex:
		print(ex)
		return  jsonify({'method':'profileInfo', 'status':500, 'error':'Internal error'}), 500

@app.route('/delete', methods = ['GET'])
@auth.login_required
def deleteUser():
	username = auth.current_user()
	try:
		db = database.Database()
		db.deleteUser(username)
		db.close()
		return jsonify({'method':'delete', 'status':0, 'message':'Account deleted successfully'})
	except Exception as ex:
		return  jsonify({'method':'delete', 'status':500, 'error':'Internal error'}), 500

@app.route('/generate-room', methods=['POST'])
@auth.login_required
def generateRoom():
	body = request.json

	try:
		db = database.Database()

		byteSourceImage = base64.b64decode(body['sourceImage'])
		byteTargetImage = base64.b64decode(body['targetImage'])
		dir_name = os.path.join(PATH,auth.current_user()+'_'+body['timestamp'])+'/'
		print(dir_name)

		sourceImage = Image.open(BytesIO(byteSourceImage))
		targetImage = Image.open(BytesIO(byteTargetImage))

		if not os.path.exists(dir_name):
			os.makedirs(os.path.join(dir_name,'images'))

		if sourceImage.mode in ('RGBA','P'):
			sourceImage = sourceImage.convert('RGB')

		if targetImage.mode in ('RGBA', 'P'):
			targetImage = targetImage.convert('RGB')
			
		sourceImage.save(os.path.join(dir_name,'images','sourceImage.jpg'))
		targetImage.save(os.path.join(dir_name,'images','targetImage.jpg'))

		os.system('srun -c 3 --mem 10G --gres=gpu:2,gpumem:5G python /home/usuaris/imatge/aleix.clemens/RoomGAN/generateRoom.py {}'.format(dir_name))

		with open(os.path.join(dir_name,'result','generatedImage.jpg'), 'rb') as generatedImage:
			byte_generated_image = generatedImage.read()
			db.insertMeasure(auth.current_user(), byteSourceImage, byteTargetImage, byte_generated_image)
			generatedImage = base64.b64encode(byte_generated_image).decode('utf-8')

		db.close()

		return jsonify({'method':'generateRoom', 'status':0, 'generatedImage':generatedImage})
	except Exception as ex:
		print(ex)
		traceback.print_exc()
		return jsonify({'method':'generateRoom', 'status':500,'error':'Internal error'})

@app.route('/measures', methods=['GET'])
@auth.login_required
def getMeasure():
	username = auth.current_user()
	offset = request.args.get('offset')
	try:
		db = database.Database()
		data = db.getMeasures(username, offset)
		db.close()
		return jsonify({'method':'getMeasures', 'status':0, 'data':data})
	except Exception as ex:
		print(ex)
		return jsonify({'method':'getMeasures', 'status':500, 'error':'Internal error'}), 500

@app.route('/measure/delete', methods=['GET'])
@auth.login_required
def deleteMeasure():
	username = auth.current_user()
	id = request.args.get('id')
	try:
		db = database.Database()
		db.deleteMeasure(id, username)
		db.close()
		return jsonify({'method':'deleteMeasure','status':0,'message':'Measure deleted successfully'})
	except Exception as ex:
		print(ex)
		return jsonify({'method':'deleteMeasure','status':500, 'error':'Internal error'})

@app.route('/measure/insert', methods=['POST'])
@auth.login_required
def insertMeasure():
	username = auth.current_user()
	body = request.json
	sourceImage = base64.b64decode(body['sourceImage'])
	targetImage = base64.b64decode(body['targetImage'])
	generatedImage = base64.b64decode(body['generatedImage'])

	try:
		db = database.Database()
		db.insertMeasure(username,sourceImage,targetImage,generatedImage)
		db.close()
		return jsonify({'method':'insertMeasure','status':0,'message':'Image inserted successfully'})
	except Exception as ex:
		print(ex)
		return jsonify({'method':'insertMeasure','status':500,'message':'Internal error'})

@app.route('/hello', methods=['GET'])
def hello():
	return jsonify({'data':'Hello World'})


if __name__ == '__main__':
	context = ssl.SSLContext()
	context.load_cert_chain('certs2/cert.pem','certs2/key.pem')
	#app.run(debug=True, host='0.0.0.0', port=8999, ssl_context=context)
	app.run(debug=True, host='0.0.0.0', port=35632, ssl_context=context)
	#app.run(debug=True, host='0.0.0.0', port=35632)
