package sd2223.trab2.clients.soap;

import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.namespace.QName;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import sd2223.trab2.api.Message;
import sd2223.trab2.api.java.FeedsPull;
import sd2223.trab2.api.java.Result;
import sd2223.trab2.api.soap.pull.FeedsService;
import sd2223.trab2.tls.InsecureHostnameVerifier;

public class SoapFeedsPullClient extends SoapFeedsClient implements FeedsPull {

	public SoapFeedsPullClient(String serverURI) {
		super(serverURI);
		HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());

	}

	private FeedsService stub;
	synchronized protected FeedsService stub() {
		if (stub == null) {
			QName QNAME = new QName(FeedsService.NAMESPACE, FeedsService.NAME);
			Service service = Service.create(super.toURL(super.uri + WSDL), QNAME);			
			this.stub = service.getPort(sd2223.trab2.api.soap.pull.FeedsService.class);
			super.setTimeouts( (BindingProvider) stub);
		}
		Thread.dumpStack();
		return stub;
	}
	
	@Override
	public Result<List<Message>> pull_getTimeFilteredPersonalFeed(String user, long time,String secret) {
		return super.reTry( () -> super.toJavaResult( () -> stub().pull_getTimeFilteredPersonalFeed(user, time,secret) ) );
	}
}
