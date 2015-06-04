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
