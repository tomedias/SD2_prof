package sd2223.trab2.servers.java;

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

    final KafkaPublisher publisher;



    final SyncPoint<String> sync;

    private long version=-1L;

    private final String replicaID;





    public JavaFeedsRep(){
        super();
        this.replicaID = IP.hostName().split("-")[0].replaceFirst("feeds","");
        publisher = KafkaPublisher.createPublisher(KAFKA_BROKERS);
        this.sync = new SyncPoint<>();
        startKafka();
        setVersion(send("starting kafka"));
        sync();
    }

    private void sleep( int ms ) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void sync(){
        sync.waitForVersion(version,Integer.MAX_VALUE);

    }
    public void startKafka(){
        KafkaSubscriber subscriber = KafkaSubscriber.createSubscriber(JavaFeedsRep.KAFKA_BROKERS, List.of(JavaFeedsRep.TOPIC), JavaFeedsRep.FROM_BEGINNING);
        subscriber.start(false, (r) -> {
            String[] command = r.value().split(" ");
            switch (command[0]) {
                case "postMessage" ->
                        __PostMessage(command[1], JSON.decode(command[3], Message.class));
                case "removeFromPersonalFeed" ->
                        __RemoveFromPersonalFeed(command[1], Long.parseLong(command[2]));
                case "subUser" -> __SubUser(command[1], command[2]);
                case "unsubscribeUser" -> __UnsubscribeUser(command[1], command[2]);
                case "deleteUserFeed" -> __DeleteUserFeed(command[1]);
                default -> System.out.println("Starting replica " + IP.hostName());
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
        if (offset >= 0)
            System.out.println("Message published with sequence number: " + msg);
        else
            System.err.println("Failed to publish message");
        return offset;

    }

    @Override
    protected List<Message> getTimeFilteredPersonalFeed(String user, long time) {
        sync();
        System.out.println("Get time filtered personal feed");

        return super.getTimeFilteredPersonalFeed(user, time);
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        sync();
        System.out.println("Listing subs");

        return super.listSubs(user);
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {

        sync();
        System.out.println("Get Message");
        return super.getMessage(user, mid);
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        sync();
        System.out.println("Getting messages");

        return super.getMessages(user, time);
    }

    @Override
    public Result<List<Message>> pull_getTimeFilteredPersonalFeed(String user, long time) {
        sync();
        System.out.println("pull_getTimeFilteredPersonalFeed");
        return super.pull_getTimeFilteredPersonalFeed(user, time);
    }

    @Override
    protected void deleteFromUserFeed(String user, Set<Long> mids) {
        sync();
        System.out.println("Deleting from user feed");

        super.deleteFromUserFeed(user, mids);
    }

    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        sync();
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
        sync();

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
        sync();

        var preconditionsResult = preconditions.subUser(user, userSub, pwd);
        if( ! preconditionsResult.isOK() )
            return preconditionsResult;
        setVersion(send("subUser " + user + " " + userSub + " " + pwd));
        return ok();
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        sync();

        var preconditionsResult = preconditions.unsubscribeUser(user, userSub, pwd);
        if( ! preconditionsResult.isOK() )
            return preconditionsResult;
        setVersion(send("unsubscribeUser " + user + " " + userSub + " " + pwd));
        return ok();
    }

    @Override
    public Result<Void> deleteUserFeed(String user) {
        sync();

        var preconditionsResult = preconditions.deleteUserFeed(user);
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
