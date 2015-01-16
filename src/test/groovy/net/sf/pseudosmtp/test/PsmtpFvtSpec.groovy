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
package net.sf.pseudosmtp.test

import groovy.servlet.GroovyServlet

import javax.servlet.DispatcherType

import net.sf.pseudosmtp.datastore.DataStore
import net.sf.pseudosmtp.j2ee.filters.RestRequestValidator
import net.sf.pseudosmtp.j2ee.listeners.SmtpManager

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.glassfish.jersey.servlet.ServletContainer

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

abstract class PsmtpFvtSpec extends Specification {
	static {
		System.setProperty('psmtp.testing', 'true')
		final Server _SERVER = new Server(10001)
		
		ServletContextHandler sch = new ServletContextHandler(ServletContextHandler.SESSIONS)
		sch.contextPath = '/'
		sch.displayName = 'pseudoSMTP'
		sch.addEventListener(new SmtpManager())
		sch.addFilter(RestRequestValidator, '/api/*', EnumSet.allOf(DispatcherType))
		sch.setInitParameter('psmtp.root', new File(new File(System.getProperty('user.home')), 'psmtp_test').absolutePath)
		sch.setInitParameter('psmtp.smtp.bind-address', '127.0.0.1')
		sch.setInitParameter('psmtp.log-level.app', 'trace')
		sch.setInitParameter('psmtp.log-level.smtp', 'error')
		sch.resourceBase = new File('src/main/webapp/WEB-INF/groovy')
		_SERVER.handler = sch
		
		ServletHolder holder = new ServletHolder(ServletContainer)
		holder.setInitParameter('jersey.config.server.provider.packages', 'net.sf.pseudosmtp.resources')
		sch.addServlet(holder, '/api/*')
		
		holder = new ServletHolder(GroovyServlet)
		sch.addServlet(holder, '*.groovy')
		
		_SERVER.start()
		while(!_SERVER.started)
			sleep 1000
			
		Runtime.runtime.addShutdownHook {
			_SERVER?.stop()
		}
	}
	
	@Shared PsmtpRestClient restClnt
	
	def setupSpec() {
		restClnt = new PsmtpRestClient()
	}
	
	boolean isStepwiseSpec() {
		return this.getClass().getAnnotation(Stepwise) != null
	}
	
	def cleanup() {
		if(!isStepwiseSpec())
			DataStore.instance.shutdown()
	}
}
