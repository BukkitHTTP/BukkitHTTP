package nano.http.bukkit.mock.dirty;

import sun.reflect.ReflectionFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;

public class MakeAccessible {
    private static MethodHandle export = null;

    private static void makeExp() throws Throwable {
        if (export != null) {
            return;
        }
        Constructor<?> ctor = ReflectionFactory.getReflectionFactory().newConstructorForSerialization(MethodHandles.Lookup.class, MethodHandles.Lookup.class.getDeclaredConstructor(new Class[]{Class.class}));
        MethodHandles.Lookup lookup = (MethodHandles.Lookup) ctor.newInstance(new Object[]{AccessibleObject.class});
        export = lookup.findVirtual(AccessibleObject.class, "setAccessible0", MethodType.methodType(Boolean.TYPE, Boolean.TYPE));
    }

    public static void makeAccessible(AccessibleObject ao) {
        try {
            if (!ao.isAccessible()) {
                ao.setAccessible(true);
            }
        } catch (Exception e) {
            try {
                makeExp();
                export.invoke(ao, true);
            } catch (Throwable t) {
                throw new RuntimeException("Failed to make accessible", t);
            }
        }
    }
}
