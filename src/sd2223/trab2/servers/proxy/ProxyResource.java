package sd2223.trab2.servers.proxy;

import jakarta.inject.Singleton;
import sd2223.trab2.api.java.Feeds;
import sd2223.trab2.servers.rest.RestFeedsResource;

@Singleton
public class ProxyResource extends RestFeedsResource<Feeds>{


    public ProxyResource() {
        super(Mastodon.getInstance());
    }
}
