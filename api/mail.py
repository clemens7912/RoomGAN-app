from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
import smtplib
import threading


class Mail():

	def __init__(self, sender, password, receiver, userPassword):
		self.msg = MIMEMultipart()
		self.msg['From'] = sender
		self.msg['To'] = receiver
		#self.msg['Subject'] = "Password recovery"
		self.password = password
		self.userPassword = userPassword
		#self.message = self.writeMessage(userPassword)
		#threading.Thread(target=self.sendMessage).start()


	def writeRecovery(self):
		self.msg['Subject'] = "Password recovery"
		str = "Your password is: "+self.userPassword
		self.msg.attach(MIMEText(str,'plain'))


	def writeVerification(self,username):
		self.msg['Subject'] = "Account Verification"
		str = "Your account has been created. To activate it, press the following link: "
		self.msg.attach(MIMEText(str,'plain'))
		str1 = "<a href=https://172.26.105.25:35632/verify?username="+username+">Verify your account</a>"
		self.msg.attach(MIMEText(str1,'html'))
		return str

	def send(self):
		threading.Thread(target=self.sendMessage).start()

	def sendMessage(self):
		server = smtplib.SMTP('smtp.gmail.com:587')
		server.starttls()

		server.login(self.msg['From'], self.password)

		server.sendmail(self.msg['From'], self.msg['To'], self.msg.as_string())

		server.quit()


if __name__ == "__main__":
	mail = Mail("paeaccenture@gmail.com", "PAEAccenture-1","clemens7912@gmail.com","Probando")