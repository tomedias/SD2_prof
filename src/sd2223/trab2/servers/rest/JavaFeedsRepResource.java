package sd2223.trab2.servers.rest;

import jakarta.inject.Singleton;
import sd2223.trab2.api.Message;
import sd2223.trab2.api.java.FeedsPull;
import sd2223.trab2.api.rest.FeedsServicePull;
import sd2223.trab2.servers.java.JavaFeedsRep;

import java.util.List;
@Singleton
public class JavaFeedsRepResource  extends RestFeedsResource<FeedsPull> implements FeedsServicePull {
    public JavaFeedsRepResource() {
        super(new JavaFeedsRep());
    }

    @Override
    public List<Message> pull_getTimeFilteredPersonalFeed(String user, long time) {
        return super.fromJavaResult( impl.pull_getTimeFilteredPersonalFeed(user, time));
    }
}
