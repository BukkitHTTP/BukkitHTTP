package nano.http.bukkit.api.debug;

import nano.http.bukkit.internal.Bukkit_Node;
import nano.http.d2.core.Response;
import nano.http.d2.serve.ServeProvider;

import java.util.Properties;

public class DebugRouter implements ServeProvider {
    private final Bukkit_Node node;
    private final String uri;

    public DebugRouter(Bukkit_Node node, String uri) {
        this.node = node;
        this.uri = uri;
    }

    @Override
    public Response serve(String uri, String method, Properties header, Properties parms, Properties files) {
        if (uri.startsWith(this.uri)) {
            return node.serve(new StringBuilder(uri).delete(0, this.uri.length()).toString(), method, header, parms, files);
        }
        return node.fallback(uri, method, header, parms, files);
    }
}
