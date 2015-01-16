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
import net.sf.pseudosmtp.AppSettings
import net.sf.pseudosmtp.datastore.DataStore

if(isAuth(headers.Authorization)) {
	def method = request.method.toUpperCase()
	if(method == 'GET') {
		def config = AppSettings.instance
		html.html {
			head {
				title('pseudoSMTP Configuration')
			}
			body {
				form(method: 'POST', action: request.requestURL) {
					div {
						span('New admin password: ')
						input(type: 'password', name: 'pwd', value: config.adminPassword)
					}
					div {
						span('App root dir: ')
						input(type: 'text', name: 'app_root', value: config.appRoot.toString())
					}		
					div {
						span('SMTP bind address: ')
						input(type: 'text', name: 'bind_addr', value: config.smtpBindAddressString)
					}
					div {
						span('SMTP port: ')
						input(type: 'text', name: 'port', value: config.smtpPort.toString())
					}
					div {
						span('App log level: ')
						select(name: 'app_lvl') {
							['error', 'warn', 'info', 'debug', 'trace'].each {
								def opts = [value: it]
								if(it == config.appLogLevel.toString().toLowerCase())
									opts['selected'] = 'selected'
								option(opts, it.toUpperCase())
							}
						}
					}
					div {
						span('SMTP log level: ')
						select(name: 'smtp_lvl') {
							['error', 'warn', 'info', 'debug', 'trace'].each {
								def opts = [value: it]
								if(it == config.smtpLogLevel.toString().toLowerCase())
									opts['selected'] = 'selected'
								option(opts, it.toUpperCase())
							}
						}
					}
					div {
						input(type: 'submit', name: 'submit', value: 'update')
					}
					p {
						b('Submitting this form will cause the SMTP server to restart.')
					}
				}
			}
		}
	} else if(method == 'POST') {
		def config = AppSettings.instance
		
		def val = params.pwd
		if(val)
			config.adminPassword = val
		
		val = params.bind_addr
		if(val)
			config.smtpBindAddress = val
			
		val = params.app_root
		if(val)
			config.appRoot = val
		
		val = params.port
		if(val && val ==~ /\d+/)
			config.smtpPort = val
		
		config.appLogLevel = params.app_lvl
		config.smtpLogLevel = params.smtp_lvl
		
		response.sendRedirect(request.requestURL.toString())
	}
} else {
	response.setHeader('WWW-Authenticate', 'Basic realm="pseudoSMTP"')
	response.sendError(401, 'Authentication Required')
}

boolean isAuth(String auth) {
	if(auth && auth.startsWith('Basic ')) {
		def enc = auth.substring(6)
		def dec = new String(enc.decodeBase64())
		def creds = dec.split(':', 2)
		return creds[0].toLowerCase() == 'admin' && creds[1] == DataStore.instance.getSetting('adminPwd', 'admin')
	} else
		return false
}