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
package com.github.pseudosmtp.test

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import com.github.pseudosmtp.datastore.DataStore
import com.github.pseudosmtp.standalone.Launcher

abstract class PsmtpFvtSpec extends Specification {
	static private final boolean USE_EXT_HOST = Boolean.parseBoolean(System.getProperty('psmtp.testing.external')) 
	static final String EXT_HOST
	static final String WEB_CONTEXT
	static final int WEB_PORT
	static final int SMTP_PORT
	static final String MY_IP
	
	static {
		System.setProperty('psmtp.testing', 'true')
		if(!USE_EXT_HOST) {
			EXT_HOST = 'localhost'
			WEB_PORT = 10001
			SMTP_PORT = 2525
			MY_IP = 'localhost'

			Launcher.startServer(WEB_PORT, '/', new File('src/main/webapp').absolutePath)

			Runtime.runtime.addShutdownHook {
				Launcher.stopServer()
			}
		} else {
			EXT_HOST = System.getProperty('psmtp.testing.host')
			WEB_PORT = System.getProperty('psmtp.testing.port.web').toInteger()
			SMTP_PORT = System.getProperty('psmtp.testing.port.smtp').toInteger()
			WEB_CONTEXT = System.getProperty('psmtp.testing.web.context')
			def myIp = InetAddress.localHost.toString()
			myIp = myIp.substring(myIp.indexOf('/') + 1)
			MY_IP = myIp
		}
	}
	
	@Shared PsmtpRestClient restClnt
	
	def setupSpec() {
		if(!USE_EXT_HOST)
			restClnt = new PsmtpRestClient()
		else {
			restClnt = new PsmtpRestClient("http://$EXT_HOST:$WEB_PORT/${WEB_CONTEXT}api/", MY_IP)
		}
	}
	
	boolean isStepwiseSpec() {
		return this.getClass().getAnnotation(Stepwise) != null
	}
	
	def cleanup() {
		if(!isStepwiseSpec()) {
			if(!PsmtpFvtSpec.USE_EXT_HOST)
				DataStore.instance.shutdown()
			else
				restClnt.deleteAll()
		}
	}
}
