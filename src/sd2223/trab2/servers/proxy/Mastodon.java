package sd2223.trab2.servers.proxy;

import com.google.gson.reflect.TypeToken;
import sd2223.trab2.api.Message;
import sd2223.trab2.api.java.Feeds;
import sd2223.trab2.api.java.Result;
import sd2223.trab2.servers.java.JavaFeedsPreProxy;
import sd2223.trab2.servers.java.JavaFeedsPullPreconditions;
import sd2223.trab2.servers.proxy.msgs.MastodonAccount;
import sd2223.trab2.servers.proxy.msgs.PostStatusArgs;
import sd2223.trab2.servers.proxy.msgs.PostStatusResult;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import static sd2223.trab2.api.java.Result.error;
import static sd2223.trab2.api.java.Result.ok;
import static sd2223.trab2.api.java.Result.ErrorCode.*;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

import utils.JSON;

import java.util.List;

public class Mastodon implements Feeds {
    static String MASTODON_NOVA_SERVER_URI = "http://10.170.138.52:3000";
    static String MASTODON_SOCIAL_SERVER_URI = "https://mastodon.social";

    static String MASTODON_SERVER_URI = MASTODON_NOVA_SERVER_URI;

    private static final String clientKey = "f-M_gQyRnoHo4iXZWIXtzKy32Wf4PB5hbxDS7niXvLA";
    private static final String clientSecret = "dPA8m1SFkSd4wqWqtfL4FRJt9CDNeQEyLi6AeNWACAI";
    private static final String accessTokenStr = "p03OOdUw1peibeZWpQD8oOoW9-ID9lJv6L_LfT1uaVU";

    static final String STATUSES_PATH= "/api/v1/statuses";

    static final String STATUSES_PATH_ID= "/api/v1/statuses/%s";
    static final String TIMELINES_PATH = "/api/v1/timelines/home";
    static final String ACCOUNT_FOLLOWING_PATH = "/api/v1/accounts/%s/following";
    static final String VERIFY_CREDENTIALS_PATH = "/api/v1/accounts/verify_credentials";
    static final String SEARCH_ACCOUNTS_PATH = "/api/v1/accounts/search";
    static final String ACCOUNT_FOLLOW_PATH = "/api/v1/accounts/%s/follow";
    static final String ACCOUNT_UNFOLLOW_PATH = "/api/v1/accounts/%s/unfollow";

    private static final int HTTP_OK = 200;

    private static final int HTTP_NOT_FOUND = 404;

    protected OAuth20Service service;
    protected OAuth2AccessToken accessToken;

    JavaFeedsPreProxy preconditions;

    private static Mastodon impl;

    protected Mastodon() {
        try {
            service = new ServiceBuilder(clientKey).apiSecret(clientSecret).build(MastodonApi.instance());
            accessToken = new OAuth2AccessToken(accessTokenStr);
        } catch (Exception x) {
            x.printStackTrace();
            System.exit(0);
        }
        preconditions = new JavaFeedsPreProxy();
    }

    synchronized public static Mastodon getInstance() {
        if (impl == null)
            impl = new Mastodon();
        return impl;
    }

    private String getEndpoint(String path, Object ... args ) {
        var fmt = MASTODON_SERVER_URI + path;
        return String.format(fmt, args);
    }

    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        var preconditionsResult = preconditions.postMessage(user, pwd, msg);
        if( ! preconditionsResult.isOK() )
            return preconditionsResult;
        try {
            final OAuthRequest request = new OAuthRequest(Verb.POST, getEndpoint(STATUSES_PATH));

            JSON.toMap( new PostStatusArgs(msg.getText())).forEach( (k, v) -> {
                request.addBodyParameter(k, v.toString());
            });

            service.signRequest(accessToken, request);

            Response response = service.execute(request);
            if (response.getCode() == HTTP_OK) {
                var res = JSON.decode(response.getBody(), PostStatusResult.class);
                return ok(res.getId());
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(INTERNAL_ERROR);
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        var preconditionsResult = preconditions.getMessages(user,time);
        if( ! preconditionsResult.isOK() )
            return preconditionsResult;
        try {
            final OAuthRequest request = new OAuthRequest(Verb.GET, getEndpoint(TIMELINES_PATH));

            service.signRequest(accessToken, request);

            Response response = service.execute(request);

            if (response.getCode() == HTTP_OK) {
                List<PostStatusResult> res = JSON.decode(response.getBody(), new TypeToken<List<PostStatusResult>>() {
                });
                return ok(res.stream().map(PostStatusResult::toMessage).filter(m->m.getCreationTime()>time).toList());
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(Result.ErrorCode.INTERNAL_ERROR);
    }



    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        var preconditionsResult = preconditions.removeFromPersonalFeed(user,mid,pwd);
        if( ! preconditionsResult.isOK() )
            return preconditionsResult;
        try {
            final OAuthRequest request = new OAuthRequest(Verb.DELETE, getEndpoint(STATUSES_PATH_ID,mid));


            service.signRequest(accessToken, request);

            Response response = service.execute(request);
            if (response.getCode() == HTTP_OK) {
                return ok();
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(INTERNAL_ERROR);
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        var preconditionsResult = preconditions.getMessage(user,mid);
        if( ! preconditionsResult.isOK() )
            return preconditionsResult;
        try {
            final OAuthRequest request = new OAuthRequest(Verb.GET, getEndpoint(STATUSES_PATH_ID,mid));

            service.signRequest(accessToken, request);

            Response response = service.execute(request);

            if (response.getCode() == HTTP_OK) {
                PostStatusResult res = JSON.decode(response.getBody(), new TypeToken<PostStatusResult>() {
                });

                return ok(res.toMessage());
            }else
                return response.getCode() == HTTP_NOT_FOUND ? error(NOT_FOUND) : error(INTERNAL_ERROR);
        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(Result.ErrorCode.INTERNAL_ERROR);
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        String id = searchUser(userSub);
        var preconditionsResult = preconditions.subUser(user,searchUser(id),pwd);
        if( ! preconditionsResult.isOK() )
            return preconditionsResult;

        try {
            final OAuthRequest request = new OAuthRequest(Verb.POST, getEndpoint(ACCOUNT_FOLLOW_PATH,id));

            service.signRequest(accessToken, request);

            Response response = service.execute(request);
            if (response.getCode() == HTTP_OK) {
                return ok();
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(INTERNAL_ERROR);
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        String id = searchUser(userSub);
        var preconditionsResult = preconditions.subUser(user,searchUser(id),pwd);
        if(! preconditionsResult.isOK() )
            return preconditionsResult;

        try {
            final OAuthRequest request = new OAuthRequest(Verb.POST, getEndpoint(ACCOUNT_UNFOLLOW_PATH,id));
            service.signRequest(accessToken, request);
            Response response = service.execute(request);
            if (response.getCode() == HTTP_OK) {
                return ok();
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(INTERNAL_ERROR);
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        var preconditionsResult = preconditions.listSubs(user);
        if(! preconditionsResult.isOK() )
            return preconditionsResult;
        String id = searchUser(user);
        try {
            final OAuthRequest request = new OAuthRequest(Verb.GET, getEndpoint(ACCOUNT_FOLLOWING_PATH,id));

            service.signRequest(accessToken, request);

            Response response = service.execute(request);

            if (response.getCode() == HTTP_OK) {
                List<MastodonAccount> res = JSON.decode(response.getBody(), new TypeToken<List<MastodonAccount>>() {
                });

                return ok(res.stream().map(MastodonAccount::username).toList());
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(Result.ErrorCode.INTERNAL_ERROR);
    }

    @Override
    public Result<Void> deleteUserFeed(String user,String secret) {
        //not to be implemented since never called by the users api. (deleting account automatically deletes the feed :) )
        return error(NOT_IMPLEMENTED);
    }

    private String searchUser(String user) {
        try {
            final OAuthRequest request = new OAuthRequest(Verb.GET, getEndpoint(SEARCH_ACCOUNTS_PATH));
            request.addQuerystringParameter("q",user);

            service.signRequest(accessToken, request);

            Response response = service.execute(request);

            if (response.getCode() == HTTP_OK) {
                List<MastodonAccount> res = JSON.decode(response.getBody(), new TypeToken<List<MastodonAccount>>() {
                });
                return res.get(0).id();
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
        return null;
    }
}
