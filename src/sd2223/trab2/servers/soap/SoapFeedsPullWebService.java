package sd2223.trab2.servers.soap;

import java.util.List;

import jakarta.jws.WebService;
import sd2223.trab2.api.Message;
import sd2223.trab2.api.java.FeedsPull;
import sd2223.trab2.api.soap.FeedsException;
import sd2223.trab2.api.soap.pull.FeedsService;
import sd2223.trab2.servers.java.JavaFeedsPull;

@WebService(serviceName=FeedsService.NAME, targetNamespace=FeedsService.NAMESPACE, endpointInterface=FeedsService.INTERFACE)
public class SoapFeedsPullWebService extends SoapFeedsWebService<FeedsPull> implements FeedsService {

	public SoapFeedsPullWebService() {
		super( new JavaFeedsPull());
	}

	@Override
	public List<Message> pull_getTimeFilteredPersonalFeed(String user, long time) throws FeedsException {
		return super.fromJavaResult( impl.pull_getTimeFilteredPersonalFeed(user, time));
	}

}
