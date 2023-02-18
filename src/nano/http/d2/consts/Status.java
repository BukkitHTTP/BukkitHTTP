package nano.http.d2.consts;

@SuppressWarnings("unused")
public class Status {
    /**
     * Some HTTP response status codes
     */
    public static final String
            HTTP_OK = "200 OK",
            HTTP_NOCONTENT = "204 No Content",
            HTTP_REDIRECT = "301 Moved Permanently",
            HTTP_FORBIDDEN = "403 Forbidden",
            HTTP_NOTFOUND = "404 Not Found",
            HTTP_BADREQUEST = "400 Bad Request",
            HTTP_INTERNALERROR = "500 Internal Server Error",
            HTTP_NOTIMPLEMENTED = "501 Not Implemented";
}
