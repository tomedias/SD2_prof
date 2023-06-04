package sd2223.trab2.servers;

public class Domain {
	static String domain;
	static long uuid;

	static String secret;
	
	public  static void set( String _domain, long _uuid) {
		domain = _domain;
		uuid = _uuid;
	}

	public static String getSecret() {
		return secret;
	}

	public static void setSecret(String secret) {
		Domain.secret = secret;
	}

	public static String get() {
		return domain;
	}

	public static long uuid() {
		return uuid;
	}
	
	public static boolean isRemoteUser(String user) {
		var parts = user.split("@");
		return parts.length > 1 && ! parts[1].equals( domain );
	}
}
