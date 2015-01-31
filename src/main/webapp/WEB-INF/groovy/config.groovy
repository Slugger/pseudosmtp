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
import com.github.pseudosmtp.AppSettings
import com.github.pseudosmtp.j2ee.helpers.BasicAuthHelper
import com.github.pseudosmtp.j2ee.listeners.SmtpManager

if(BasicAuthHelper.isRequesterAdmin(request)) {
	def method = request.method.toUpperCase()
	if(method == 'GET') {
		if(params.SHUTDOWN == 'TRUE') {
			response.contentType = 'text/plain'
			try {
				Class.forName('com.github.pseudosmtp.standalone.Main')
				final def cls = Class.forName('com.github.pseudosmtp.standalone.Launcher')
				out << 'pseudoSMTP is shutting down...'
				def m = cls.getDeclaredMethod('stopServer')
				m.accessible = true
				m.invoke(null)
			} catch(ClassNotFoundException) { // WAR file; ignore it
				out << 'You appear to be running pseudoSMTP in a J2EE container.'
				out << 'Shutdown the app from your container\'s admin instead.'
			}
		} else
			response.sendRedirect('config.html')
	} else if(method == 'POST') {
		def config = AppSettings.instance

		def val = params.pwd
		if(val)
			config.adminPassword = val

		val = params.bind_addr
		if(val)
			config.setSmtpBindAddress(val)

		val = params.port
		if(val && val ==~ /\d+/)
			config.setSmtpPort(val)

		config.setAppLogLevel(params.app_lvl)
		config.setSmtpLogLevel(params.smtp_lvl)
		SmtpManager.restartSmtpServer()

		response.sendRedirect('config.html')
	}
} else {
	response.setHeader('WWW-Authenticate', 'Basic realm="pseudoSMTP"')
	response.sendError(401, 'Authentication Required')
}