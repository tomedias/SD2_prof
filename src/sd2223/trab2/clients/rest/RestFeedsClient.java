package sd2223.trab2.clients.rest;

import static sd2223.trab2.api.java.Result.error;
import static sd2223.trab2.api.java.Result.ErrorCode.NOT_IMPLEMENTED;

import java.util.List;

import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import sd2223.trab2.api.Message;
import sd2223.trab2.api.java.Feeds;
import sd2223.trab2.api.java.Result;
import sd2223.trab2.api.rest.FeedsService;
import sd2223.trab2.servers.Domain;
import sd2223.trab2.tls.InsecureHostnameVerifier;

import javax.net.ssl.HttpsURLConnection;


public class RestFeedsClient extends RestClient implements Feeds {	
	protected static final String PERSONAL = "personal";

	final protected WebTarget target;
	
	public RestFeedsClient( String serverURI ) {
		super( serverURI );
		target = client.target( serverURI ).path( FeedsService.PATH );
		HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());


	}


	@Override
	public Result<Void> deleteUserFeed(String user,String secret) {
		return super.reTry(() -> clt_deleteUserFeed(user,secret));
	}

	@Override
	public Result<Message> getMessage(String user, long mid) {
		return super.reTry(() -> clt_getMessage(user, mid));
	}

	@Override
	public Result<List<Message>> getMessages(String user, long time) {
		return super.reTry(() -> clt_getMessages(user, time));
	}

	@Override
	public Result<Long> postMessage(String user, String pwd, Message msg) {
		return error( NOT_IMPLEMENTED );
	}

	@Override
	public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
		return error( NOT_IMPLEMENTED );
	}

	@Override
	public Result<Void> subUser(String user, String userSub, String pwd) {
		return error( NOT_IMPLEMENTED );
	}

	@Override
	public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
		return error( NOT_IMPLEMENTED );
	}

	@Override
	public Result<List<String>> listSubs(String user) {
		return error( NOT_IMPLEMENTED );
	}

		
	private Result<Message> clt_getMessage(String user, long mid) {
		Response r = target.path(user).path( Long.toString(mid) )
				.request()

				.get();

		return super.toJavaResult(r, Message.class);
	}

	private Result<List<Message>> clt_getMessages(String user, long time) {
		Response r = target.path( user )
				.queryParam(FeedsService.TIME, time)
				.request()
				.get();

		return super.toJavaResult(r, new GenericType<List<Message>>() {});
	}
	
	public Result<Void> clt_deleteUserFeed(String user,String secret) {
		Response r = target.path(PERSONAL).path( user )
				.queryParam(FeedsService.SECRET, secret)
				.request()
				.delete();

		return super.toJavaResult(r, Void.class);
	}
}
