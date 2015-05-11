import groovyx.net.http.RESTClient

if(params.clnt) {
	def url = new URL(request.requestURL.toString())
	def ctxPath = request.contextPath ? "/$request.contextPath" : ''
	def rest = new RESTClient(new URL("$url.protocol://localhost:${url.port}$ctxPath/api/"))
	request.setAttribute('metadata', rest.get(path: 'messages', query: params).data)
	request.getRequestDispatcher('/inbox.html').forward(request, response)
} else
	request.getRequestDispatcher('/login.html').forward(request, response)