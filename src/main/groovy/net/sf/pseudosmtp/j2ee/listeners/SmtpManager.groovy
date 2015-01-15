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
package net.sf.pseudosmtp.j2ee.listeners

import javax.mail.Session
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

import net.sf.pseudosmtp.AppSettings
import net.sf.pseudosmtp.datastore.DataStore
import net.sf.pseudosmtp.smtp.PsmtpMessageHandlerFactory

import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.log4j.PatternLayout
import org.apache.log4j.RollingFileAppender
import org.subethamail.smtp.server.SMTPServer

class SmtpManager implements ServletContextListener {

	static private volatile boolean svcsStarted = false
	static private final SMTPServer SMTP_SRV = new SMTPServer(new PsmtpMessageHandlerFactory())
	static final Session SMTP_SESSION = Session.getDefaultInstance(new Properties())
	
	private Logger LOG
	
	private initLogging() {
		def layout = new PatternLayout('%-5p %d{yyyy-MM-dd HH:mm:ss.SSS} [%c{1}] %m%n')
		def appender = new RollingFileAppender(layout, new File(AppSettings.instance.appRoot, 'app.log').absolutePath)
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
		appender = new RollingFileAppender(layout, new File(AppSettings.instance.appRoot, 'smtp.log').absolutePath)
		appender.maxBackupIndex = 2
		appender.maxFileSize = '15MB'
		l.addAppender(appender)
		l.level = AppSettings.instance.smtpLogLevel
	}

	private void startSmtpServer() {
		SMTP_SRV.bindAddress = AppSettings.instance.smtpAddress
		SMTP_SRV.port = AppSettings.instance.smtpPort
		SMTP_SRV.start()
		LOG.info "SMTP server listening on ${SMTP_SRV.bindAddress ?: '*'}:$SMTP_SRV.port"
	}
	
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		if(sce.servletContext.servletContextName == 'pseudoSMTP' && !svcsStarted) {
			svcsStarted = true
			AppSettings.instance.init(sce.servletContext)
			initLogging()
			startSmtpServer()
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if(sce.servletContext.servletContextName == 'pseudoSMTP') {
			svcsStarted = false
			if(SMTP_SRV?.isRunning()) {
				SMTP_SRV.stop()
				LOG.info 'SMTP server stopped!'
			} else
				LOG.error 'SMTP server NOT stopped!'
			DataStore.shutdown()
		}
	}
}
