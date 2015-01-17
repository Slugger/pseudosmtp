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
package com.github.pseudosmtp.j2ee.helpers

import javax.servlet.http.HttpServletRequest

import com.github.pseudosmtp.j2ee.filters.RestRequestValidator;

class ApiHelper {

	static String getClientScope(HttpServletRequest req) {
		def clnt = req?.getAttribute(RestRequestValidator.CLNT_SCOPE_ATTR)?.toString()
		if(!clnt) // Test env only
			clnt = req?.getParameter(RestRequestValidator.CLNT_SCOPE_PARAM) ?: 'localhost'
		return clnt
	}
	
	private ApiHelper() {}
}
