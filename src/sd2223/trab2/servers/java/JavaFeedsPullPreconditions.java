package sd2223.trab2.servers.java;
import static sd2223.trab2.api.java.Result.error;
import static sd2223.trab2.api.java.Result.ok;
import static sd2223.trab2.api.java.Result.ErrorCode.BAD_REQUEST;
import static sd2223.trab2.api.java.Result.ErrorCode.FORBIDDEN;
import static sd2223.trab2.api.java.Result.ErrorCode.NOT_FOUND;

import java.util.List;

import sd2223.trab2.api.Message;
import sd2223.trab2.api.java.FeedsPull;
import sd2223.trab2.api.java.Result;
import sd2223.trab2.servers.Domain;
import sd2223.trab2.servers.java.JavaFeedsCommon.FeedUser;

public class JavaFeedsPullPreconditions extends JavaFeedsPreconditions implements FeedsPull {
	
	@Override
	public Result<Void> subUser(String user, String userSub, String pwd) {
		
		var ures = getUser( FeedUser.from( user, pwd ) ).error();
		if (ures == NOT_FOUND || ures == FORBIDDEN)
			return error(ures);

		var ures2 = getUser( FeedUser.from( userSub ) ).error();
		if (ures2 == NOT_FOUND || ures2 != FORBIDDEN)
			return error(ures2);

		return ok();
	}

	@Override
	public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
		var ures = getUser( FeedUser.from( user, pwd ) ).error();
		if (ures == NOT_FOUND || ures == FORBIDDEN)
			return error(ures);

		var ures2 = getUser( FeedUser.from( userSub ) ).error();
		if (ures2 == NOT_FOUND || ures2 != FORBIDDEN)
			return error(ures2);

		return ok();
	}

	@Override
	public Result<List<Message>> pull_getTimeFilteredPersonalFeed(String user, long time,String secret) {
		if(!secret.equals(Domain.getSecret())){
			return error(FORBIDDEN);
		}
		if (user == null)
			return error(BAD_REQUEST);

		var ures = getUser(FeedUser.from(user)).error();
		if (ures == NOT_FOUND)
			return error(NOT_FOUND);

		return ok();
	}
}
