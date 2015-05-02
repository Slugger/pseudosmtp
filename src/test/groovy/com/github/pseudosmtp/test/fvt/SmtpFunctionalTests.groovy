/*
 Copyright 2015 Battams, Derek
 
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
package com.github.pseudosmtp.test.fvt

import javax.mail.SendFailedException
import javax.mail.internet.MimeMessage

import org.apache.commons.net.smtp.SMTPClient
import org.apache.commons.net.smtp.SMTPReply

import com.github.pseudosmtp.AppSettings
import com.github.pseudosmtp.j2ee.listeners.SmtpManager
import com.github.pseudosmtp.test.PsmtpFvtSpec
import com.github.pseudosmtp.test.helpers.EmailHelper

class SmtpFunctionalTests extends PsmtpFvtSpec {
	
	static { // Ensure the SMTP server is alive
		def smtpClnt = new SMTPClient()
		smtpClnt.connect(PsmtpFvtSpec.EXT_HOST, PsmtpFvtSpec.SMTP_PORT)
		if(!SMTPReply.isPositiveCompletion(smtpClnt.getReplyCode()))
			throw new RuntimeException('SMTP server appears not to be running')
		smtpClnt.sendNoOp()
		if(!SMTPReply.isPositiveCompletion(smtpClnt.getReplyCode()))
			throw new RuntimeException("SMTP server rejected NOOP: ${smtpClnt.getReplyString()}")
		smtpClnt.sendCommand('QUIT')
		smtpClnt.disconnect()
	}
			
	def 'Ensure mail is received and stored'() {
		when: 'an email is sent'
			assert restClnt.getAll().size() == 0
			EmailHelper.sendQuickMessage('sender@localhost.com', 'Test Message', 'A test message.', ['receiver@locahost.com'])
			def msgs = restClnt.getAll()
		then: 'the email is stored'
			msgs.size() == 1
			restClnt[msgs[0].id] != null
	}
	
	def 'Ensure multiple emails are properly stored'() {
		when: 'multiple emails are submitted'
			assert restClnt.getAll().size() == 0
			(1..5).each {
				EmailHelper.sendQuickMessage("sender$it@localhost.com", "Test Message #$it", "This is test message #$it.", ["receiver$it@localhost.com"])
			}
			def msgs = restClnt.getAll()
		then: 'all emails are processed, stored and can be read back'
			msgs.size() == 5
			msgs.eachWithIndex { it, i ->
				def msg = new MimeMessage(SmtpManager.SMTP_SESSION, restClnt[it.id])
				assert msg.content.trim() == "This is test message #${i + 1}."
			}
	}
	
	def 'Ensure rejected senders are rejected'() {
		setup:
			def origRegex = AppSettings.instance.senderRegex
			AppSettings.instance.senderRegex = '.*'
			assert restClnt.getAll().size() == 0
		when: 'a sender that should be rejected tries to send an email'
			EmailHelper.sendQuickMessage('sender@localhost', 'Test Message', 'This is a test', ['user@foo.com'])
		then: 'the mail is not accepted'
			thrown(SendFailedException)
			restClnt.getAll().size() == 0
		cleanup:
			AppSettings.instance.senderRegex = origRegex
	}
	
	def 'Ensure rejected recipients are rejected'() {
		setup:
			def origRegex = AppSettings.instance.recipientRegex
			AppSettings.instance.recipientRegex = '.*@invalid'
			assert restClnt.getAll().size() == 0
		when: 'a recipient that should be rejected is addressed'
			EmailHelper.sendQuickMessage('sender@localhost', 'Test Message', 'This is a test', ['user@invalid'])
		then: 'the message is rejected'
			thrown(SendFailedException)
			restClnt.getAll().size() == 0
		when: 'a valid recipient is addressed'
			assert restClnt.getAll().size() == 0
			EmailHelper.sendQuickMessage('sender@localhost', 'Test Message', 'This is a test', ['user@good.com'])
		then: 'the message is accepted'
			restClnt.getAll().size() == 1
		cleanup:
			AppSettings.instance.recipientRegex = origRegex
	}
}
