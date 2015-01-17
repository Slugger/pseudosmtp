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

import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient

class PsmtpRestClient {

	private RESTClient clnt
	private String defaultScope
	
	PsmtpRestClient(String baseUrl = 'http://127.0.0.1:10001/api/', String defaultScope = 'localhost') {
		clnt = new RESTClient(baseUrl)
		this.defaultScope = defaultScope
	}

	List getAll(Map filters = null, String clntScope = null) {
		try {
			def params = [clnt: clntScope ?: defaultScope]
			filters.each { k, val ->
				if(val instanceof Map) { // it's a full blown json filter
					params[k] = JsonOutput.toJson(val) 
				} else if(val instanceof Collection || val.getClass().isArray()) { // it's a list of values to 'OR'
					def json = new JsonBuilder()
					json {
						o '='
						v val 
					}
					params[k] = json.toString()
				} else // treat it as a single value
					params[k] = val.toString()
			}
			return clnt.get([path: 'messages', query: params]).data
		} catch(HttpResponseException e) {
			if(e.statusCode == 404)
				return []
			throw e
		}		
	}
	
	InputStream getAt(int id, String clntScope = null) {
		try {
			return clnt.get([path: "messages/$id", query: [clnt: clntScope ?: defaultScope]]).data
		} catch(HttpResponseException e) {
			if(e.statusCode == 404)
				return null
			throw e
		}
	}
	
	void deleteAll(String clntScope = null) {
		clnt.delete([path: 'messages/', query: [clnt: clntScope ?: defaultScope]])
	}
	
	boolean delete(int id, String clntScope = null) {
		clnt.delete([path: "messages/$id", query: [clnt: clntScope ?: defaultScope]])
	}
}
