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

import javax.servlet.DispatcherType

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.glassfish.jersey.servlet.ServletContainer

import com.github.pseudosmtp.j2ee.filters.RestRequestValidator
import com.github.pseudosmtp.j2ee.listeners.SmtpManager

final class Launcher {
	static private Server SERVER
	
	static main(args) {
		def opts = parseCmdLine(args)
		if(!opts.h) {
			startServer(opts.i ? opts.i.toInteger() : 8080, opts.c ?: '/', opts.r ?: null)
			Runtime.runtime.addShutdownHook {
				stopServer()
			}
		}
	}
	
	static private def parseCmdLine(def args) {
		def cli = new CliBuilder(usage: 'psmtp.jar [options]')
		cli.p(args: 1, argName: 'port', 'Port number the embedded Jetty server will listen on. [8080]')
		cli.c(args: 1, argName: 'context', 'Context path to deploy psmtp at. [<root> i.e. /]')
		cli.r(args: 1, argName: 'resource_base', 'Base directory used for resource discovery (usually only changed for testing purposes).')
		cli.h('Display this help and exit.')
		def opts = cli.parse(args)
		if(opts.h)
			cli.usage()
		return opts
	}
	
	static private void stopServer() {
		if(SERVER?.isRunning()) {
			println 'Shutting down embedded Jetty...'
			SERVER.stop()
		}
	}
	
	static private void startServer(int port, String contextPath, String resourceBase) {
		SERVER = new Server(port)
		
		ServletContextHandler sch = new ServletContextHandler(ServletContextHandler.SESSIONS)
		sch.contextPath = contextPath
		sch.displayName = 'pseudoSMTP'
		sch.addEventListener(new SmtpManager())
		sch.addFilter(RestRequestValidator, '/api/*', EnumSet.allOf(DispatcherType))
		if(resourceBase)
			sch.resourceBase = new File(resourceBase)
		SERVER.handler = sch
		
		ServletHolder holder = new ServletHolder(ServletContainer)
		holder.setInitParameter('jersey.config.server.provider.packages', 'com.github.pseudosmtp.resources')
		sch.addServlet(holder, '/api/*')
		
		holder = new ServletHolder(GroovyServlet)
		sch.addServlet(holder, '*.groovy')
		
		SERVER.start()
	}
}
