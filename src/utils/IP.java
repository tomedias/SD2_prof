package utils;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

public class IP {

	private static String URI;

	public static String hostAddress() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return "?.?.?.?";
		}
	}
	public static String hostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return "?.?.?.?";
		}
	}

	public static void setUri(String uri) {
		URI = uri;

	}
	public static String getUri() {
		return URI;
	}
}