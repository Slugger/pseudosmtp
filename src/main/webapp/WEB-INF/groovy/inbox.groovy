import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient

if(params.clnt) {
	def url = new URL(request.requestURL.toString())
	def ctxPath = request.contextPath ?: '/'
	def rest = new RESTClient(new URL("$url.protocol://localhost:${url.port}$ctxPath/api/"))
	def data
	try {
		data = rest.get(path: 'messages', query: params).data
		request.setAttribute('metadata', data)
	} catch(HttpResponseException e) {
		if(e.statusCode == 404) // empty mailbox
			request.setAttbiute('metadata', [])
		else
			throw e
	}
	request.setAttribute('remoteHost', url.host)
	request.getRequestDispatcher('/inbox.html').forward(request, response)
} else
	request.getRequestDispatcher('/login.html').forward(request, response)