package sd2223.trab2.servers.java;

import sd2223.trab2.api.Message;
import sd2223.trab2.api.java.Result;
import sd2223.trab2.kafka.KafkaPublisher;
import sd2223.trab2.kafka.KafkaSubscriber;
import utils.JSON;
import java.util.*;
import static sd2223.trab2.api.java.Result.ErrorCode.*;
import static sd2223.trab2.api.java.Result.error;
import static sd2223.trab2.api.java.Result.ok;

public class JavaFeedsRep extends JavaFeedsPull{

    static final String TOPIC = "topic";

    static final String KAFKA_BROKERS = "kafka:9092"; // When running in docker container...

    final KafkaPublisher publisher;
    static final String FROM_BEGINNING = "earliest";

    public JavaFeedsRep(){
        super();
        publisher = KafkaPublisher.createPublisher(KAFKA_BROKERS);
        startKafka();
        System.out.println("Continue Kafka publisher");
        //Catch up sleep :3
        try
        {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startKafka(){
        KafkaSubscriber subscriber = KafkaSubscriber.createSubscriber(JavaFeedsRep.KAFKA_BROKERS, List.of(JavaFeedsRep.TOPIC), JavaFeedsRep.FROM_BEGINNING);
        subscriber.start(false, (r) -> {
            String[] command = r.value().split(" ");
            switch (command[0]) {
                case "postMessage" -> __PostMessage(command[1], JSON.decode(command[3], Message.class));
                case "removeFromPersonalFeed" ->
                        __RemoveFromPersonalFeed(command[1], Long.parseLong(command[2]));
                case "subUser" -> __SubUser(command[1], command[2]);
                case "unsubscribeUser" -> __UnsubscribeUser(command[1], command[2]);
                case "deleteUserFeed" -> __DeleteUserFeed(command[1]);
                default -> System.err.println("Unknown command: " + command[0]);
            }
        });
    }

    private long send( String msg) {
        long offset = publisher.publish(TOPIC, msg);
        if (offset >= 0)
            System.out.println("Message published with sequence number: " + msg);
        else
            System.err.println("Failed to publish message");
        return offset;

    }

    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        var preconditionsResult = preconditions.postMessage(user, pwd,msg);
        if( ! preconditionsResult.isOK() )
            return preconditionsResult;
        long mid = serial.incrementAndGet();
        msg.setId(mid);
        msg.setCreationTime(System.currentTimeMillis());
        send("postMessage " + user + " " + pwd + " " + JSON.encode(msg));
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
        send("removeFromPersonalFeed " + user + " " + mid + " " + pwd);


        return ok();
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        var preconditionsResult = preconditions.subUser(user, userSub, pwd);
        if( ! preconditionsResult.isOK() )
            return preconditionsResult;
        send("subUser " + user + " " + userSub + " " + pwd);
        return ok();
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        var preconditionsResult = preconditions.unsubscribeUser(user, userSub, pwd);
        if( ! preconditionsResult.isOK() )
            return preconditionsResult;
        send("unsubscribeUser " + user + " " + userSub + " " + pwd);
        return ok();
    }

    @Override
    public Result<Void> deleteUserFeed(String user) {
        var preconditionsResult = preconditions.deleteUserFeed(user);
        if( ! preconditionsResult.isOK() )
            return preconditionsResult;
        if(feeds.get(user) == null)
            return error(NOT_FOUND);
        send("deleteUserFeed " + user);
        return ok();
    }

    private void __PostMessage(String user, Message msg) {
        System.out.println("postMessage " + user + " " + msg);
        FeedInfo ufi = feeds.computeIfAbsent(user, FeedInfo::new );
        synchronized (ufi.user()) {
            ufi.messages().add(msg.getId());
            messages.putIfAbsent(msg.getId(), msg);
        }
    }

    private void __RemoveFromPersonalFeed(String user, long mid) {
        var ufi = feeds.get(user);
        synchronized (ufi.user()) {
            ufi.messages().remove(mid);
        }
        deleteFromUserFeed( user, Set.of(mid) );
    }

    private void __SubUser(String user, String userSub) {
        var ufi = feeds.computeIfAbsent(user, FeedInfo::new );
        synchronized (ufi.user()) {
            ufi.following().add(userSub);
        }

    }

    private void __UnsubscribeUser(String user, String userSub) {
        FeedInfo ufi = feeds.computeIfAbsent(user, FeedInfo::new);
        synchronized (ufi.user()) {
            ufi.following().remove(userSub);
        }

    }

    private void __DeleteUserFeed(String user) {
        FeedInfo ufi = feeds.remove(user);
        synchronized (ufi.user()) {
            deleteFromUserFeed(user, ufi.messages());
            for (var u : ufi.followees())
                ufi.following().remove(u);
        }
    }
}
