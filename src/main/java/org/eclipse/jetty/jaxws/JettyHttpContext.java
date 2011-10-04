package org.eclipse.jetty.jaxws;

import java.util.Set;

import javax.xml.ws.spi.http.HttpContext;
import javax.xml.ws.spi.http.HttpHandler;

import org.eclipse.jetty.jaxws.JAXWSHandler.JettyAttributeInfo;

public class JettyHttpContext extends HttpContext {
	
	final JettyAttributeInfo info;
	final String context;
	final String path;

    JettyHttpContext(JettyAttributeInfo info, String context, String path) {
		this.info=info;
		this.context=context;
		this.path = path;
	}

	@Override
	public Object getAttribute(String name) {
		return info.getAttribute(name);
	}

	@Override
	public Set<String> getAttributeNames() {
		return info.getAttributeNames();
	}

	@Override
	public String getPath() {
		return path;
	}
	
	public String getContext() {
		return context;
	}
	
	public HttpHandler getHandler(){
		return this.handler;
	}

}