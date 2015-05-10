import org.junit.After;

import groovyx.net.http.RESTClient

if(params.clnt) {
	def url = new URL(request.requestURL.toString())
	def ctxPath = request.contextPath ? "/$request.contextPath" : ''
	def rest = new RESTClient(new URL("$url.protocol://$url.host:${url.port}$ctxPath/api/"))
	request.setAttribute('metadata', rest.get(path: 'messages', query: [clnt: params.clnt]).data)
	request.getRequestDispatcher('/inbox.html').forward(request, response)
} else
	response.sendError(400)