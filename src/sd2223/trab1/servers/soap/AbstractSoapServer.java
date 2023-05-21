package sd2223.trab1.servers.soap;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import jakarta.xml.ws.Endpoint;
import sd2223.trab1.discovery.Discovery;
import sd2223.trab1.servers.Domain;
import sd2223.trab1.servers.java.AbstractServer;
import utils.IP;

import javax.net.ssl.SSLContext;

public class AbstractSoapServer<T> extends AbstractServer {
	private static final String SOAP_CTX = "/soap";

	final T webservice;

	protected AbstractSoapServer( boolean enableSoapDebug, Logger log, String service, int port, T webservice) {
		super(log, service, String.format(SERVER_BASE_URI, IP.hostName(), port, SOAP_CTX));
		this.webservice = webservice;
	}

	protected void start(int port) {
		try{
			var server = HttpsServer.create(new InetSocketAddress(IP.hostAddress(), port), 0);
			server.setExecutor(Executors.newCachedThreadPool());
			server.setHttpsConfigurator(new HttpsConfigurator(SSLContext.getDefault()));
			Endpoint endpoint = Endpoint.create(webservice);
			endpoint.publish(server.createContext(SOAP_CTX));
			server.start();
			Log.info(String.format("%s Soap Server ready @ %s\n", service, serverURI));
			Discovery.getInstance().announce(service,serverURI);

		}catch (Exception e) {
			Log.severe(e.getMessage());
		}
	}
}
