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
package com.github.pseudosmtp.j2ee.listeners

import javax.mail.Session
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

import org.apache.log4j.Logger
import org.apache.log4j.PatternLayout
import org.apache.log4j.RollingFileAppender
import org.subethamail.smtp.server.SMTPServer

import com.github.pseudosmtp.AppSettings
import com.github.pseudosmtp.datastore.DataStore
import com.github.pseudosmtp.smtp.PsmtpMessageHandlerFactory

class SmtpManager implements ServletContextListener {

	static private SmtpManager SELF = null
	
	static private volatile boolean svcsStarted = false
	static private SMTPServer smtpSrv
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
	}

	private void stopSmtpServer() {
		if(smtpSrv?.isRunning()) {
			smtpSrv.stop()
			LOG.info 'SMTP server stopped!'
		} else
			LOG.error 'SMTP server NOT stopped!'
	}
	
	private void startSmtpServer() {
		smtpSrv = new SMTPServer(new PsmtpMessageHandlerFactory())
		smtpSrv.bindAddress = AppSettings.instance.smtpBindAddress
		smtpSrv.port = AppSettings.instance.smtpPort
		smtpSrv.start()
		LOG.info "SMTP server listening on ${smtpSrv.bindAddress ?: '*'}:$smtpSrv.port"
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
