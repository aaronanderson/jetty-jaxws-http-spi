package org.eclipse.jetty.jaxws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;

import org.eclipse.jetty.jaxws.JAXWSHandler;
import org.eclipse.jetty.jaxws.JettyHttpContext;
import org.eclipse.jetty.server.Server;
import org.junit.Test;

public class EndpointTest {

	@Test
	public void invalid() throws Exception {
		ServerSocket portNum = new ServerSocket(0);
		int port = portNum.getLocalPort();
		portNum.close();

		Server server = new Server(port);
		JAXWSHandler handler = new JAXWSHandler();
		server.setHandler(handler);
		server.start();
		try {
			String ctxPath = "/echo";
			String epPath = "/service";
			String endpointAddr = "http://localhost:" + port + ctxPath + epPath;
			URL wsdlURL = new URL(endpointAddr + "?wsdl");
			HttpURLConnection wsdlConnection = (HttpURLConnection) wsdlURL.openConnection();
			wsdlConnection.connect();
			assertEquals(HttpServletResponse.SC_NOT_FOUND, wsdlConnection.getResponseCode());
		} finally {
			server.stop();
		}
	}

	@Test
	public void duplicate() throws Exception {
		String ctxPath = "/echo";
		String epPath = "/service";
		JAXWSHandler handler = new JAXWSHandler();
		handler.register(ctxPath, epPath);
		try {
			handler.register(ctxPath, epPath);
			fail();
		} catch (Exception e) {
		}
	}

	@Test
	public void testEcho() throws Exception {
		ServerSocket portNum = new ServerSocket(0);
		int port = portNum.getLocalPort();
		portNum.close();
		String ctxPath = "/echo";
		String epPath = "/service";
		String endpointAddr = "http://localhost:" + port + ctxPath + epPath;

		Server server = new Server(port);
		JAXWSHandler handler = new JAXWSHandler();
		server.setHandler(handler);
		server.start();

		try {
			JettyHttpContext context = handler.register(ctxPath, epPath);
			Endpoint endpoint = Endpoint.create(new EchoServiceImpl());
			endpoint.publish(context);

			URL wsdlURL = new URL(endpointAddr + "?wsdl");
			HttpURLConnection wsdlConnection = (HttpURLConnection) wsdlURL.openConnection();
			wsdlConnection.connect();
			assertEquals(HttpServletResponse.SC_OK, wsdlConnection.getResponseCode());
			StringBuilder wsdl = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(wsdlConnection.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				wsdl.append(line);
				wsdl.append("\n");
			}
			//System.out.println(wsdl);
			assertTrue(wsdl.toString().contains("name=\"EchoService\""));

			QName serviceName = new QName(EchoService.TNS, EchoServiceImpl.SERVICE_NAME);
			QName portName = new QName(EchoService.TNS, EchoServiceImpl.PORT_NAME);
			// Service service = Service.create(serviceName);
			// service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING,
			// endpointAddr);
			Service service = Service.create(wsdlURL, serviceName);
			EchoService echoService = (EchoService) service.getPort(portName, EchoService.class);
			assertEquals("echo: test message", echoService.echo("test message"));

			endpoint.stop();
			handler.unregister(context);
		} finally {
			server.stop();
		}
	}

}