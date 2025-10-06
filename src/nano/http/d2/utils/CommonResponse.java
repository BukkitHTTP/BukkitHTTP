package nano.http.d2.utils;

import nano.http.d2.consts.Mime;
import nano.http.d2.consts.Status;
import nano.http.d2.core.Response;

import java.io.ByteArrayInputStream;

public class CommonResponse {
    public Response text(String text) {
        return new Response(Status.HTTP_OK, Mime.MIME_PLAINTEXT, Misc.BOM + text);
    }

    public Response redirect(String location) {
        Response res = new Response(Status.HTTP_REDIRECT);
        res.addHeader("Location", location);
        return res;
    }

    public Response file(String name, byte[] data) {
        Response res = new Response(Status.HTTP_OK, Mime.MIME_DEFAULT_BINARY, new ByteArrayInputStream(data));
        res.addHeader("Content-Disposition", "attachment; filename=\"" + name + "\"");
        return res;
    }
}
