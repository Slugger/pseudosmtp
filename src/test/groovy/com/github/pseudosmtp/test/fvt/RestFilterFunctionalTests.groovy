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

import javax.mail.internet.MimeMessage

import com.github.pseudosmtp.j2ee.listeners.SmtpManager
import com.github.pseudosmtp.test.PsmtpFvtSpec
import com.github.pseudosmtp.test.helpers.EmailHelper

class RestFilterFunctionalTests extends PsmtpFvtSpec {

	def setup() {
		(1..5).each {
			EmailHelper.sendQuickMessage("sender$it@localhost.com", "Test Message #$it", "This is test message #$it.", ["receiver$it@localhost.com"])
		}
	}
	
	def 'Basic filtering works'() {
		when: 'A basic email filter is applied'
			def msgs = restClnt.getAll([sender: 'sender1@localhost.com'])
			def msg = new MimeMessage(SmtpManager.SMTP_SESSION, restClnt[msgs[0].id])
		then: 'Only the expected email is returned'
			restClnt.getAll().size() == 5
			msgs.size() == 1
			msg.content.trim() == 'This is test message #1.'
			msg.sender.toString() == 'sender1@localhost.com'
	}
	
	def 'Multiple filters work'() {
		when: 'A basic email filter is applied'
			def msgs = restClnt.getAll([sender: 'sender1@localhost.com', to: 'receiver1@localhost.com'])
			def msg = new MimeMessage(SmtpManager.SMTP_SESSION, restClnt[msgs[0].id])
		then: 'Only the expected email is returned'
			restClnt.getAll().size() == 5
			msgs.size() == 1
			msg.content.trim() == 'This is test message #1.'
			msg.sender.toString() == 'sender1@localhost.com'
	}
	
	def 'Non matching filter returns no results'() {
		when: 'A non-matching filter is applied'
			def msgs = restClnt.getAll([sender: 'sender1@localhost.com', to: 'receiver2@localhost.com'])
		then: 'no messages are returned'
			restClnt.getAll().size() == 5
			msgs.size() == 0
	}
	
	def 'Search email headers for a match'() {
		when: 'A matching filter is applied'
			def msgs = restClnt.getAll(['MIME-Version': '1.0', sender: [o: '!=', v: 'sender3@localhost.com']])
		then: 'matches are found'
			restClnt.getAll().size() == 5
			msgs.size() == 4
	}
	
	def 'Search email headers with no matches'() {
		when: 'A non-matching filter is applied'
			def msgs = restClnt.getAll(['MIME-Version': '1.1'])
		then: 'no messages are returned'
			restClnt.getAll().size() == 5
			msgs.size() == 0
	}
}
