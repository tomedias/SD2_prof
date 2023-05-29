package sd2223.trab2.api.java;

public interface FeedsRep extends FeedsPull{

    void setVersion(long version);

    long getVersion();
}
