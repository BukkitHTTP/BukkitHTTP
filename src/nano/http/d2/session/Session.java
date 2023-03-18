package nano.http.d2.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class Session {
    private final List<String> permissions = new ArrayList<>();
    private final Map<String, Object> attributes = new HashMap<>();
    public long lastAccess = System.currentTimeMillis();

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public void grantPermission(String permission) {
        if (!hasPermission(permission)) {
            permissions.add(permission);
        }
    }

    public boolean hasPermissions(String... permissions) {
        for (String permission : permissions) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    public void revokePermission(String permission) {
        if (hasPermission(permission)) {
            permissions.remove(permission);
        }
    }

    public void revokeAllPermissions() {
        permissions.clear();
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void removeAttribute(String key) {
        attributes.remove(key);
    }
}
