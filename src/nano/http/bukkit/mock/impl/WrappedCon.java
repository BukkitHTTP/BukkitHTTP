package nano.http.bukkit.mock.impl;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;

public class WrappedCon extends HttpsURLConnection {
    private final HttpURLConnection impl;
    private final Map<String, String> requestHeaders = new HashMap<>();
    private InceptedInputStream in;
    private InceptedOutputStream out;


    public WrappedCon(HttpURLConnection impl, URL url) {
        super(url);
        this.impl = impl;
    }

    @Override
    public String getCipherSuite() {
        if (impl instanceof HttpsURLConnection) {
            return ((HttpsURLConnection) impl).getCipherSuite();
        }
        return "";
    }

    @Override
    public Certificate[] getLocalCertificates() {
        if (impl instanceof HttpsURLConnection) {
            return ((HttpsURLConnection) impl).getLocalCertificates();
        }
        return new Certificate[0];
    }

    @Override
    public Certificate[] getServerCertificates() throws SSLPeerUnverifiedException {
        if (impl instanceof HttpsURLConnection) {
            return ((HttpsURLConnection) impl).getServerCertificates();
        }
        return new Certificate[0];
    }

    @Override
    public void disconnect() {
        impl.disconnect();
    }

    @Override
    public boolean usingProxy() {
        return impl.usingProxy();
    }

    @Override
    public void connect() throws IOException {
        impl.connect();
    }

    @Override
    public void setRequestMethod(String method) throws ProtocolException {
        impl.setRequestMethod(method);
    }

    @Override
    public void setRequestProperty(String key, String value) {
        requestHeaders.put(key, value);
        impl.setRequestProperty(key, value);
    }

    @Override
    public String getHeaderField(int n) {
        return impl.getHeaderField(n);
    }

    @Override
    public int getResponseCode() throws IOException {
        return impl.getResponseCode();
    }

    @Override
    public InputStream getErrorStream() {
        return impl.getErrorStream();
    }

    @Override
    public void setDoOutput(boolean dooutput) {
        impl.setDoOutput(dooutput);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (in == null) {
            System.out.println("Observed Connection to: " + impl.getURL() + " with method: " + impl.getRequestMethod());
            for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                System.out.println("> C2S Header: " + entry.getKey() + " = " + entry.getValue());
            }
            for (String key : impl.getHeaderFields().keySet()) {
                System.out.println("> S2C Header: " + key + " = " + impl.getHeaderField(key));
            }
            in = new InceptedInputStream(impl.getInputStream());
        }
        return in;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (out == null) {
            out = new InceptedOutputStream(impl.getOutputStream());
        }
        return out;
    }
}

class InceptedInputStream extends InputStream {
    private final InputStream impl;
    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public InceptedInputStream(InputStream impl) {
        this.impl = impl;
    }

    @Override
    public int read() throws IOException {
        int data = impl.read();
        buffer.write(data);
        return data;
    }

    @Override
    public void close() throws IOException {
        if (buffer != null) {
            System.out.println("Observed S2C Response Body: " + buffer.toString(StandardCharsets.UTF_8));
            buffer = null;
        }
        impl.close();
    }
}

class InceptedOutputStream extends OutputStream {
    private final OutputStream impl;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public InceptedOutputStream(OutputStream impl) {
        this.impl = impl;
    }

    @Override
    public void write(int b) throws IOException {
        buffer.write(b);
        impl.write(b);
    }

    @Override
    public void close() throws IOException {
        System.out.println("Observed C2S Request Body: " + buffer.toString(StandardCharsets.UTF_8));
        super.close();
        impl.close();
    }
}
