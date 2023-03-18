package nano.http.d2.session;

import java.util.*;

@SuppressWarnings("unused")
public class Session {
    public final List<String> permissions = new ArrayList<>();
    private final Map<String, Object> attributes = new HashMap<>();
    public long lastAccess = System.currentTimeMillis();

    public void grantPermissions(String... permissions) {
        for (String perm : permissions) {
            if (!this.permissions.contains(perm)) {
                this.permissions.add(perm);
            }
        }
    }

    public boolean hasPermissions(String... permissions) {
        for (String permission : permissions) {
            if (!this.permissions.contains(permission)) {
                return false;
            }
        }
        return true;
    }

    public void revokePermissions(String... permission) {
        for (String perm : permission) {
            permissions.remove(perm);
        }
    }

    public void clearPermissions() {
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

    public void clearAttributes() {
        attributes.clear();
    }

    public void reset() {
        clearAttributes();
        clearPermissions();
    }
}
