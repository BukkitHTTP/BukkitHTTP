package nano.http.d2.hooks.impls;

import nano.http.d2.console.Console;
import nano.http.d2.console.Logger;
import nano.http.d2.consts.Mime;
import nano.http.d2.consts.Status;
import nano.http.d2.core.Response;
import nano.http.d2.hooks.interfaces.RequestHookProvider;
import nano.http.d2.serve.ServeProvider;
import nano.http.d2.session.Captcha;
import nano.http.d2.session.Session;
import nano.http.d2.session.SessionManager;

import java.io.ByteArrayInputStream;
import java.util.Properties;

public class DefaultRequest implements RequestHookProvider {
    private static String ACME = null;

    static {
        Console.register("acme", () -> {
            Logger.info("ACME:");
            ACME = Console.await();
            Logger.info("ACME is ready.");
        });
    }

    @Override
    public Response serve(String uri, String method, Properties header, Properties parms, Properties files, ServeProvider sp, String ip) {
        if (uri.contains("acme-challenge") && ACME != null) {
            return new Response(Status.HTTP_OK, Mime.MIME_PLAINTEXT, ACME);
        }
        if (method.equals("OPTIONS") || method.equals("HEAD")) {
            return new Response(Status.HTTP_NOCONTENT, Mime.MIME_PLAINTEXT, "Responded by : NanoHTTPd2");
        }
        if (!(method.equals("PUT") || method.equals("POST") || method.equals("GET"))) {
            DefaultSock.strike(ip, 20);
            return new Response(Status.HTTP_NOTIMPLEMENTED, Mime.MIME_PLAINTEXT, "Sorry but NanoHTTPd2 has not implemented this method yet.");
        }
        if (uri.equals("/favicon.ico")) {
            return new Response(Status.HTTP_OK, Mime.MIME_DEFAULT_BINARY, DefaultRequest.class.getResourceAsStream("/META-INF/favicon.ico"));
        }
        if (uri.equals("/captcha.jpg")) {
            Session s = SessionManager.getSession(header);
            if (s == null) {
                return new Response(Status.HTTP_FORBIDDEN, Mime.MIME_PLAINTEXT, "Please Allow Cookies");
            }
            Object captcha = s.getAttribute("b_captcha");
            if (captcha == null) {
                captcha = Captcha.generateCaptcha();
                s.setAttribute("b_captcha", captcha);
            }
            //noinspection DataFlowIssue
            return new Response(Status.HTTP_OK, Mime.MIME_JPEG, new ByteArrayInputStream(Captcha.drawImage((String) captcha)));
        }
        parms.setProperty("IP", ip);
        return sp.serve(uri, method, header, parms, files);
    }
}
