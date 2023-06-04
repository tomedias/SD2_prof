package sd2223.trab2.servers.java;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.LoggerFactory;
import sd2223.trab2.api.Message;
import sd2223.trab2.api.java.FeedsRep;
import sd2223.trab2.api.java.Result;
import sd2223.trab2.kafka.KafkaPublisher;
import sd2223.trab2.kafka.KafkaSubscriber;
import sd2223.trab2.kafka.SyncPoint;
import sd2223.trab2.servers.Domain;
import utils.IP;
import utils.JSON;

import java.util.*;
import static sd2223.trab2.api.java.Result.ErrorCode.*;
import static sd2223.trab2.api.java.Result.error;
import static sd2223.trab2.api.java.Result.ok;

public class JavaFeedsRep extends JavaFeedsPull implements FeedsRep {


    static final String TOPIC = Domain.get();

    static final String KAFKA_BROKERS = "kafka:9092"; // When running in docker container...
    private static final String FROM_BEGINNING = "earliest";;
    public static final String POST_MESSAGE = "postMessage";
    public static final String REMOVE_FROM_PERSONAL_FEED = "removeFromPersonalFeed";
    public static final String SUB_USER = "subUser";
    public static final String UNSUBSCRIBE_USER = "unsubscribeUser";
    public static final String DELETE_USER_FEED = "deleteUserFeed";

    final KafkaPublisher publisher;

    final SyncPoint<String> sync;

    private long version=-1L;

    private final String replicaID;


    public JavaFeedsRep()  {
        super();
        org.slf4j.Logger kafkaLogger = LoggerFactory.getLogger("org.apache.kafka");
        ((ch.qos.logback.classic.Logger)kafkaLogger).setLevel(ch.qos.logback.classic.Level.OFF);
        //startZookeeper();
        this.replicaID = IP.hostName().split("-")[0].replaceFirst("feeds","");
        publisher = KafkaPublisher.createPublisher(KAFKA_BROKERS);
        this.sync = new SyncPoint<>();
        startKafka();
        setVersion(send("Starting replica " + IP.hostName()));
        sync();
    }

    public void sync(){
        sync.waitForVersion(version,Integer.MAX_VALUE);
    }
    public void startKafka(){
        KafkaSubscriber subscriber = KafkaSubscriber.createSubscriber(JavaFeedsRep.KAFKA_BROKERS, List.of(JavaFeedsRep.TOPIC), JavaFeedsRep.FROM_BEGINNING);
        subscriber.start(false, (r) -> {
            String[] command = r.value().split(" ");
            switch (command[0]) {
                case POST_MESSAGE -> __PostMessage(command[1], JSON.decode(command[3], Message.class));
                case REMOVE_FROM_PERSONAL_FEED -> __RemoveFromPersonalFeed(command[1], Long.parseLong(command[2]));
                case SUB_USER -> __SubUser(command[1], command[2]);
                case UNSUBSCRIBE_USER -> __UnsubscribeUser(command[1], command[2]);
                case DELETE_USER_FEED -> __DeleteUserFeed(command[1]);
                default -> System.out.println(r.value());
            }
            sync.setVersion(r.offset());
        });
    }
    public void setVersion(long version) {
       this.version = version;
    }

    public long getVersion() {
        return version;
    }

    private long send(String msg) {
        long offset = publisher.publish(TOPIC,replicaID, msg);
        if (offset < 0)
            System.err.println("Failed to publish message");
        return offset;

    }

    @Override
    protected List<Message> getTimeFilteredPersonalFeed(String user, long time) {
        return super.getTimeFilteredPersonalFeed(user, time);
    }

    @Override
    public Result<List<String>> listSubs(String user) {
            return super.listSubs(user);
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        sync();
        return super.getMessage(user, mid);
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        sync();
        return super.getMessages(user, time);
    }

    @Override
    public Result<List<Message>> pull_getTimeFilteredPersonalFeed(String user, long time,String secret) {
        sync();
        return super.pull_getTimeFilteredPersonalFeed(user, time,secret);
    }

    @Override
    protected void deleteFromUserFeed(String user, Set<Long> mids) {
        sync();
        super.deleteFromUserFeed(user, mids);
    }

    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        var preconditionsResult = preconditions.postMessage(user, pwd,msg);
        if( ! preconditionsResult.isOK() )
            return preconditionsResult;
        long mid = serial.incrementAndGet();
        msg.setId(mid);
        msg.setCreationTime(System.currentTimeMillis());
        setVersion(send("postMessage " + user + " " + pwd + " " + JSON.encode(msg)));
        return ok(mid);

    }

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        var preconditionsResult = preconditions.removeFromPersonalFeed(user, mid, pwd);
        if( ! preconditionsResult.isOK() )
            return preconditionsResult;
        var ufi = feeds.get(user);
        if( ufi == null )
            return error(NOT_FOUND);
        synchronized (ufi.user()) {
            if (!ufi.messages().contains(mid))
                return error(NOT_FOUND);
        }
        setVersion(send("removeFromPersonalFeed " + user + " " + mid + " " + pwd));


        return ok();
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        var preconditionsResult = preconditions.subUser(user, userSub, pwd);
        if( ! preconditionsResult.isOK() )
            return preconditionsResult;
        setVersion(send("subUser " + user + " " + userSub + " " + pwd));
        return ok();
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        var preconditionsResult = preconditions.unsubscribeUser(user, userSub, pwd);
        if( ! preconditionsResult.isOK() )
            return preconditionsResult;
        setVersion(send("unsubscribeUser " + user + " " + userSub + " " + pwd));
        return ok();
    }

    @Override
    public Result<Void> deleteUserFeed(String user,String secret) {
        var preconditionsResult = preconditions.deleteUserFeed(user,secret);
        if( ! preconditionsResult.isOK() )
            return preconditionsResult;
        if(feeds.get(user) == null)
            return error(NOT_FOUND);
        setVersion(send("deleteUserFeed " + user));
        return ok();
    }

    public void __PostMessage(String user, Message msg) {

        System.out.println("Posting message: " + msg);
        FeedInfo ufi = feeds.computeIfAbsent(user, FeedInfo::new );
        synchronized (ufi.user()) {
            ufi.messages().add(msg.getId());
            messages.putIfAbsent(msg.getId(), msg);
        }
    }

    public void __RemoveFromPersonalFeed(String user, long mid) {
        System.out.println("Removing from personal feed: " + mid);
        var ufi = feeds.get(user);
        synchronized (ufi.user()) {
            ufi.messages().remove(mid);
        }
        messages.keySet().removeAll( Set.of(mid) );
    }

    public void __SubUser(String user, String userSub) {
        System.out.println("Subscribing user: " + userSub);
        var ufi = feeds.computeIfAbsent(user, FeedInfo::new );
        synchronized (ufi.user()) {
            ufi.following().add(userSub);
        }

    }

    public void __UnsubscribeUser(String user, String userSub) {
        System.out.println("Unsubscribing user: " + userSub);
        FeedInfo ufi = feeds.computeIfAbsent(user, FeedInfo::new);
        synchronized (ufi.user()) {
            ufi.following().remove(userSub);
        }

    }

    public void __DeleteUserFeed(String user) {
        System.out.println("Deleting user feed: " + user);
        FeedInfo ufi = feeds.remove(user);
        synchronized (ufi.user()) {
            deleteFromUserFeed(user, ufi.messages());
            for (var u : ufi.followees())
                ufi.following().remove(u);
        }
    }
}
