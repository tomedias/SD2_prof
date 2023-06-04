package sd2223.trab2.servers.rest;

import org.glassfish.jersey.server.ResourceConfig;
import sd2223.trab2.api.java.Feeds;
import sd2223.trab2.servers.Domain;
import utils.Args;
import utils.IP;

import java.util.logging.Logger;

public class RestFeedsRepServer extends AbstractRestServer{

    public static final int PORT = 4568;

    private static Logger Log = Logger.getLogger(RestFeedsRepServer.class.getName());

    protected RestFeedsRepServer() {
        super( Log, Feeds.SERVICENAME, PORT);
    }

    @Override
    protected void registerResources(ResourceConfig config) {
        config.register( JavaFeedsRepResource.class );
    }

    public static void main(String[] args) {
        Args.use( args );
        Domain.set( args[0], Long.valueOf(args[1]));
        Domain.setSecret(args[2]);
        IP.setUri(String.format(SERVER_BASE_URI, IP.hostName(), PORT, "/rest"));
        new RestFeedsRepServer().start(PORT);
    }
}
