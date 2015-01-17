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
package com.github.pseudosmtp

import org.apache.log4j.Level

import com.github.pseudosmtp.datastore.DataStore

@Singleton
class AppSettings {
	static private final String APP_ROOT = 'appRoot'
	static private final String BIND_ADDR = 'smtpBindAddr'
	static private final String SMTP_PORT = 'smtpPort'
	static private final String APP_LOG_LVL = 'appLogLevel'
	static private final String SMTP_LOG_LVL = 'smtpLogLevel'
	static private final String ADMIN_PWD = 'adminPwd'
	
	InetAddress smtpBindAddress
	int smtpPort
	Level appLogLevel
	Level smtpLogLevel
	String adminPassword

	String getAdminPassword() {
		return DataStore.instance.getSetting(ADMIN_PWD, 'admin')
	}
	
	void setAdminPassword(String pwd) {
		DataStore.instance.setSetting(ADMIN_PWD, pwd)
	}
	
	InetAddress getSmtpBindAddress() {
		return InetAddress.getByName(DataStore.instance.getSetting(BIND_ADDR, '0.0.0.0'))
	}
	
	String getSmtpBindAddressString() {
		def str = getSmtpBindAddress().toString()
		return str.substring(str.indexOf('/') + 1)
	}
	
	void setSmtpBindAddress(InetAddress addr) {
		def str = addr.toString()
		str = addr.substring(str.indexOf('/') + 1)
		setSmtpBindAddress(str)
	}
	
	void setSmtpBindAddress(String addr) {
		DataStore.instance.setSetting(BIND_ADDR, addr)
	}
	
	int getSmtpPort() {
		return DataStore.instance.getSetting(SMTP_PORT, '2525').toInteger()
	}
	
	void setSmtpPort(int port) {
		setSmtpPort(port.toString())
	}
	
	void setSmtpPort(String port) {
		DataStore.instance.setSetting(SMTP_PORT, port)
	}
	
	Level getAppLogLevel() {
		return Level.toLevel(DataStore.instance.getSetting(APP_LOG_LVL, System.getProperty('psmtp.testing') ? 'trace' : 'info'))
	}
	
	void setAppLogLevel(Level lvl) {
		setAppLogLevel(lvl.toString())
	}
	
	void setAppLogLevel(String lvl) {
		DataStore.instance.setSetting(APP_LOG_LVL, lvl)
	}

	Level getSmtpLogLevel() {
		return Level.toLevel(DataStore.instance.getSetting(SMTP_LOG_LVL, 'error'))
	}
	
	void setSmtpLogLevel(Level lvl) {
		setSmtpLogLevel(lvl.toString())
	}
	
	void setSmtpLogLevel(String lvl) {
		DataStore.instance.setSetting(SMTP_LOG_LVL, lvl)
	}
}
