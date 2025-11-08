package nano.http.d2.database.internal;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SerlCtx {
    final Set<Object> seen = new HashSet<>();
    final Map<Class<?>, ClassWithId> cache = new HashMap<>();
    int classTypeId = 1;

    // Dumb idea - Record subclass has private for default permission
    @SuppressWarnings("ClassCanBeRecord")
    static class ClassWithId {
        final int id;
        final Field[] fields;

        ClassWithId(int id, Field[] fields) {
            this.id = id;
            this.fields = fields;
        }
    }
}
