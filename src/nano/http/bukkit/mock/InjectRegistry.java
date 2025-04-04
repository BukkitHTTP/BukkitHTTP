package nano.http.bukkit.mock;

import nano.http.d2.serve.ServeProvider;

import java.util.HashMap;
import java.util.Map;

public class InjectRegistry {
    private static final Map<String, ServeProvider> registry = new HashMap<>();

    private static boolean initialized = false;

    private static void doInject() {
        if (initialized) {
            return;
        }
        initialized = true;
        InjectURI.inject();
    }

    public static ServeProvider get(String name) {
        doInject();
        return registry.get(name);
    }

    public static void register(String name, ServeProvider provider) {
        doInject();
        registry.put(name, provider);
    }
}
