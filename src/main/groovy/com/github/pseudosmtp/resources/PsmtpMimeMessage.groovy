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
package com.github.pseudosmtp.resources

import groovy.json.JsonBuilder
import groovy.json.JsonException
import groovy.json.JsonSlurper

import javax.servlet.http.HttpServletRequest
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import com.github.pseudosmtp.datastore.DataStore
import com.github.pseudosmtp.datastore.QueryBuilder
import com.github.pseudosmtp.datastore.SqlFilter
import com.github.pseudosmtp.j2ee.filters.RestRequestValidator
import com.github.pseudosmtp.j2ee.helpers.ApiHelper

@Path('/messages')
class PsmtpMimeMessage {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	String listMessages(@Context HttpServletRequest req) {
		List objs = []
		DataStore.instance.findByClient(ApiHelper.getClientScope(req), parseReqQry(req)).each {
			objs << [id: it, url: "/api/messages/$it"]
		}
		if(objs.size() > 0) {
			def json = new JsonBuilder()
			json(objs)
			return json.toString()
		}
		throw new WebApplicationException(Response.Status.NOT_FOUND)
	}
	
	protected QueryBuilder parseReqQry(HttpServletRequest req) {
		def filters = []
		Map params = req.getParameterMap()
		def paramVals = []
		params.each { k, v ->
			if(k != RestRequestValidator.CLNT_SCOPE_PARAM) {
				JsonSlurper json = new JsonSlurper()
				v.each {
					try {
						def f = json.parseText(it)
						if(!(f instanceof Map))
							throw new JsonException('Maps are the only JSON struct accepted!')
						filters << new SqlFilter(column: k, operator: f.o, value: f.v, params: paramVals)
					} catch(JsonException e) {
						filters << new SqlFilter(column: k, operator: '=', value: v, params: paramVals)
					}
				}
			}
		}
		return filters.size() > 0 ? new QueryBuilder(filters, paramVals) : null
	}
	
	@DELETE
	void deleteAll(@Context HttpServletRequest req) {
		DataStore.instance.deleteAllByClient(ApiHelper.getClientScope(req))
	}
	
	@GET
	@Path('/{id: \\d+}')
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	Response getMessage(@Context HttpServletRequest req, @PathParam('id')String id) {
		InputStream msg = DataStore.instance.findById(id.toLong(), ApiHelper.getClientScope(req))
		if(msg != null)
			return Response.ok(msg).build()
		throw new WebApplicationException(Response.Status.NOT_FOUND)
	}
	
	@DELETE
	@Path('/{id: \\d+}')
	void deleteMessage(@Context HttpServletRequest req, @PathParam('id')String id) {
		if(!DataStore.instance.deleteMessage(id.toLong(), ApiHelper.getClientScope(req)))
			throw new WebApplicationException(Response.Status.NOT_FOUND)
	}
}
