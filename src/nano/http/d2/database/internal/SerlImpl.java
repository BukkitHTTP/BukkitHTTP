package nano.http.d2.database.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SerlImpl {
    private static void writeBool(boolean value, OutputStream os) throws IOException {
        os.write(value ? 2 : 4);
    }

    private static boolean readBool(InputStream is) throws IOException {
        int val = is.read();
        if (val == 2) return true;
        if (val == 4) return false;
        if (val == -1) throw new IOException("Unexpected end of stream");
        throw new IOException("Invalid boolean value: " + val);
    }

    private static void writeByte(byte value, OutputStream os) throws IOException {
        os.write(value);
    }

    private static byte readByte(InputStream is) throws IOException {
        int val = is.read();
        if (val == -1) throw new IOException("Unexpected end of stream");
        return (byte) val;
    }

    private static void writeShort(short value, OutputStream os) throws IOException {
        os.write((value >> 8) & 0xFF);
        os.write(value & 0xFF);
    }

    private static short readShort(InputStream is) throws IOException {
        int high = is.read();
        int low = is.read();
        if (high == -1 || low == -1) throw new IOException("Unexpected end of stream");
        return (short) ((high << 8) | low);
    }

    private static void writeChar(char value, OutputStream os) throws IOException {
        os.write((value >> 8) & 0xFF);
        os.write(value & 0xFF);
    }

    private static char readChar(InputStream is) throws IOException {
        int high = is.read();
        int low = is.read();
        if (high == -1 || low == -1) throw new IOException("Unexpected end of stream");
        return (char) ((high << 8) | low);
    }

    private static void writeInt(int value, OutputStream os) throws IOException {
        os.write((value >> 24) & 0xFF);
        os.write((value >> 16) & 0xFF);
        os.write((value >> 8) & 0xFF);
        os.write(value & 0xFF);
    }

    private static int readInt(InputStream is) throws IOException {
        int b1 = is.read();
        int b2 = is.read();
        int b3 = is.read();
        int b4 = is.read();
        if (b1 == -1 || b2 == -1 || b3 == -1 || b4 == -1) throw new IOException("Unexpected end of stream");
        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }

    static void writeLong(long value, OutputStream os) throws IOException {
        os.write((int) ((value >> 56) & 0xFF));
        os.write((int) ((value >> 48) & 0xFF));
        os.write((int) ((value >> 40) & 0xFF));
        os.write((int) ((value >> 32) & 0xFF));
        os.write((int) ((value >> 24) & 0xFF));
        os.write((int) ((value >> 16) & 0xFF));
        os.write((int) ((value >> 8) & 0xFF));
        os.write((int) (value & 0xFF));
    }

    static long readLong(InputStream is) throws IOException {
        long b1 = is.read();
        long b2 = is.read();
        long b3 = is.read();
        long b4 = is.read();
        long b5 = is.read();
        long b6 = is.read();
        long b7 = is.read();
        long b8 = is.read();
        if (b1 == -1 || b2 == -1 || b3 == -1 || b4 == -1 || b5 == -1 || b6 == -1 || b7 == -1 || b8 == -1) throw new IOException("Unexpected end of stream");
        return (b1 << 56) | (b2 << 48) | (b3 << 40) | (b4 << 32) | (b5 << 24) | (b6 << 16) | (b7 << 8) | b8;
    }

    private static void writeFloat(float value, OutputStream os) throws IOException {
        writeInt(Float.floatToIntBits(value), os);
    }

    private static float readFloat(InputStream is) throws IOException {
        return Float.intBitsToFloat(readInt(is));
    }

    private static void writeDouble(double value, OutputStream os) throws IOException {
        writeLong(Double.doubleToLongBits(value), os);
    }

    private static double readDouble(InputStream is) throws IOException {
        return Double.longBitsToDouble(readLong(is));
    }

    private static void writeString(String value, OutputStream os) throws IOException {
        if (value == null) {
            writeInt(-1, os);
            return;
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeInt(bytes.length, os);
        os.write(bytes);
    }

    private static String readString(InputStream is) throws IOException {
        int length = readInt(is);
        if (length == -1) return null;
        if (length < 0) throw new IOException("Invalid string length: " + length);
        byte[] bytes = new byte[length];
        int read = is.read(bytes);
        if (read != length) throw new IOException("Unexpected end of stream");
        return new String(bytes, StandardCharsets.UTF_8);
    }

    static void writeObject(Object obj, OutputStream os, SerlCtx ctx) throws IOException {
        if (obj == null) {
            writeInt(-1, os);
            return;
        }
        Class<?> cls = obj.getClass();
        if (cls == Boolean.class) {
            writeInt(-2, os);
            writeBool((Boolean) obj, os);
        } else if (cls == Byte.class) {
            writeInt(-3, os);
            writeByte((Byte) obj, os);
        } else if (cls == Short.class) {
            writeInt(-4, os);
            writeShort((Short) obj, os);
        } else if (cls == Character.class) {
            writeInt(-5, os);
            writeChar((Character) obj, os);
        } else if (cls == Integer.class) {
            writeInt(-6, os);
            writeInt((Integer) obj, os);
        } else if (cls == Long.class) {
            writeInt(-7, os);
            writeLong((Long) obj, os);
        } else if (cls == Float.class) {
            writeInt(-8, os);
            writeFloat((Float) obj, os);
        } else if (cls == Double.class) {
            writeInt(-9, os);
            writeDouble((Double) obj, os);
        } else if (cls == String.class) {
            writeInt(-10, os);
            writeString((String) obj, os);
        } else if (cls.isArray()) {
            writeInt(-11, os);
            writeString(cls.getName(), os);
            int length = Array.getLength(obj);
            writeInt(length, os);
            for (int i = 0; i < length; i++) {
                writeObject(Array.get(obj, i), os, ctx);
            }
        } else if (cls.isEnum()) {
            writeInt(-12, os);
            writeString(cls.getName(), os);
            writeString(((Enum<?>) obj).name(), os);
        } else if (Map.class.isAssignableFrom(cls)) {
            writeInt(-13, os);
            writeString(cls.getName(), os);
            Map<?, ?> map = (Map<?, ?>) obj;
            writeInt(map.size(), os);
            for (Map.Entry<?, ?> e : map.entrySet()) {
                writeObject(e.getKey(), os, ctx);
                writeObject(e.getValue(), os, ctx);
            }
        } else if (Collection.class.isAssignableFrom(cls)) {
            writeInt(-14, os);
            writeString(cls.getName(), os);
            Collection<?> collection = (Collection<?>) obj;
            writeInt(collection.size(), os);
            for (Object item : collection) {
                writeObject(item, os, ctx);
            }
        } else {
            if (!ctx.seen.add(obj)) {
                throw new IOException("Cyclic reference detected");
            }
            SerlCtx.ClassWithId classWithId = ctx.cache.get(cls);
            if (classWithId == null) {
                if (!cls.isAnnotationPresent(SerlClz.class)) {
                    throw new IOException("Class " + cls.getName() + " is not marked as safely serializable with @SerlClz");
                }

                int id = ctx.classTypeId++;
                Field[] fields = cls.getFields();
                List<Field> serializableFields = new ArrayList<>();
                for (Field field : fields) {
                    if (!(Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers()))) {
                        String name = field.getName();
                        if (!name.startsWith("_") && !name.startsWith("$") && !name.equals("serialVersionUID")) {
                            serializableFields.add(field);
                        }
                    }
                }
                Field[] fArray = serializableFields.toArray(new Field[0]);
                ctx.cache.put(cls, new SerlCtx.ClassWithId(id, fArray));

                classWithId = new SerlCtx.ClassWithId(id, fArray);

                // Class definition header
                writeInt(-999, os);
                // ID is reconstructable from reading order
                writeString(cls.getName(), os);
                writeInt(fArray.length, os);
                for (Field field : fArray) {
                    writeString(field.getName(), os);
                }
            }
            // System.out.println("Writing typeId: " + classWithId.id + " for class: " + cls.getName());
            writeInt(classWithId.id, os);
            for (Field field : classWithId.fields) {
                try {
                    Object fieldValue = field.get(obj);
                    writeObject(fieldValue, os, ctx);
                } catch (IllegalAccessException e) {
                    throw new IOException("Failed to access field: " + field.getName(), e);
                }
            }
        }
    }

    static Object readObject(InputStream is, DeSerlCtx ctx) throws IOException {
        int typeId = readInt(is);
        if (typeId == -1) return null;
        if (typeId == -2) return readBool(is);
        if (typeId == -3) return readByte(is);
        if (typeId == -4) return readShort(is);
        if (typeId == -5) return readChar(is);
        if (typeId == -6) return readInt(is);
        if (typeId == -7) return readLong(is);
        if (typeId == -8) return readFloat(is);
        if (typeId == -9) return readDouble(is);
        if (typeId == -10) return readString(is);
        if (typeId == -11) {
            String arrayTypeName = readString(is);
            Class<?> arrayType = ctx.typeMap.get(arrayTypeName);
            if (arrayType == null) {
                try {
                    arrayType = Class.forName(arrayTypeName, true, ctx.classLoader);
                    ctx.typeMap.put(arrayTypeName, arrayType);
                } catch (ClassNotFoundException e) {
                    throw new IOException("Failed to load array class: " + arrayTypeName, e);
                }
            }
            int length = readInt(is);
            Object array = Array.newInstance(arrayType.getComponentType(), length);
            for (int i = 0; i < length; i++) {
                Array.set(array, i, readObject(is, ctx));
            }
            return array;
        }

        if (typeId == -12) {
            String name = readString(is);
            Class<?> enumClass = ctx.typeMap.get(name);
            if (enumClass == null) {
                try {
                    enumClass = Class.forName(name, true, ctx.classLoader);
                    ctx.typeMap.put(enumClass.getName(), enumClass);
                } catch (ClassNotFoundException e) {
                    throw new IOException("Failed to load enum class", e);
                }
            }
            //noinspection ALL
            return Enum.valueOf((Class<Enum>) enumClass, readString(is));
        }

        if (typeId == -13) {
            String mapTypeName = readString(is);
            Constructor<?> mapType = ctx.constructorMap.get(mapTypeName);
            if (mapType == null) {
                try {
                    Class<?> mapClass = Class.forName(mapTypeName, true, ctx.classLoader);
                    mapType = mapClass.getConstructor();
                    ctx.constructorMap.put(mapTypeName, mapType);
                } catch (ClassNotFoundException | NoSuchMethodException e) {
                    throw new IOException("Failed to load map class: " + mapTypeName, e);
                }
            }

            int size = readInt(is);
            Map<Object, Object> map;
            try {
                //noinspection unchecked
                map = (Map<Object, Object>) mapType.newInstance();
            } catch (Exception e) {
                throw new IOException("Failed to instantiate map class: " + mapTypeName, e);
            }
            for (int i = 0; i < size; i++) {
                Object key = readObject(is, ctx);
                Object value = readObject(is, ctx);
                map.put(key, value);
            }
            return map;
        }

        if (typeId == -14) {
            String collTypeName = readString(is);
            Constructor<?> collType = ctx.constructorMap.get(collTypeName);
            if (collType == null) {
                try {
                    Class<?> collClass = Class.forName(collTypeName, true, ctx.classLoader);
                    collType = collClass.getConstructor();
                    ctx.constructorMap.put(collTypeName, collType);
                } catch (ClassNotFoundException | NoSuchMethodException e) {
                    throw new IOException("Failed to load collection class: " + collTypeName, e);
                }
            }

            int size = readInt(is);
            Collection<Object> collection;
            try {
                //noinspection unchecked
                collection = (Collection<Object>) collType.newInstance();
            } catch (Exception e) {
                throw new IOException("Failed to instantiate collection class: " + collTypeName, e);
            }
            for (int i = 0; i < size; i++) {
                Object item = readObject(is, ctx);
                collection.add(item);
            }
            return collection;
        }

        if (typeId == -999) {
            String className = readString(is);
            try {
                Class<?> cls = Class.forName(className, true, ctx.classLoader);
                if (!cls.getAnnotation(SerlClz.class).annotationType().equals(SerlClz.class)) {
                    throw new IOException("Class " + className + " is not marked as safely serializable with @SerlClz");
                }
                Constructor<?> constructor = cls.getConstructor();
                int fieldCount = readInt(is);
                Field[] fields = new Field[fieldCount];
                for (int i = 0; i < fieldCount; i++) {
                    String fieldName = readString(is);
                    assert fieldName != null;
                    try {
                        fields[i] = cls.getField(fieldName);
                    } catch (NoSuchFieldException e) {
                        ctx.isDirty = true;
                    }
                }
                Method m = null;
                try {
                    m = cls.getDeclaredMethod("__");
                } catch (NoSuchMethodException ignored) {
                }
                ctx.classList.add(new DeSerlCtx.ClassWithId(cls, constructor, fields, m));
                return readObject(is, ctx); // Read the actual object now that class is registered
            } catch (ClassNotFoundException e) {

                throw new IOException("Failed to load class: " + className, e);
            } catch (NoSuchMethodException e) {
                throw new IOException("No default constructor for class: " + className, e);
            }
        }

        DeSerlCtx.ClassWithId classWithId = ctx.classList.get(typeId - 1);
        Object obj;
        try {
            obj = classWithId.constructor.newInstance();
        } catch (Exception e) {
            throw new IOException("Failed to instantiate class: " + classWithId.cls.getName(), e);
        }
        for (Field field : classWithId.fields) {
            if (field != null) {
                Object fieldValue = readObject(is, ctx);
                try {
                    field.set(obj, fieldValue);
                } catch (IllegalAccessException e) {
                    throw new IOException("Failed to set field: " + field.getName(), e);
                }
            } else {
                readObject(is, ctx);
            }
        }
        if (classWithId.__ != null) {
            try {
                classWithId.__.invoke(obj);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IOException("Class refused to get deserialized: " + classWithId.cls.getName(), e);
            }
        }
        return obj;
    }
}
