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
package net.sf.pseudosmtp

import javax.servlet.ServletContext

import org.apache.log4j.Level

@Singleton
class AppSettings {

	File appRoot
	InetAddress smtpAddress
	int smtpPort
	Level appLogLevel
	Level smtpLogLevel
	
	void init(ServletContext sc) {
		appRoot = new File(sc.getInitParameter('psmtp.root') ?: new File(new File(System.getProperty('user.home')), '.pseudosmtp').absolutePath)
		def addr = sc.getInitParameter('psmtp.smtp.bind-address')
		try {
			smtpAddress =  addr ? InetAddress.getByName(addr) : null
		} catch(Throwable t) {
			smtpAddress = null
		}
		smtpPort = sc.getInitParameter('psmtp.smtp.port')?.toInteger() ?: 2525
		appLogLevel = Level.toLevel(sc.getInitParameter('psmtp.log-level.app')?.toUpperCase(), Level.INFO)
		smtpLogLevel = Level.toLevel(sc.getInitParameter('psmtp.log-level.smtp')?.toUpperCase(), Level.WARN)
	}
	
	File setAppRoot() { throw new UnsupportedOperationException('Read only property!') }
	String setSmtpAddress() { throw new UnsupportedOperationException('Read only property!') }
	int setSmtpPort() { throw new UnsupportedOperationException('Read only property!') }
	Level setAppLogLevel() { throw new UnsupportedOperationException('Read only property!') }
	Level setSmtpLogLevel() { throw new UnsupportedOperationException('Read only property!') }
}
