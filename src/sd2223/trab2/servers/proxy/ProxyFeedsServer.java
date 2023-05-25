package sd2223.trab2.servers.proxy;

import org.glassfish.jersey.server.ResourceConfig;
import sd2223.trab2.servers.Domain;
import sd2223.trab2.servers.rest.AbstractRestServer;
import utils.Args;

import java.util.logging.Logger;

public class ProxyFeedsServer extends AbstractRestServer{

    public static final int PORT = 3000;
     public static final String SERVICE = "proxy";
    private static Logger Log = Logger.getLogger(ProxyFeedsServer.class.getName());
    protected ProxyFeedsServer() {
        super( Log, SERVICE, PORT);
    }

    @Override
    protected void registerResources(ResourceConfig config) {
        config.register( ProxyResource.class );
    }

    public static void main(String[] args) {
        Args.use( args );
        Domain.set( args[0], Long.valueOf(args[1]));
        new ProxyFeedsServer().start(PORT);
    }
}
