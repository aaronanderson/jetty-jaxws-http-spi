/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.eclipse.jetty.jaxws;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/*
 * 
 * The JAXWSHandler supports the dynamic addition and removal of JAX-WS endpoints at runtime. The ServletContextHandler
 * implementation could have been selected but it only supports static pre-start additions of (servlets i.e. enpoints). Removals would require a 
 * Jetty server stop, reorganization of active servlets, re-registration with the context(s), and start.
 *
 */
public class JAXWSHandler extends AbstractHandler {
	private Map<String, Map<String, JettyHttpContext>> ctx = new ConcurrentHashMap<String, Map<String, JettyHttpContext>>();

	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		for (Map.Entry<String, Map<String, JettyHttpContext>> centry : ctx.entrySet()) {
			if (target.startsWith(centry.getKey())) {
				for (Map.Entry<String, JettyHttpContext> pentry : centry.getValue().entrySet()) {
					if (target.substring(centry.getKey().length()).equals(pentry.getKey())) {
						//TODO add logging support and enable trace based on log level
						JettyExchange exchange = new JettyExchange(pentry.getValue(), request, response);
						//JettyTraceExchange exchange = new JettyTraceExchange(pentry.getValue(), request, response);
						pentry.getValue().getHandler().handle(exchange);
						//System.out.println(exchange); 
						return;
					}
				}
			}
		}
	}

	public JettyHttpContext register(String context, String path) throws Exception {
		if (context == null || !context.startsWith("/")) {
			throw new Exception("context must not be null and begin with /");
		}
		if (path == null || !path.startsWith("/")) {
			throw new Exception("path must not be null and begin with /");
		}
		Map<String, JettyHttpContext> entry = ctx.get(context);
		if (entry == null) {
			entry = new ConcurrentHashMap<String, JettyHttpContext>();
			ctx.put(context, entry);
		} else {
			if (entry.containsKey(path)) {
				throw new Exception(String.format("Context %s path %s already registered", context, path));
			}
		}
		JettyHttpContext hctx = new JettyHttpContext(new JettyAttributeInfo(), context, path);
		entry.put(path, hctx);
		return hctx;
	}

	public Set<JettyHttpContext> list(String context) {
		HashSet<JettyHttpContext> set = new HashSet<JettyHttpContext>();
		if (context == null) {
			for (Map<String, JettyHttpContext> entry : ctx.values()) {
				for (JettyHttpContext jctx : entry.values()) {
					set.add(jctx);
				}
			}
			return set;
		}

		Map<String, JettyHttpContext> entry = ctx.get(context);
		if (entry != null) {
			set.addAll(entry.values());
		}
		return set;
	}

	public void unregister(JettyHttpContext context) throws Exception {
		Map<String, JettyHttpContext> entry = ctx.get(context.getContext());
		if (entry != null) {
			entry.remove(context.getPath());
		}

	}

	public class JettyAttributeInfo {

		public Object getAttribute(String name) {
			return getServer().getAttribute(name);
		}

		public Set<String> getAttributeNames() {
			Set<String> attrNames = new HashSet<String>();
			for (Iterator<String> i = (Iterator<String>) getServer().getAttributeNames(); i.hasNext();) {
				attrNames.add(i.next());
			}
			return attrNames;
		}

	}
	
}