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
package com.github.pseudosmtp.j2ee.filters

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.ws.rs.core.Response

import org.apache.log4j.Logger

import com.github.pseudosmtp.j2ee.filters.RestRequestValidator;

class RestRequestValidator implements Filter {
	static volatile Logger LOG = null // We have to lazily init this logger because logging is initialized after filters by container
	static final String CLNT_SCOPE_PARAM = 'clnt'
	static final String CLNT_SCOPE_ATTR = 'PSMTP_CLNT'
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if(!LOG)
			LOG = Logger.getLogger(RestRequestValidator)
		def clnt = request.getParameter(CLNT_SCOPE_PARAM)
		if(!clnt)
			response.sendError(Response.Status.BAD_REQUEST.statusCode, 'All API requests must include a client scope!')
		else {
			try {
				def ip = InetAddress.getByName(clnt).toString()
				ip = ip.substring(ip.indexOf('/') + 1)
				request.setAttribute(CLNT_SCOPE_ATTR, ip)
				if(LOG.isDebugEnabled())
					LOG.debug "Received CLNT: $clnt; set CLNT_SCOPE: $ip"
			} catch(Throwable t) {
				response.sendError(Response.Status.BAD_REQUEST.statusCode, t.message)
				return
			}
			chain.doFilter(request, response)
		}
	}

	@Override
	public void destroy() {}

}
