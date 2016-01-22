/*
 Copyright 2015-2016 Battams, Derek
 
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

import groovy.json.JsonException
import groovy.json.JsonOutput
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
		def scope = ApiHelper.getClientScope(req)
		List objs = DataStore.instance.findByClient(scope, parseReqQry(req))
		if(objs.size() > 0) {
			def baseUrl = req.requestURL.toString()
			if(!baseUrl.endsWith('/'))
				baseUrl += '/'
			objs.each {
				it['__url'] = new URL(new URL(baseUrl), "${it.id}?clnt=$scope")
				it['_attachmentInfo'].each { a ->
					a['__url'] = new URL(new URL(baseUrl), "${it.id}/attachments/${URLEncoder.encode(a['fileName'], 'UTF-8')}?clnt=$scope")
				}
			}
			return JsonOutput.toJson(objs)
		}
		throw new WebApplicationException("No messages with scope=${ApiHelper.getClientScope(req)} exist.", Response.Status.NOT_FOUND)
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
		if(msg)
			return Response.ok(msg).build()
		throw new WebApplicationException("No message with id=$id and scope=${ApiHelper.getClientScope(req)} exists.", Response.Status.NOT_FOUND)
	}
	
	@DELETE
	@Path('/{id: \\d+}')
	void deleteMessage(@Context HttpServletRequest req, @PathParam('id')String id) {
		DataStore.instance.deleteMessage(id.toLong(), ApiHelper.getClientScope(req))
	}
		
	@GET
	@Path('/{id: \\d+}/attachments/{fname}')
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	Response getAttachment(@Context HttpServletRequest req, @PathParam('id')String id, @PathParam('fname')String fileName) {
		InputStream attach = DataStore.instance.getAttachment(id.toLong(), fileName, ApiHelper.getClientScope(req))
		if(attach)
			return Response.ok(attach).build()
		throw new WebApplicationException("No attachment named '$fileName' associated with MimeMessage #$id.", Response.Status.NOT_FOUND)
	}
}
