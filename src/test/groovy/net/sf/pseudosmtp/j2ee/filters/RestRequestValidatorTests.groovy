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
package net.sf.pseudosmtp.j2ee.filters

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import net.sf.pseudosmtp.test.PsmtpSpec

class RestRequestValidatorTests extends PsmtpSpec {
	
	private HttpServletRequest req
	private HttpServletResponse resp
	private FilterChain chain
	
	def setup() {
		req = Mock()
		resp = Mock()
		chain = Mock()
	}
	
	def 'Client scope attr is set properly'() {
		when:
			new RestRequestValidator().doFilter(req, resp, chain)
		then:
			1 * req.getParameter(RestRequestValidator.CLNT_SCOPE_PARAM) >> 'localhost'
			1 * req.setAttribute(RestRequestValidator.CLNT_SCOPE_ATTR, _)
			1 * chain.doFilter(req, resp)
			0 * _._ // No more calls on mocks!
	}
	
	def 'Client scope attr is not set properly'() {
		when:
			new RestRequestValidator().doFilter(req, resp, chain)
		then:
			1 * req.getParameter(RestRequestValidator.CLNT_SCOPE_PARAM)
			1 * resp.sendError(400, _)
			0 * _._ // No more calls on mocks!
	}
}
