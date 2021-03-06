/*
 Copyright 2016 Battams, Derek
 
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

class RestAttachmentsFunctionalTests extends PsmtpFvtSpec {
	
	static private String ATTACH_NAME = '\u053B.txt'

	def setup() {
		EmailHelper.sendQuickMessageWithAttachment("asender@localhost.com", "Test Message With Attachment", "This is test message with attachment.", ATTACH_NAME, ["areceiver@localhost.com"])
	}
	
	def 'Attachments are properly listed'() {
		when: 'the email is searched for'
			def msgs = restClnt.getAll([has_attachment: 'true'])
		then: 'the email is found'
			msgs.size() == 1
			msgs[0].id > 0
			msgs[0]._attachments == 1
			msgs[0]._attachmentInfo.size() == 1
			msgs[0]._attachmentInfo[0].fileName == ATTACH_NAME
	}
	
	def 'Attachments can be fetched'() {
		when: 'the email is searched for fetched'
			def msg = restClnt.getAll([has_attachment: 'true'])[0]
		and: 'the attachment is downloaded'
			InputStream is = new URL(msg['_attachmentInfo'][0].__url).content
		then: 'the attachment is received as expected'
			is.getText() == 'abc'
	}
	
	def 'Invalid attachments return 404'() {
		when: 'an invalid attachment is requested'
			new URL(restClnt.getAll([has_attachment: 'true'])[0]['_attachmentInfo'][0].__url.replace('.txt', '.text')).content
		then: 'a 404 is returned'
			thrown(FileNotFoundException)
	}
}
