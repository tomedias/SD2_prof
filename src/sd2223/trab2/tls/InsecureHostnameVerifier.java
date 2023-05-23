package sd2223.trab2.tls;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class InsecureHostnameVerifier implements HostnameVerifier {
    @Override
    public boolean verify(String s, SSLSession sslSession) {
        System.out.println(s);
        System.out.println(sslSession.getPeerHost());
        return true;
    }
}
