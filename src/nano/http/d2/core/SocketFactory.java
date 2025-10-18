package nano.http.d2.core;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.security.KeyStore;

public class SocketFactory {
    public static ServerSocket createServerSocket(int port) throws Exception {
        if (port == 443) {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            char[] passphrase = "123456".toCharArray();
            @SuppressWarnings("IOStreamConstructor")
            InputStream keystoreStream = new FileInputStream("test.jks");
            keystore.load(keystoreStream, passphrase);
            keystoreStream.close();
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keystore, passphrase);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keystore);
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            SSLServerSocketFactory res = ctx.getServerSocketFactory();

            SSLServerSocket ss;
            ss = (SSLServerSocket) res.createServerSocket();
            ss.setEnabledProtocols(ss.getSupportedProtocols());
            ss.setUseClientMode(false);
            ss.setWantClientAuth(false);
            ss.setNeedClientAuth(false);
            ss.bind(new java.net.InetSocketAddress(port));
            return ss;
        } else {
            return new ServerSocket(port);
        }
    }
}
