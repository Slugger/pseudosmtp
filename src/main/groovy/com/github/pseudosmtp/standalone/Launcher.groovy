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
package com.github.pseudosmtp.standalone

import groovy.servlet.GroovyServlet
import groovy.servlet.TemplateServlet

import javax.servlet.DispatcherType

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.glassfish.jersey.servlet.ServletContainer

import com.github.pseudosmtp.j2ee.filters.RestRequestValidator
import com.github.pseudosmtp.j2ee.listeners.SmtpManager

final class Launcher {
	static private Server SERVER
	static private volatile boolean shutdownStarted = false
	
	static private void stopServer() {
		if(!shutdownStarted && SERVER?.isRunning()) {
			shutdownStarted = true
			println 'Shutting down embedded Jetty...'
			new Timer(true).runAfter(3000) {
				SERVER.stop()
			}
		}
	}
	
	static private void startServer(int port, String contextPath, def baseResource) {
		SERVER = new Server(port)
		
		ServletContextHandler sch = new ServletContextHandler(ServletContextHandler.SESSIONS)
		sch.contextPath = contextPath
		sch.displayName = 'pseudoSMTP'
		sch.addEventListener(new SmtpManager())
		sch.addFilter(RestRequestValidator, '/api/*', EnumSet.allOf(DispatcherType))
		if(!baseResource) {
			def base = Launcher.class.protectionDomain.codeSource.location.toExternalForm()
			if(base.endsWith('.jar'))
				sch.resourceBase = "jar:${base}!/"
			else
				sch.resourceBase = base
		} else
			sch.resourceBase = new File(baseResource).toURI().toString()
		
		SERVER.handler = sch
		
		ServletHolder holder = new ServletHolder(ServletContainer)
		holder.setInitParameter('jersey.config.server.provider.packages', 'com.github.pseudosmtp.resources')
		sch.addServlet(holder, '/api/*')
		
		holder = new ServletHolder(GroovyServlet)
		sch.addServlet(holder, '*.groovy')
		
		holder = new ServletHolder(TemplateServlet)
		sch.addServlet(holder, '*.html')
		
		holder = new ServletHolder(DefaultServlet)
		sch.addServlet(holder, '/')
		
		SERVER.start()
	}
}
