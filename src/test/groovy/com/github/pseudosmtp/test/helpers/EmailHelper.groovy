/*
 Copyright 2015-2016 Battams, Derek
 
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
 
		http://www.apache.org/licenses/LICENSE-2.0
 
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package com.github.pseudosmtp.test.helpers

import javax.mail.Session
import javax.mail.Transport
import javax.mail.Message.RecipientType
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

import com.github.pseudosmtp.test.PsmtpFvtSpec

class EmailHelper {
	static private final Session SMTP_SESSION
	
	static {
		def smtpProps = new Properties()
		smtpProps.setProperty('mail.smtp.host', PsmtpFvtSpec.EXT_HOST)
		smtpProps.setProperty('mail.smtp.port', PsmtpFvtSpec.SMTP_PORT.toString())
		SMTP_SESSION = Session.getInstance(smtpProps)
	}
	
	static MimeMessage createQuickMessage(String from, String subject, String msg, List to = null, List cc = null, List bcc = null, Session session = SMTP_SESSION) {
		def mime = new MimeMessage(session)
		mime.subject = subject
		mime.sender = new InternetAddress(from)
		if(to)
			mime.addRecipients(RecipientType.TO, to.join(','))
		if(cc)
			mime.addRecipients(RecipientType.CC, cc.join(','))
		if(bcc)
			mime.addRecipients(RecipientType.BCC, bcc.join(','))
		if(!to && !cc && !bcc)
			throw new RuntimeException('Must include at least one recipient in an email!')
		mime.sentDate = new Date()
		mime.text = msg
		return mime
	}
	
	static MimeMessage createQuickMessageWithAttachment(String from, String subject, String msg, String fileName, List to = null, List cc = null, List bcc = null, Session session = SMTP_SESSION) {
		def mimeMsg = createQuickMessage(from, subject, msg, to, cc, bcc, session)
		
		def mimeMultipart = new MimeMultipart()
		
		def txtPart = new MimeBodyPart()
		txtPart.setContent(msg, 'text/plain')
		mimeMultipart.addBodyPart(txtPart)
		
		def attachPart = new MimeBodyPart()
		final def file = new File(File.createTempDir(), fileName)
		Runtime.runtime.addShutdownHook { file.parentFile.deleteDir() }
		file << 'abc'
		attachPart.attachFile(file)
		mimeMultipart.addBodyPart(attachPart)
		
		mimeMsg.content = mimeMultipart
		mimeMsg
	}
	
	static void sendQuickMessage(String from, String subject, String msg, List to = null, List cc = null, List bcc = null, Session session = SMTP_SESSION) {
		Transport.send(createQuickMessage(from, subject, msg, to, cc, bcc, session))
	}
	
	static void sendQuickMessageWithAttachment(String from, String subject, String msg, String fileName, List to = null, List cc = null, List bcc = null, Session session = SMTP_SESSION) {
		Transport.send(createQuickMessageWithAttachment(from, subject, msg, fileName, to, cc, bcc, session))
	}
	
	private EmailHelper() {}

}
