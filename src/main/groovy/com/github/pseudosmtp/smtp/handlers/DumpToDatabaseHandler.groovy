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
package com.github.pseudosmtp.smtp.handlers

import groovy.util.logging.Log4j

import javax.mail.internet.InternetAddress

import org.subethamail.smtp.MessageHandler
import org.subethamail.smtp.RejectException
import org.subethamail.smtp.TooMuchDataException

import com.github.pseudosmtp.AppSettings
import com.github.pseudosmtp.datastore.DataStore

@Log4j
class DumpToDatabaseHandler implements MessageHandler {

	private String from
	private String clntAddr
	private List to
	
	DumpToDatabaseHandler(String clntAddr) {
		this.clntAddr = clntAddr
		to = []
	}
	
	@Override
	void from(String from) throws RejectException {
		def regex = AppSettings.instance.senderRegex
		def fromAddr = new InternetAddress(from, true).address
		if(regex && fromAddr.matches(regex))
			throw new RejectException("Sender rejected (matches regex)! [$from]")
		this.from = fromAddr
	}

	@Override
	void recipient(String recipient) throws RejectException {
		def regex = AppSettings.instance.recipientRegex
		def addr = new InternetAddress(recipient, true).address
		if(addr.matches(regex))
			throw new RejectException("Recipient rejected (matched regex)! [$addr]")
		to << addr
	}

	@Override
	void data(InputStream data) throws RejectException, TooMuchDataException, IOException {
		DataStore.instance.insertMessage(clntAddr, data, from, to)
		data.close()
	}

	@Override
	void done() {
		log.debug "Done handling message! [clnt: $clntAddr, from: $from]"
	}
}
