import sqlite3
import json
import base64

class Database:
	def __init__(self):
		self.connection = sqlite3.connect("roomGan.db")
		self.connection.row_factory = sqlite3.Row
		self.cursor = self.connection.cursor()

	def selectUser(self, username):
		sql = 'SELECT PASSWORD FROM USERS WHERE USERNAME = ?'
		try:
			self.cursor.execute(sql,(username,))
			users = self.cursor.fetchone()  #Returns a tuple with the user info
			if users:
				return users['PASSWORD']
			return None
		except Exception as ex:
			raise

	def deleteUser(self,username):
		sql = 'DELETE FROM USERS WHERE USERNAME = ?'
		try:
			self.cursor.execute(sql,(username,))
			self.connection.commit()
		except Exception as ex:
			raise

	def recover(self,username):
		sql = 'SELECT EMAIL,PASSWORD FROM USERS WHERE USERNAME = ?'
		try:
			self.cursor.execute(sql,(username,))
			users = self.cursor.fetchone()
			return (users['EMAIL'],users['PASSWORD'])
		except Exception as ex:
			raise

	def getProfileInfo(self, username):
		sql = 'SELECT EMAIL,PASSWORD FROM USERS WHERE USERNAME = ?'
		try:
			self.cursor.execute(sql,(username,))
			user = self.cursor.fetchone()
			return {'mail':user['EMAIL'],'password':user['PASSWORD']}
		except Exception as ex:
			raise


	def registerUser(self,username,password, mail):
		values=(username,password,mail)
		sql = 'INSERT INTO USERS VALUES (?,?,?)'
		try:
			self.cursor.execute(sql, values)
			self.connection.commit()
		except Exception as ex:
			raise
		
	def registerUnverified(self,username,password,mail):
		values=(username,password,mail)
		sql1='INSERT INTO UNVERIFIEDUSERS (USERNAME,PASSWORD,EMAIL) VALUES (?,?,?)'
		sql2='DELETE FROM UNVERIFIEDUSERS WHERE (JULIANDAY()-JULIANDAY(REGISTERTIME)) > 1.0'
		try:
			self.cursor.execute(sql1,values)
			self.cursor.execute(sql2)
			self.connection.commit()
		except Exception as ex:
			raise

	def selectUnverified(self,username):
		print("Seleccionando")
		sql1 = 'SELECT PASSWORD,EMAIL FROM UNVERIFIEDUSERS WHERE USERNAME=? AND (JULIANDAY()-JULIANDAY(REGISTERTIME)) < 1.0'
		sql2 = 'DELETE FROM UNVERIFIEDUSERS WHERE USERNAME=?'
		try:
			self.cursor.execute(sql1,(username,))
			data = self.cursor.fetchone()
			if(data):
				print("He obtenido datos")
				password = data['PASSWORD']
				email = data['EMAIL']
				self.registerUser(username,password,email)
				self.cursor.execute(sql2,(username,))
				self.connection.commit()
				return {'verification':'user verified'}
			print("No hay datos en la tabla")	
			self.cursor.execute(sql2)
			self.connection.commit()	 
			return {'verification':'user not verified'}
		except Exception as ex:
			print(ex)
			raise

	def updatePassword(self,username, password):
		sql = 'UPDATE USERS SET PASSWORD=? WHERE USERNAME=?'
		try:
			self.cursor.execute(sql,(password,username))
			self.connection.commit()
		except Exception as ex:
			raise

	def insertMeasure(self, username, sourceImage, targetImage, generatedImage):
		sql = 'INSERT INTO MEASURES (USERNAME,SOURCE_IMAGE,TARGET_IMAGE,GENERATED_IMAGE) VALUES (?,?,?,?)'
		try:
			self.cursor.execute(sql, (username,sourceImage,targetImage,generatedImage))
			self.connection.commit()
		except Exception as ex:
			raise

	def selectMeasure(self, username):
		sql = 'SELECT SOURCE_IMAGE, TARGET_IMAGE, GENERATED_IMAGE FROM MEASURES WHERE USERNAME=?'
		try:
			self.cursor.execute(sql, (username,))
			measures = self.cursor.fetchone()
			return {'sourceImage':base64.b64encode(measures['SOURCE_IMAGE']).decode('utf-8'), 
			'targetImage':base64.b64encode(measures['TARGET_IMAGE']).decode('utf-8'), 
			'generatedImage':base64.b64encode(measures['GENERATED_IMAGE']).decode('utf-8')}
		except Exception as ex:
			raise

	def getMeasures(self, username, offset):
		sql = 'SELECT ID, SOURCE_IMAGE, TARGET_IMAGE, GENERATED_IMAGE FROM MEASURES WHERE USERNAME=? ORDER BY CREATED_AT DESC LIMIT 5 OFFSET ?'
		try:
			self.cursor.execute(sql, (username, offset))
			measures = self.cursor.fetchall()
			images = []
			for measure in measures:
				images.append({'id':measure['ID'],'sourceImage':base64.b64encode(measure['SOURCE_IMAGE']).decode('utf-8'), 
			'targetImage':base64.b64encode(measure['TARGET_IMAGE']).decode('utf-8'), 
			'generatedImage':base64.b64encode(measure['GENERATED_IMAGE']).decode('utf-8')})
			return images
		except Exception as ex:
			raise ex

	def deleteMeasure(self, id, username):
		sql = 'DELETE FROM MEASURES WHERE ID=? AND USERNAME=?'
		try:
			self.cursor.execute(sql, (id,username))
			self.connection.commit()
		except Exception as ex:
			raise ex
		
	
	def close(self):
		self.connection.close()

if __name__ == '__main__':
	db = Database()
	print(db.selectUser('aleix.clemens'))
	db.close()