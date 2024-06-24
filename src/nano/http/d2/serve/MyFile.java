package nano.http.d2.serve;

import nano.http.d2.core.NanoHTTPd;
import nano.http.d2.core.Response;

import java.io.File;
import java.util.Properties;

public class MyFile {
    public static void main(String[] args) throws Exception {
        final File currentDir = new File(".");
        new NanoHTTPd(80, new ServeProvider() {
            @Override
            public Response serve(String uri, String method, Properties header, Properties parms, Properties files) {
                return FileServer.serveFile(uri, header, currentDir, true);
            }
        });
    }
}
