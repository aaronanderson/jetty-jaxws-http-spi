package org.eclipse.jetty.jaxws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

@WebService(targetNamespace = EchoService.TNS)
public interface EchoService {
	public static final String TNS = "http://jetty.codehaus.org/jaxws-test";

	@WebMethod
	@WebResult(targetNamespace = EchoService.TNS, name = "EchoMessage")
	public String echo(@WebParam(targetNamespace = EchoService.TNS, name = "Message") String msg);

}