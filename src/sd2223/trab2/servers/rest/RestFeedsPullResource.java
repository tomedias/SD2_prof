package sd2223.trab2.servers.rest;

import java.util.List;

import jakarta.inject.Singleton;
import sd2223.trab2.api.Message;
import sd2223.trab2.api.java.FeedsPull;
import sd2223.trab2.api.rest.FeedsServicePull;
import sd2223.trab2.servers.java.JavaFeedsPull;

@Singleton
public class RestFeedsPullResource extends RestFeedsResource<FeedsPull> implements FeedsServicePull {

	public RestFeedsPullResource() {
		super(new JavaFeedsPull());
	}

	@Override
	public List<Message> pull_getTimeFilteredPersonalFeed(String user, long time,String secret) {
		return super.fromJavaResult( impl.pull_getTimeFilteredPersonalFeed(user, time,secret));
	}

}
