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
package com.github.pseudosmtp.smtp

import java.util.concurrent.ExecutorService

import javax.net.ssl.SSLServerSocketFactory

import org.subethamail.smtp.AuthenticationHandlerFactory
import org.subethamail.smtp.MessageHandlerFactory
import org.subethamail.smtp.server.SMTPServer

class SMTPSServer extends SMTPServer {

	SMTPSServer(MessageHandlerFactory handlerFactory) {
		super(handlerFactory);
		// TODO Auto-generated constructor stub
	}

	SMTPSServer(MessageHandlerFactory handlerFactory,
			AuthenticationHandlerFactory authHandlerFact) {
		super(handlerFactory, authHandlerFact);
		// TODO Auto-generated constructor stub
	}

	SMTPSServer(MessageHandlerFactory msgHandlerFact,
			AuthenticationHandlerFactory authHandlerFact,
			ExecutorService executorService) {
		super(msgHandlerFact, authHandlerFact, executorService);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected ServerSocket createServerSocket() throws IOException {
		SSLServerSocketFactory sf = SSLServerSocketFactory.default
		sf.createServerSocket(port, backlog, bindAddress)
	}
	
	@Override
	boolean getEnableTLS() { false } // Never enable TLS over SMTPS connection
}
