package sd2223.trab2.servers.rest;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sd2223.trab2.api.Message;
import sd2223.trab2.api.java.FeedsRep;
import sd2223.trab2.api.java.Result;
import sd2223.trab2.api.rest.FeedsServiceRep;
import sd2223.trab2.servers.java.JavaFeedsRep;

import java.util.List;

import static sd2223.trab2.servers.rest.RestResource.statusCodeFrom;


@Singleton
public class JavaFeedsRepResource implements FeedsServiceRep{


    FeedsRep impl;
    public JavaFeedsRepResource() throws Exception {
        impl = new JavaFeedsRep();
    }


    @Override
    public long postMessage(Long version, String user, String pwd, Message msg) {
        if(version==null) version=-1L;
        impl.setVersion(version);
        return fromJavaResult(impl.postMessage(user,pwd,msg));
    }

    @Override
    public void removeFromPersonalFeed(Long version, String user, long mid, String pwd) {
        if(version==null) version=-1L;
        impl.setVersion(version);
        fromJavaResultE(impl.removeFromPersonalFeed(user, mid,pwd));
    }

    @Override
    public Message getMessage(Long version, String user, long mid) {
        if(version==null) version=-1L;
        impl.setVersion(version);
        return fromJavaResult(impl.getMessage(user,mid));
    }

    @Override
    public List<Message> getMessages(Long version, String user, long time) {
        if(version==null) version=-1L;
        impl.setVersion(version);
        return fromJavaResult(impl.getMessages(user,time));
    }

    @Override
    public void subUser(Long version, String user, String userSub, String pwd) {
        if(version==null) version=-1L;
        impl.setVersion(version);
        fromJavaResultE(impl.subUser(user,userSub,pwd));
    }

    @Override
    public void unsubscribeUser(Long version, String user, String userSub, String pwd) {
        if(version==null) version=-1L;
        impl.setVersion(version);
        fromJavaResultE(impl.unsubscribeUser(user, userSub, pwd));
    }

    @Override
    public List<String> listSubs(Long version, String user) {
        if(version==null) version=-1L;
        impl.setVersion(version);
        return fromJavaResult(impl.listSubs(user));
    }

    @Override
    public void deleteUserFeed(Long version, String user,String secret) {
        if(version==null) version=-1L;
        impl.setVersion(version);
        fromJavaResultE(impl.deleteUserFeed(user,secret));
    }



    @Override
    public List<Message> pull_getTimeFilteredPersonalFeed(Long version, String user, long time,String secret) {
        if(version==null) version=-1L;
        impl.setVersion(version);
        return fromJavaResult( impl.pull_getTimeFilteredPersonalFeed(user, time,secret));
    }

    protected <T> T fromJavaResult(Result<T> result) {
        if (result.isOK()){
            throw new WebApplicationException(Response.status(200).
                    header(FeedsServiceRep.HEADER_VERSION, impl.getVersion()).
                    encoding(MediaType.APPLICATION_JSON).entity(result.value()).build());
        }

        if( result.error() == Result.ErrorCode.REDIRECTED && result.errorValue() != null )
            throw new WebApplicationException(Response.status(200).
                    header(FeedsServiceRep.HEADER_VERSION, impl.getVersion()).
                    encoding(MediaType.APPLICATION_JSON).entity(result.errorValue()).build());
        throw new WebApplicationException(statusCodeFrom(result));
    }
    protected void fromJavaResultE(Result<Void> result) {
        if (result.isOK()){
            throw new WebApplicationException(Response.status(204).
                    header(FeedsServiceRep.HEADER_VERSION, impl.getVersion()).
                    build());
        }
        if( result.error() == Result.ErrorCode.REDIRECTED && result.errorValue() != null )
            throw new WebApplicationException(Response.status(204).
                    header(FeedsServiceRep.HEADER_VERSION, impl.getVersion())
                    .build());
        throw new WebApplicationException(statusCodeFrom(result));
    }
}
