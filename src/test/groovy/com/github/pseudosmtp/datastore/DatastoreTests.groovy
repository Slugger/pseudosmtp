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
package com.github.pseudosmtp.datastore

import javax.mail.Message.RecipientType
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

import com.github.pseudosmtp.j2ee.listeners.SmtpManager
import com.github.pseudosmtp.test.PsmtpSpec

class DatastoreTests extends PsmtpSpec {
	
	def setup() {
		MimeMessage msg = new MimeMessage(SmtpManager.SMTP_SESSION)
		msg.subject = 'Test Message 1'
		msg.sender = new InternetAddress('sender@test.com')
		msg.addRecipients(RecipientType.TO, 'to@test.com')
		msg.from = msg.sender
		msg.sentDate = new Date()
		msg.text = 'Here is a test message.'
		msg.saveChanges()
		def os = new ByteArrayOutputStream()
		msg.writeTo(os)
		DataStore.instance.insertMessage('localhost', new ByteArrayInputStream(os.toByteArray()), 'sender@test.com', ['to@test.com'])
	}
	
	def 'Ensure db is created'() {
		when: 'the datastore is accessed'
			def ds = DataStore.instance
		then: 'a backing database is created'
			ds.sql.firstRow('SELECT value FROM settings WHERE name = \'dbVersion\'').value.toInteger() >= 0
	}
	
	def 'Ensure messages can be found by client id'() {
		when: 'expected messages are searched for'
			def msgs = DataStore.instance.findByClient('localhost')
		then: 'the messages are found'
			msgs.size() == 1
			msgs[0] == 1
	}
	
	def 'Ensure invalid clients return no results'() {
		when: 'an invalid client is specified'
			def msgs = DataStore.instance.findByClient('foo')
		then: 'no messages are found'
			msgs.size() == 0
	}
	
	def 'Ensure messages are deleted by client'() {
		when: 'messages by client are deleted'
			assert DataStore.instance.findByClient('localhost').size() == 1
			DataStore.instance.deleteAllByClient('localhost')
		then: 'no messages are left'
			DataStore.instance.findByClient('localhost').size() == 0
	}
	
	def 'Ensure messages can be pulled out by id'() {
		when: 'a specific message is requested'
			def msg = DataStore.instance.findById(1, 'localhost')
			assert msg != null
			msg = new MimeMessage(SmtpManager.SMTP_SESSION, msg)
		then: 'a valid MIME message binary stream is returned'
			msg.from[0].toString() == 'sender@test.com'
	}
	
	def 'Ensure messages that don\'t exist return no matches'() {
		when: 'a specific message id does not exist'
			def msg = DataStore.instance.findById(10000, 'localhost')
		then: 'null is returned'
			msg == null
	}
	
	def 'Settings can be written and read'() {
		when:
			def ds = DataStore.instance
			assert ds.getSetting('foo') == null
			ds.setSetting('foo', 'bar')
		then:
			'bar' == ds.getSetting('foo')
	}	
}