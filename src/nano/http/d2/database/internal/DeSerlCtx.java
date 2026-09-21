package nano.http.d2.database.internal;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeSerlCtx {
    final ClassLoader classLoader;
    final List<ClassWithId> classList = new ArrayList<>();
    final Map<String, Class<?>> typeMap = new HashMap<>();
    final Map<String, Constructor<?>> constructorMap = new HashMap<>();
    boolean isDirty = false;

    // The inflated source stream, bound by SerlBridge before parsing.
    // While bound, every length field in the data can (and must) be
    // validated against the bytes actually present, so a crafted file
    // cannot request giant allocations up front.
    ByteArrayInputStream source = null;

    int remaining() {
        return source == null ? Integer.MAX_VALUE : source.available();
    }

    public DeSerlCtx(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    static class ClassWithId {
        final Class<?> cls;
        final Constructor<?> constructor;
        final Field[] fields;
        final Method __;

        ClassWithId(Class<?> cls, Constructor<?> constructor, Field[] fields, Method __) {
            this.cls = cls;
            this.constructor = constructor;
            this.fields = fields;
            this.__ = __;
        }
    }
}
