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
package com.github.pseudosmtp.j2ee.listeners

import javax.mail.Session
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.log4j.PatternLayout
import org.apache.log4j.RollingFileAppender
import org.subethamail.smtp.server.SMTPServer

import com.github.pseudosmtp.AppSettings
import com.github.pseudosmtp.datastore.DataStore
import com.github.pseudosmtp.smtp.PsmtpMessageHandlerFactory
import com.github.pseudosmtp.smtp.SMTPSServer

class SmtpManager implements ServletContextListener {
	
	static { // handle attachment filename encoding for JavaMail APIs
		System.setProperty('mail.mime.encodefilename', 'true')
		System.setProperty('mail.mime.decodefilename', 'true')
		System.setProperty('mail.mime.charset', 'UTF-8')
	}

	static private SmtpManager SELF = null
	
	static private volatile boolean svcsStarted = false
	static private SMTPServer smtpSrv
	static private SMTPServer smtpsSrv
	static final Session SMTP_SESSION = Session.getDefaultInstance(new Properties())
	
	static void restartSmtpServer() {
		SELF.initLogging()
		SELF.stopSmtpServer()
		SELF.startSmtpServer()
	}

	private Logger LOG
	
	void initLogging() {
		def layout = new PatternLayout('%-5p %d{yyyy-MM-dd HH:mm:ss.SSS} [%c{1}] %m%n')
		def appender = new RollingFileAppender(layout, new File(DataStore.appRoot, 'app.log').absolutePath)
		appender.maxBackupIndex = 10
		appender.maxFileSize = '5MB'
		def l = Logger.getRootLogger()
		l.removeAllAppenders()
		l.addAppender(appender)
		l.level = AppSettings.instance.appLogLevel
		LOG = Logger.getLogger(SmtpManager)
		
		// Write the subetha logs elsewhere
		l = Logger.getLogger('org.subethamail')
		l.removeAllAppenders()
		l.additive = false
		appender = new RollingFileAppender(layout, new File(DataStore.appRoot, 'smtp.log').absolutePath)
		appender.maxBackupIndex = 2
		appender.maxFileSize = '15MB'
		l.addAppender(appender)
		l.level = AppSettings.instance.smtpLogLevel
		
		boolean isTesting = Boolean.parseBoolean(System.getProperty('psmtp.testing'))
		def isEmbedded = {
			try {
				Class.forName('com.github.pseudosmtp.standalone.Main')
				true
			} catch(ClassNotFoundException) {
				false
			}
		}
		
		if(isTesting || isEmbedded()) {
			// Write Jetty logs elsewhere
			l = Logger.getLogger('org.eclipse.jetty')
			l.removeAllAppenders()
			l.additive = false
			appender = new RollingFileAppender(layout, new File(DataStore.appRoot, 'jetty.log').absolutePath)
			appender.maxBackupIndex = 5
			appender.maxFileSize = '5MB'
			l.addAppender(appender)
			l.level = AppSettings.instance.jettyLogLevel
		}
		
		if(isTesting) {
			// Write HttpClient logs elsewhere
			appender = new RollingFileAppender(layout, new File(DataStore.appRoot, 'httpc.log').absolutePath)
			appender.maxBackupIndex = 3
			appender.maxFileSize = '25MB'

			l = Logger.getLogger('org.apache.http')
			l.removeAllAppenders()
			l.additive = false
			l.addAppender(appender)
			l.level = Level.DEBUG

			l = Logger.getLogger('groovyx.net.http')
			l.removeAllAppenders()
			l.additive = false
			l.addAppender(appender)
			l.level = Level.DEBUG
		}		
	}

	private void stopSmtpServer() {
		if(smtpSrv?.isRunning()) {
			smtpSrv.stop()
			LOG.info 'SMTP server stopped!'
		} else
			LOG.error 'SMTP server NOT stopped!'

		if(smtpsSrv?.isRunning()) {
			smtpsSrv.stop()
			LOG.info 'SMTPS server stopped!'
		} else if(smtpsSrv)
			LOG.error 'SMTPS server NOT stopped!'
	}
	
	private void startSmtpServer() {
		def settings = AppSettings.instance
		smtpSrv = new SMTPServer(new PsmtpMessageHandlerFactory())
		smtpSrv.bindAddress = settings.smtpBindAddress
		smtpSrv.port = settings.smtpPort
		def keystore = settings.keystoreFile
		if(settings.enableStarttls) {
			if(keystore?.exists()) {
				def pwd = settings.keystorePassword
				if(pwd) {
					System.setProperty('javax.net.ssl.keyStore', keystore.absolutePath)
					System.setProperty('javax.net.ssl.keyStorePassword', pwd)
					smtpSrv.setEnableTLS(true)
					LOG.info 'STARTTLS support enabled'
				} else
					LOG.warn 'STARTTLS ignored because keystore password is not set'
			} else
				LOG.warn "STARTTLS ignored because keystore file does not exist [${keystore?.absolutePath}]"
		}
		smtpSrv.start()
		LOG.info "SMTP server listening on ${smtpSrv.bindAddress ?: '*'}:$smtpSrv.port"
		if(settings.enableSmtps) {
			if(keystore?.exists()) {
				def pwd = settings.keystorePassword
				if(pwd) {
					System.setProperty('javax.net.ssl.keyStore', keystore.absolutePath)
					System.setProperty('javax.net.ssl.keyStorePassword', pwd)
					smtpsSrv = new SMTPSServer(new PsmtpMessageHandlerFactory())
					smtpsSrv.bindAddress = settings.smtpBindAddress
					smtpsSrv.port = settings.smtpsPort
					smtpsSrv.start()
					LOG.info "SMTPS server listening on ${smtpsSrv.bindAddress ?: '*'}:$smtpsSrv.port"
				} else
					LOG.warn 'SMTPS ignored because keystore password is not set'
			} else
				LOG.warn "SMTPS ignored because keystore file does not exist [${keystore?.absolutePath}]"
		}
	}
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		SELF = this
		if(sce.servletContext.servletContextName == 'pseudoSMTP' && !svcsStarted) {
			svcsStarted = true
			initLogging()
			startSmtpServer()
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if(sce.servletContext.servletContextName == 'pseudoSMTP') {
			svcsStarted = false
			stopSmtpServer()
			DataStore.shutdown()
		}
	}
}
