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
package net.sf.pseudosmtp.j2ee.helpers;

import javax.servlet.http.HttpServletRequest

import net.sf.pseudosmtp.j2ee.filters.RestRequestValidator
import net.sf.pseudosmtp.test.PsmtpSpec

public class ApiHelperTests extends PsmtpSpec {
	
	private HttpServletRequest req
	
	def setup() {
		req = Mock()
	}
	
	def 'Client attr is available'() {
		when:
			def scope = ApiHelper.getClientScope(req)
		then:
			1 * req.getAttribute(RestRequestValidator.CLNT_SCOPE_ATTR) >> 'myhost'
			0 * _._
			scope == 'myhost'		
	}
	
	def 'Client attr not available, use client param instead'() {
		when:
			def scope = ApiHelper.getClientScope(req)
		then:
			1 * req.getAttribute(RestRequestValidator.CLNT_SCOPE_ATTR)
			1 * req.getParameter(RestRequestValidator.CLNT_SCOPE_PARAM) >> 'myhost'
			0 * _._
			scope == 'myhost'
	}
	
	def 'Client attr is not available and no client param'() {
		when:
			def scope = ApiHelper.getClientScope(req)
		then:
			1 * req.getAttribute(RestRequestValidator.CLNT_SCOPE_ATTR)
			1 * req.getParameter(RestRequestValidator.CLNT_SCOPE_PARAM)
			0 * _._
			scope == 'localhost' 
	}
}
