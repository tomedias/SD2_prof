package sd2223.trab2.servers.java;

import sd2223.trab2.api.java.Result;

import static sd2223.trab2.api.java.Result.ErrorCode.FORBIDDEN;
import static sd2223.trab2.api.java.Result.ErrorCode.NOT_FOUND;
import static sd2223.trab2.api.java.Result.error;
import static sd2223.trab2.api.java.Result.ok;

public class JavaFeedsPreProxy extends JavaFeedsPreconditions{
    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {

        var ures = getUser( JavaFeedsCommon.FeedUser.from( user, pwd ) ).error();
        if (ures == NOT_FOUND || ures == FORBIDDEN)
            return error(ures);
        if(userSub==null){
            return error(NOT_FOUND);
        }
        return ok();
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        var ures = getUser( JavaFeedsCommon.FeedUser.from( user, pwd ) ).error();
        if (ures == NOT_FOUND || ures == FORBIDDEN)
            return error(ures);
        if(userSub==null){
            return error(NOT_FOUND);
        }
        return ok();
    }
}
