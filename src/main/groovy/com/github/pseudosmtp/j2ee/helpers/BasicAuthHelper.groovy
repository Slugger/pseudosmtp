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

import com.github.pseudosmtp.AppSettings

class BasicAuthHelper {

	static boolean isRequesterAdmin(HttpServletRequest req) {
		def auth = req.getHeader('Authorization')
		if(auth?.startsWith('Basic ')) {
			def enc = auth.substring(6)
			def dec = new String(enc.decodeBase64())
			def creds = dec.split(':', 2)
			return creds[1] == AppSettings.instance.adminPassword && creds[0].toLowerCase() == 'admin'
		} else
			return false
	}
	
	private BasicAuthHelper() {}

}
