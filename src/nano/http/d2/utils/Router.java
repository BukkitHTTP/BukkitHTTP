package nano.http.d2.utils;

import nano.http.d2.consts.Mime;
import nano.http.d2.consts.Status;
import nano.http.d2.core.Response;
import nano.http.d2.serve.ServeProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@SuppressWarnings("unused")
public class Router implements ServeProvider {

    private final List<Node> nodes = new ArrayList<>();
    private final ServeProvider defaultSP;

    public Router(ServeProvider fallback) {
        defaultSP = fallback;
    }

    @Override
    public Response serve(String uri, String method, Properties header, Properties parms, Properties files) {
        for (Node node : nodes) {
            if (uri.startsWith(node.uri)) {
                StringBuilder sb = new StringBuilder();
                sb.append(uri);
                sb.delete(0, node.uri.length());
                return node.sp.serve(sb.toString(), method, header, parms, files);
            }
        }
        if (defaultSP != null) {
            return defaultSP.serve(uri, method, header, parms, files);
        }
        return new Response(Status.HTTP_NOTFOUND, Mime.MIME_PLAINTEXT, "404 Not Found - NanoRouter");
    }

    public boolean add(String path, ServeProvider sp) {
        for (Node node : nodes) {
            if (path.startsWith(node.uri)) {
                return false;
            }
        }
        nodes.add(new Node(path, sp));
        return true;
    }

    public void clear() {
        nodes.clear();
    }

    public boolean remove(String path) {
        for (Node node : nodes) {
            if (node.uri.equals(path)) {
                nodes.remove(node);
                return true;
            }
        }
        return false;
    }
}

class Node {
    public final String uri;
    public final ServeProvider sp;

    public Node(String uri, ServeProvider sp) {
        this.uri = uri;
        this.sp = sp;
    }
}
