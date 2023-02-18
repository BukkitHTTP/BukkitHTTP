package nano.http.d2.serve;

import nano.http.d2.core.Response;

import java.util.Properties;

public interface ServeProvider {
    /**
     * Override this to customize the server.<p>
     * <p/>
     * (By default, this delegates to serveFile() and allows directory listing.)
     *
     * @param uri    Percent-decoded URI without parameters, for example "/index.cgi"
     * @param method "GET", "POST" etc.
     * @param parms  Parsed, percent decoded parameters from URI and, in case of POST, data.
     * @param header Header entries, percent decoded
     * @return HTTP response, see class Response for details
     */
    Response serve(String uri, String method, Properties header, Properties parms, Properties files);
}
