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

import groovy.servlet.GroovyServlet

import javax.servlet.DispatcherType

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.glassfish.jersey.servlet.ServletContainer

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import com.github.pseudosmtp.datastore.DataStore
import com.github.pseudosmtp.j2ee.filters.RestRequestValidator
import com.github.pseudosmtp.j2ee.listeners.SmtpManager
import com.github.pseudosmtp.standalone.Launcher

abstract class PsmtpFvtSpec extends Specification {
	static {
		System.setProperty('psmtp.testing', 'true')

		Launcher.startServer(10001, '/', new File('src/main/webapp/WEB-INF/groovy').absolutePath)
					
		Runtime.runtime.addShutdownHook {
			Launcher.stopServer()
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
