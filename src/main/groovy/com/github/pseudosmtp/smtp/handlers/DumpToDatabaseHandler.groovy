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
	public void from(String from) throws RejectException {
		this.from = new InternetAddress(from, true).address
	}

	@Override
	public void recipient(String recipient) throws RejectException {
		to << new InternetAddress(recipient, true).address
	}

	@Override
	public void data(InputStream data) throws RejectException,
			TooMuchDataException, IOException {
		DataStore.instance.insertMessage(clntAddr, data, from, to)
		data.close()
	}

	@Override
	public void done() {
		log.debug "Done handling message! [clnt: $clntAddr, from: $from]"
	}
}
