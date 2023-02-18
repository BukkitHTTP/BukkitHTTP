package nano.http.d2.serve;

import nano.http.d2.console.Logger;
import nano.http.d2.core.Response;

import java.io.File;
import java.util.Enumeration;
import java.util.Properties;

public class ExampleServer implements ServeProvider {
    public Response serve(String uri, String method, Properties header, Properties parms, Properties files) {
        Logger.debug(method + " '" + uri + "' ");

        Enumeration<?> e = header.propertyNames();
        while (e.hasMoreElements()) {
            String value = (String) e.nextElement();
            Logger.debug("  HDR: '" + value + "' = '" +
                    header.getProperty(value) + "'");
        }
        e = parms.propertyNames();
        while (e.hasMoreElements()) {
            String value = (String) e.nextElement();
            Logger.debug("  PRM: '" + value + "' = '" +
                    parms.getProperty(value) + "'");
        }
        e = files.propertyNames();
        while (e.hasMoreElements()) {
            String value = (String) e.nextElement();
            Logger.debug("  UPLOADED: '" + value + "' = '" +
                    files.getProperty(value) + "'");
        }
        return FileServer.serveFile(uri, header, new File("."), true);
        // We make this an "ExampleServer" not only to print the request information,
        // But also to show how to use FileServer.serveFile() to serve files.
        // While you may find this class useless, you can still use FileServer.serveFile() aside your APIs.
    }
}
