package nano.http.d2.utils;

import sun.reflect.ReflectionFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.util.function.Supplier;

// SHUT THE F**K UP.
@SuppressWarnings("ALL")
public class CarrierThreadLocal {
    private static final Constructor<ThreadLocal<?>> tl;

    static {
        try {
            Constructor<?> ctor = ReflectionFactory.getReflectionFactory().newConstructorForSerialization(MethodHandles.Lookup.class, MethodHandles.Lookup.class.getDeclaredConstructor(Class.class));
            MethodHandles.Lookup lookup = (MethodHandles.Lookup) ctor.newInstance(new Object[]{AccessibleObject.class});
            MethodHandle export = lookup.findVirtual(AccessibleObject.class, "setAccessible0", MethodType.methodType(Boolean.TYPE, Boolean.TYPE));

            Class<?> clz = Class.forName("jdk.internal.misc.CarrierThreadLocal");
            tl = (Constructor<ThreadLocal<?>>) clz.getConstructor();
            export.invoke(tl, true);
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

    public static <T> ThreadLocal<T> withInitial(Supplier<? extends T> initialValue) {
        try {
            return tl.newInstance().withInitial(initialValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
