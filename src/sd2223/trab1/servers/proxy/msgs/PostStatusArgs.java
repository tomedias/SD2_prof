package sd2223.trab1.servers.proxy.msgs;

public record PostStatusArgs(String status, String visibility) {

	public PostStatusArgs(String msg) {
		this(msg, "private");
	}
}