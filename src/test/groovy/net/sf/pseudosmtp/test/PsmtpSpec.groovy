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

import net.sf.pseudosmtp.AppSettings
import net.sf.pseudosmtp.datastore.DataStore
import net.sf.pseudosmtp.j2ee.listeners.SmtpManager
import net.sf.pseudosmtp.test.mocks.MockServletContext
import spock.lang.Specification
import spock.lang.Stepwise

abstract class PsmtpSpec extends Specification {
	static { 
		System.setProperty('psmtp.testing', 'true')
		System.setProperty('psmtp.log-level.app', 'trace')
		System.setProperty('psmtp.log-level.smtp', 'warn')
		AppSettings.instance.init(MockServletContext.instance)
		new SmtpManager().initLogging()
	}
	
	boolean isStepwiseSpec() {
		return this.getClass().getAnnotation(Stepwise) != null
	}

	def cleanup() {
		if(!isStepwiseSpec())
			DataStore.shutdown()
	}
}
