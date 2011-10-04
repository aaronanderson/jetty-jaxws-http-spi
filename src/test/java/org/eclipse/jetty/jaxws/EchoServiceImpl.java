package org.eclipse.jetty.jaxws;

import javax.jws.WebService;

@WebService(targetNamespace = EchoService.TNS, serviceName = EchoServiceImpl.SERVICE_NAME, portName = EchoServiceImpl.PORT_NAME, endpointInterface = "org.eclipse.jetty.jaxws.EchoService")
public class EchoServiceImpl implements EchoService {

	public static final String SERVICE_NAME = "EchoService";
	public static final String PORT_NAME = "EchoServicePort";

	public String echo(String msg) {
		return "echo: " + msg;
	}

}