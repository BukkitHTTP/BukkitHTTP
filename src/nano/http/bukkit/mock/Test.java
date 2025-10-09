package nano.http.bukkit.mock;

import nano.http.d2.consts.Mime;
import nano.http.d2.consts.Status;
import nano.http.d2.core.Response;
import nano.http.d2.utils.CommonRequest;

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

        System.out.println(CommonRequest.get("https://example.com/test?a=1&b=2", null));
    }
}
