package nano.http.bukkit.mock;

import nano.http.d2.consts.Mime;
import nano.http.d2.consts.Status;
import nano.http.d2.core.Response;
import nano.http.d2.serve.ServeProvider;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class Test {
    public static void main(String[] args) throws Exception {
        InjectRegistry.register("example.com", (uri, method, header, parms, files) -> {
            System.out.println("uri = " + uri);
            System.out.println("method = " + method);
            for (String key : header.stringPropertyNames()) {
                System.out.println("header: " + key + " = " + header.getProperty(key));
            }
            for (String key : parms.stringPropertyNames()) {
                System.out.println("parms: " + key + " = " + parms.getProperty(key));
            }
            return new Response(Status.HTTP_OK, Mime.MIME_PLAINTEXT, "GG");
        });

        HttpURLConnection connection = (HttpURLConnection) new URL("https://example.com/?q=ret-1-plz").openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.connect();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = connection.getInputStream().read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        System.out.println(out.toString("UTF-8"));
    }
}
