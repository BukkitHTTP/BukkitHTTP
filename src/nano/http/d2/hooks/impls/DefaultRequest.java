package nano.http.d2.hooks.impls;

import nano.http.d2.consts.Mime;
import nano.http.d2.consts.Status;
import nano.http.d2.core.Response;
import nano.http.d2.hooks.interfaces.RequestHookProvider;
import nano.http.d2.serve.ServeProvider;

import java.util.Properties;

public class DefaultRequest implements RequestHookProvider {
    @Override
    public Response serve(String uri, String method, Properties header, Properties parms, Properties files, ServeProvider sp) {
        if (method.equals("OPTIONS")) {
            return new Response(Status.HTTP_NOCONTENT, Mime.MIME_PLAINTEXT, "Responded by : NanoHTTPd2");
        }
        if (uri.equals("/favicon.ico")) {
            return new Response(Status.HTTP_OK, Mime.MIME_DEFAULT_BINARY, DefaultRequest.class.getResourceAsStream("/META-INF/favicon.ico"));
        }
        return sp.serve(uri, method, header, parms, files);
    }
}
