package nano.http.bukkit.mock.impl;

import nano.http.d2.core.ParmsDecoder;
import nano.http.d2.core.Response;
import nano.http.d2.serve.ServeProvider;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.util.Properties;

public class ModifiedCon extends HttpsURLConnection {
    private final Proxy proxy;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();


    private final Properties properties;

    private Response resp = null;
    private final ServeProvider provider;

    private void ensureResp() {
        if (resp != null) {
            return;
        }
        ParmsDecoder.decodeParms(new String(outputStream.toByteArray(), StandardCharsets.UTF_8), properties);
        resp = provider.serve(uri, method, headers, properties, new Properties());
    }

    private String method = "GET";

    @Override
    public void setRequestMethod(String method) {
        this.method = method;
    }

    private final Properties headers = new Properties();

    @Override
    public void setRequestProperty(String key, String value) {
        headers.put(key, value);
    }

    @Override
    public void addRequestProperty(String key, String value) {
        headers.put(key, value);
    }

    public final String uri;

    public ModifiedCon(URL url, Proxy proxy, Properties query, ServeProvider provider, String uri) {
        super(url);
        this.proxy = proxy;
        this.properties = query;
        this.provider = provider;
        this.uri = uri;
    }

    @Override
    public int getResponseCode() {
        ensureResp();
        return 200;
    }


    @Override
    public void disconnect() {
    }

    @Override
    public boolean usingProxy() {
        return proxy != null;
    }

    @Override
    public void connect() {
    }

    @Override
    public InputStream getInputStream() {
        ensureResp();
        return resp.data;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public String getCipherSuite() {
        return "mocked";
    }

    @Override
    public Certificate[] getLocalCertificates() {
        return new Certificate[0];
    }

    @Override
    public Certificate[] getServerCertificates() {
        return new Certificate[0];
    }
}
