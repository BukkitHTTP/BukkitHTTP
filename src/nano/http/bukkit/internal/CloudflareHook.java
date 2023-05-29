package nano.http.bukkit.internal;

import nano.http.d2.consts.Mime;
import nano.http.d2.consts.Status;
import nano.http.d2.core.Response;
import nano.http.d2.hooks.interfaces.RequestHookProvider;
import nano.http.d2.serve.ServeProvider;

import java.util.Properties;

public class CloudflareHook implements RequestHookProvider {
    private final RequestHookProvider sock;

    public CloudflareHook(RequestHookProvider dad) {
        sock = dad;
    }

    @Override
    public Response serve(String uri, String method, Properties header, Properties parms, Properties files, ServeProvider sp, String ip) {
        if (header.containsKey("cf-connecting-ip")) {
            ip = header.getProperty("cf-connecting-ip");
            return sock.serve(uri, method, header, parms, files, sp, ip);
        }
        return new Response(Status.HTTP_NOTFOUND, Mime.MIME_PLAINTEXT, "404");
    }
}
