// I've given up writing a JSON utility myself
// Please refer to the org.json for how can this package be used
// Do notice that this package is heavily downsized, and only this class behaves the same as the org.json
// - huzpsb

package nano.http.d2.json;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

@SuppressWarnings({"unused", "UnusedReturnValue", "UnnecessaryUnicodeEscape", "BooleanMethodIsAlwaysInverted"})
public class NanoJSON {
    public static final Object NULL = new Null();
    static final Pattern NUMBER_PATTERN = Pattern.compile("-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?");
    private final Map<String, Object> map;

    public NanoJSON() {
        this.map = new HashMap<>();
    }

    public NanoJSON(NanoJSON jo, String... names) {
        this(names.length);
        for (String name : names) {
            try {
                this.putOnce(name, jo.opt(name));
            } catch (Exception ignore) {
            }
        }
    }

    public NanoJSON(JSONTokener x) throws JSONException {
        this();
        char c;
        String key;
        if (x.nextClean() != '{') {
            throw x.syntaxError("A JSONObject text must begin with '{'");
        }
        for (; ; ) {
            char prev = x.getPrevious();
            c = x.nextClean();
            switch (c) {
                case 0:
                    throw x.syntaxError("A JSONObject text must end with '}'");
                case '}':
                    return;
                case '{':
                case '[':
                    if (prev == '{') {
                        throw x.syntaxError("A JSON Object can not directly nest another JSON Object or JSON Array.");
                    }
                default:
                    x.back();
                    key = x.nextValue().toString();
            }
            c = x.nextClean();
            if (c != ':') {
                throw x.syntaxError("Expected a ':' after a key");
            }
            if (key != null) {
                if (this.opt(key) != null) {
                    throw x.syntaxError("Duplicate key \"" + key + "\"");
                }
                Object value = x.nextValue();
                if (value != null) {
                    this.put(key, value);
                }
            }
            switch (x.nextClean()) {
                case ';':
                case ',':
                    if (x.nextClean() == '}') {
                        return;
                    }
                    x.back();
                    break;
                case '}':
                    return;
                default:
                    throw x.syntaxError("Expected a ',' or '}'");
            }
        }
    }

    public NanoJSON(Map<?, ?> m) {
        if (m == null) {
            this.map = new HashMap<>();
        } else {
            this.map = new HashMap<>(m.size());
            for (final Entry<?, ?> e : m.entrySet()) {
                if (e.getKey() == null) {
                    throw new NullPointerException("Null key.");
                }
                final Object value = e.getValue();
                if (value != null) {
                    this.map.put(String.valueOf(e.getKey()), wrap(value));
                }
            }
        }
    }

    // Attention: This constructor is not the same in the org.json
    // This constructor is used to convert a Nano-Styled DTO to a NanoJSON
    public NanoJSON(Object dto) {
        this();
        this.populateMap(dto);
    }

    private NanoJSON(Object bean, Set<Object> objectsRecord) {
        this();
        this.populateMap(bean, objectsRecord);
    }

    public NanoJSON(Object object, String... names) {
        this(names.length);
        Class<?> c = object.getClass();
        for (String name : names) {
            try {
                this.putOpt(name, c.getField(name).get(object));
            } catch (Exception ignore) {
            }
        }
    }

    public NanoJSON(String source) throws JSONException {
        this(new JSONTokener(source));
    }

    public NanoJSON(String baseName, Locale locale) throws JSONException {
        this();
        ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale, Thread.currentThread().getContextClassLoader());
        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (key != null) {
                String[] path = key.split("\\.");
                int last = path.length - 1;
                NanoJSON target = this;
                for (int i = 0; i < last; i += 1) {
                    String segment = path[i];
                    NanoJSON nextTarget = target.optJSONObject(segment);
                    if (nextTarget == null) {
                        nextTarget = new NanoJSON();
                        target.put(segment, nextTarget);
                    }
                    target = nextTarget;
                }
                target.put(path[last], bundle.getString(key));
            }
        }
    }

    protected NanoJSON(int initialCapacity) {
        this.map = new HashMap<>(initialCapacity);
    }

    // For compatibility with the legacy code
    @Deprecated
    public static String asJSON(Object o, Class<?> clazz) {
        return new NanoJSON(o).toString();
    }

    @Deprecated
    public static String asJSON(Object o) {
        return new NanoJSON(o).toString();
    }

    public static String doubleToString(double d) {
        if (Double.isInfinite(d) || Double.isNaN(d)) {
            return "null";
        }
        String string = Double.toString(d);
        if (string.indexOf('.') > 0 && string.indexOf('e') < 0 && string.indexOf('E') < 0) {
            while (string.endsWith("0")) {
                string = string.substring(0, string.length() - 1);
            }
            if (string.endsWith(".")) {
                string = string.substring(0, string.length() - 1);
            }
        }
        return string;
    }

    public static String[] getNames(NanoJSON jo) {
        if (jo.isEmpty()) {
            return null;
        }
        return jo.keySet().toArray(new String[jo.length()]);
    }

    public static String[] getNames(Object object) {
        if (object == null) {
            return null;
        }
        Class<?> klass = object.getClass();
        Field[] fields = klass.getFields();
        int length = fields.length;
        if (length == 0) {
            return null;
        }
        String[] names = new String[length];
        for (int i = 0; i < length; i += 1) {
            names[i] = fields[i].getName();
        }
        return names;
    }

    public static String numberToString(Number number) throws JSONException {
        if (number == null) {
            throw new JSONException("Null pointer");
        }
        testValidity(number);
        String string = number.toString();
        if (string.indexOf('.') > 0 && string.indexOf('e') < 0 && string.indexOf('E') < 0) {
            while (string.endsWith("0")) {
                string = string.substring(0, string.length() - 1);
            }
            if (string.endsWith(".")) {
                string = string.substring(0, string.length() - 1);
            }
        }
        return string;
    }

    static BigDecimal objectToBigDecimal(Object val, BigDecimal defaultValue) {
        return objectToBigDecimal(val, defaultValue, true);
    }

    static BigDecimal objectToBigDecimal(Object val, BigDecimal defaultValue, boolean exact) {
        if (NULL.equals(val)) {
            return defaultValue;
        }
        if (val instanceof BigDecimal) {
            return (BigDecimal) val;
        }
        if (val instanceof BigInteger) {
            return new BigDecimal((BigInteger) val);
        }
        if (val instanceof Double || val instanceof Float) {
            if (!numberIsFinite((Number) val)) {
                return defaultValue;
            }
            if (exact) {
                return BigDecimal.valueOf(((Number) val).doubleValue());
            }
            return new BigDecimal(val.toString());
        }
        if (val instanceof Long || val instanceof Integer || val instanceof Short || val instanceof Byte) {
            return new BigDecimal(((Number) val).longValue());
        }
        try {
            return new BigDecimal(val.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    static BigInteger objectToBigInteger(Object val, BigInteger defaultValue) {
        if (NULL.equals(val)) {
            return defaultValue;
        }
        if (val instanceof BigInteger) {
            return (BigInteger) val;
        }
        if (val instanceof BigDecimal) {
            return ((BigDecimal) val).toBigInteger();
        }
        if (val instanceof Double || val instanceof Float) {
            if (!numberIsFinite((Number) val)) {
                return defaultValue;
            }
            return BigDecimal.valueOf(((Number) val).doubleValue()).toBigInteger();
        }
        if (val instanceof Long || val instanceof Integer || val instanceof Short || val instanceof Byte) {
            return BigInteger.valueOf(((Number) val).longValue());
        }
        try {
            final String valStr = val.toString();
            if (isDecimalNotation(valStr)) {
                return new BigDecimal(valStr).toBigInteger();
            }
            return new BigInteger(valStr);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static boolean shouldIgnore(Field f) {
        //Ignore serialVersionUID
        if (f.getName().equals("serialVersionUID")) {
            return true;
        }

        //Check if the field is static
        return (f.getModifiers() & 0x00000008) != 0;
    }

    private static boolean isValidMethodName(String name) {
        return !"getClass".equals(name) && !"getDeclaringClass".equals(name);
    }

    private static String getKeyNameFromMethod(Method method) {
        final int ignoreDepth = getAnnotationDepth(method, JSONPropertyIgnore.class);
        if (ignoreDepth > 0) {
            final int forcedNameDepth = getAnnotationDepth(method, JSONPropertyName.class);
            if (forcedNameDepth < 0 || ignoreDepth <= forcedNameDepth) {
                return null;
            }
        }
        JSONPropertyName annotation = getAnnotation(method, JSONPropertyName.class);
        if (annotation != null && annotation.value() != null && !annotation.value().isEmpty()) {
            return annotation.value();
        }
        String key;
        final String name = method.getName();
        if (name.startsWith("get") && name.length() > 3) {
            key = name.substring(3);
        } else if (name.startsWith("is") && name.length() > 2) {
            key = name.substring(2);
        } else {
            return null;
        }
        if (Character.isLowerCase(key.charAt(0))) {
            return null;
        }
        if (key.length() == 1) {
            key = key.toLowerCase(Locale.ROOT);
        } else if (!Character.isUpperCase(key.charAt(1))) {
            key = key.substring(0, 1).toLowerCase(Locale.ROOT) + key.substring(1);
        }
        return key;
    }

    private static <A extends Annotation> A getAnnotation(final Method m, final Class<A> annotationClass) {
        if (m == null || annotationClass == null) {
            return null;
        }
        if (m.isAnnotationPresent(annotationClass)) {
            return m.getAnnotation(annotationClass);
        }
        Class<?> c = m.getDeclaringClass();
        if (c.getSuperclass() == null) {
            return null;
        }
        for (Class<?> i : c.getInterfaces()) {
            try {
                Method im = i.getMethod(m.getName(), m.getParameterTypes());
                return getAnnotation(im, annotationClass);
            } catch (final SecurityException | NoSuchMethodException ignored) {
            }
        }
        try {
            return getAnnotation(c.getSuperclass().getMethod(m.getName(), m.getParameterTypes()), annotationClass);
        } catch (final SecurityException | NoSuchMethodException ex) {
            return null;
        }
    }

    private static int getAnnotationDepth(final Method m, final Class<? extends Annotation> annotationClass) {
        if (m == null || annotationClass == null) {
            return -1;
        }
        if (m.isAnnotationPresent(annotationClass)) {
            return 1;
        }
        Class<?> c = m.getDeclaringClass();
        if (c.getSuperclass() == null) {
            return -1;
        }
        for (Class<?> i : c.getInterfaces()) {
            try {
                Method im = i.getMethod(m.getName(), m.getParameterTypes());
                int d = getAnnotationDepth(im, annotationClass);
                if (d > 0) {
                    return d + 1;
                }
            } catch (final SecurityException | NoSuchMethodException ignored) {
            }
        }
        try {
            int d = getAnnotationDepth(c.getSuperclass().getMethod(m.getName(), m.getParameterTypes()), annotationClass);
            if (d > 0) {
                return d + 1;
            }
            return -1;
        } catch (final SecurityException | NoSuchMethodException ex) {
            return -1;
        }
    }

    public static String quote(String string) {
        StringWriter sw = new StringWriter();
        synchronized (sw.getBuffer()) {
            try {
                return quote(string, sw).toString();
            } catch (IOException ignored) {
                return "";
            }
        }
    }

    public static Writer quote(String string, Writer w) throws IOException {
        if (string == null || string.isEmpty()) {
            w.write("\"\"");
            return w;
        }
        char b;
        char c = 0;
        String hhhh;
        int i;
        int len = string.length();
        w.write('"');
        for (i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    w.write('\\');
                    w.write(c);
                    break;
                case '/':
                    if (b == '<') {
                        w.write('\\');
                    }
                    w.write(c);
                    break;
                case '\b':
                    w.write("\\b");
                    break;
                case '\t':
                    w.write("\\t");
                    break;
                case '\n':
                    w.write("\\n");
                    break;
                case '\f':
                    w.write("\\f");
                    break;
                case '\r':
                    w.write("\\r");
                    break;
                default:
                    if (c < ' ' || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')) {
                        w.write("\\u");
                        hhhh = Integer.toHexString(c);
                        w.write("0000", 0, 4 - hhhh.length());
                        w.write(hhhh);
                    } else {
                        w.write(c);
                    }
            }
        }
        w.write('"');
        return w;
    }

    static boolean isNumberSimilar(Number l, Number r) {
        if (!numberIsFinite(l) || !numberIsFinite(r)) {
            return false;
        }
        if (l.getClass().equals(r.getClass()) && l instanceof Comparable) {
            @SuppressWarnings({"rawtypes", "unchecked"}) int compareTo = ((Comparable) l).compareTo(r);
            return compareTo == 0;
        }
        final BigDecimal lBigDecimal = objectToBigDecimal(l, null, false);
        final BigDecimal rBigDecimal = objectToBigDecimal(r, null, false);
        if (lBigDecimal == null || rBigDecimal == null) {
            return false;
        }
        return lBigDecimal.compareTo(rBigDecimal) == 0;
    }

    private static boolean numberIsFinite(Number n) {
        if (n instanceof Double && (((Double) n).isInfinite() || ((Double) n).isNaN())) {
            return false;
        } else return !(n instanceof Float) || (!((Float) n).isInfinite() && !((Float) n).isNaN());
    }

    protected static boolean isDecimalNotation(final String val) {
        return val.indexOf('.') > -1 || val.indexOf('e') > -1 || val.indexOf('E') > -1 || "-0".equals(val);
    }

    protected static Number stringToNumber(final String val) throws NumberFormatException {
        char initial = val.charAt(0);
        if ((initial >= '0' && initial <= '9') || initial == '-') {
            if (isDecimalNotation(val)) {
                try {
                    BigDecimal bd = new BigDecimal(val);
                    if (initial == '-' && BigDecimal.ZERO.compareTo(bd) == 0) {
                        return -0.0d;
                    }
                    return bd;
                } catch (NumberFormatException retryAsDouble) {
                    try {
                        Double d = Double.valueOf(val);
                        if (d.isNaN() || d.isInfinite()) {
                            throw new NumberFormatException("val [" + val + "] is not a valid number.");
                        }
                        return d;
                    } catch (NumberFormatException ignore) {
                        throw new NumberFormatException("val [" + val + "] is not a valid number.");
                    }
                }
            }
            if (initial == '0' && val.length() > 1) {
                char at1 = val.charAt(1);
                if (at1 >= '0' && at1 <= '9') {
                    throw new NumberFormatException("val [" + val + "] is not a valid number.");
                }
            } else if (initial == '-' && val.length() > 2) {
                char at1 = val.charAt(1);
                char at2 = val.charAt(2);
                if (at1 == '0' && at2 >= '0' && at2 <= '9') {
                    throw new NumberFormatException("val [" + val + "] is not a valid number.");
                }
            }
            BigInteger bi = new BigInteger(val);
            if (bi.bitLength() <= 31) {
                return bi.intValue();
            }
            if (bi.bitLength() <= 63) {
                return bi.longValue();
            }
            return bi;
        }
        throw new NumberFormatException("val [" + val + "] is not a valid number.");
    }

    public static Object stringToValue(String string) {
        if ("".equals(string)) {
            return string;
        }
        if ("true".equalsIgnoreCase(string)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(string)) {
            return Boolean.FALSE;
        }
        if ("null".equalsIgnoreCase(string)) {
            return NanoJSON.NULL;
        }
        char initial = string.charAt(0);
        if ((initial >= '0' && initial <= '9') || initial == '-') {
            try {
                return stringToNumber(string);
            } catch (Exception ignore) {
            }
        }
        return string;
    }

    public static void testValidity(Object o) throws JSONException {
        if (o instanceof Number && !numberIsFinite((Number) o)) {
            throw new JSONException("JSON does not allow non-finite numbers.");
        }
    }

    public static String valueToString(Object value) throws JSONException {
        return JSONWriter.valueToString(value);
    }

    public static Object wrap(Object object) {
        return wrap(object, null);
    }

    private static Object wrap(Object object, Set<Object> objectsRecord) {
        try {
            if (NULL.equals(object)) {
                return NULL;
            }
            if (object instanceof NanoJSON || object instanceof JSONArray || object instanceof JSONString || object instanceof Byte || object instanceof Character || object instanceof Short || object instanceof Integer || object instanceof Long || object instanceof Boolean || object instanceof Float || object instanceof Double || object instanceof String || object instanceof BigInteger || object instanceof BigDecimal || object instanceof Enum) {
                return object;
            }
            if (object instanceof Collection) {
                Collection<?> coll = (Collection<?>) object;
                return new JSONArray(coll);
            }
            if (object.getClass().isArray()) {
                return new JSONArray(object);
            }
            if (object instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) object;
                return new NanoJSON(map);
            }
            Package objectPackage = object.getClass().getPackage();
            String objectPackageName = objectPackage != null ? objectPackage.getName() : "";
            if (objectPackageName.startsWith("java.") || objectPackageName.startsWith("javax.") || object.getClass().getClassLoader() == null) {
                return object.toString();
            }
            if (objectsRecord != null) {
                return new NanoJSON(object, objectsRecord);
            }
            return new NanoJSON(object);
        } catch (JSONException exception) {
            throw exception;
        } catch (Exception exception) {
            return null;
        }
    }

    static Writer writeValue(Writer writer, Object value, int indentFactor, int indent) throws JSONException, IOException {
        if (value == null) {
            writer.write("null");
        } else if (value instanceof JSONString) {
            Object o;
            try {
                o = ((JSONString) value).toJSONString();
            } catch (Exception e) {
                throw new JSONException(e);
            }
            writer.write(o != null ? o.toString() : quote(value.toString()));
        } else if (value instanceof Number) {
            final String numberAsString = numberToString((Number) value);
            if (NUMBER_PATTERN.matcher(numberAsString).matches()) {
                writer.write(numberAsString);
            } else {
                quote(numberAsString, writer);
            }
        } else if (value instanceof Boolean) {
            writer.write(value.toString());
        } else if (value instanceof Enum<?>) {
            writer.write(quote(((Enum<?>) value).name()));
        } else if (value instanceof NanoJSON) {
            ((NanoJSON) value).write(writer, indentFactor, indent);
        } else if (value instanceof JSONArray) {
            ((JSONArray) value).write(writer, indentFactor, indent);
        } else if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            new NanoJSON(map).write(writer, indentFactor, indent);
        } else if (value instanceof Collection) {
            Collection<?> coll = (Collection<?>) value;
            new JSONArray(coll).write(writer, indentFactor, indent);
        } else if (value.getClass().isArray()) {
            new JSONArray(value).write(writer, indentFactor, indent);
        } else {
            quote(value.toString(), writer);
        }
        return writer;
    }

    static void indent(Writer writer, int indent) throws IOException {
        for (int i = 0; i < indent; i += 1) {
            writer.write(' ');
        }
    }

    private static JSONException wrongValueFormatException(String key, String valueType, Object value, Throwable cause) {
        if (value == null) {
            return new JSONException("JSONObject[" + quote(key) + "] is not a " + valueType + " (null).", cause);
        }
        if (value instanceof Map || value instanceof Iterable || value instanceof NanoJSON) {
            return new JSONException("JSONObject[" + quote(key) + "] is not a " + valueType + " (" + value.getClass() + ").", cause);
        }
        return new JSONException("JSONObject[" + quote(key) + "] is not a " + valueType + " (" + value.getClass() + " : " + value + ").", cause);
    }

    private static JSONException recursivelyDefinedObjectException(String key) {
        return new JSONException("JavaBean object contains recursively defined member variable of key " + quote(key));
    }

    @SuppressWarnings("rawtypes")
    public Class<? extends Map> getMapType() {
        return map.getClass();
    }

    public NanoJSON accumulate(String key, Object value) throws JSONException {
        testValidity(value);
        Object object = this.opt(key);
        if (object == null) {
            this.put(key, value instanceof JSONArray ? new JSONArray().put(value) : value);
        } else if (object instanceof JSONArray) {
            ((JSONArray) object).put(value);
        } else {
            this.put(key, new JSONArray().put(object).put(value));
        }
        return this;
    }

    public NanoJSON append(String key, Object value) throws JSONException {
        testValidity(value);
        Object object = this.opt(key);
        if (object == null) {
            this.put(key, new JSONArray().put(value));
        } else if (object instanceof JSONArray) {
            this.put(key, ((JSONArray) object).put(value));
        } else {
            throw wrongValueFormatException(key, "JSONArray", null, null);
        }
        return this;
    }

    public Object get(String key) throws JSONException {
        if (key == null) {
            throw new JSONException("Null key.");
        }
        Object object = this.opt(key);
        if (object == null) {
            throw new JSONException("JSONObject[" + quote(key) + "] not found.");
        }
        return object;
    }

    public <E extends Enum<E>> E getEnum(Class<E> clazz, String key) throws JSONException {
        E val = optEnum(clazz, key);
        if (val == null) {
            throw wrongValueFormatException(key, "enum of type " + quote(clazz.getSimpleName()), opt(key), null);
        }
        return val;
    }

    public boolean getBoolean(String key) throws JSONException {
        Object object = this.get(key);
        if (object.equals(Boolean.FALSE) || (object instanceof String && ((String) object).equalsIgnoreCase("false"))) {
            return false;
        } else if (object.equals(Boolean.TRUE) || (object instanceof String && ((String) object).equalsIgnoreCase("true"))) {
            return true;
        }
        throw wrongValueFormatException(key, "Boolean", object, null);
    }

    public BigInteger getBigInteger(String key) throws JSONException {
        Object object = this.get(key);
        BigInteger ret = objectToBigInteger(object, null);
        if (ret != null) {
            return ret;
        }
        throw wrongValueFormatException(key, "BigInteger", object, null);
    }

    public BigDecimal getBigDecimal(String key) throws JSONException {
        Object object = this.get(key);
        BigDecimal ret = objectToBigDecimal(object, null);
        if (ret != null) {
            return ret;
        }
        throw wrongValueFormatException(key, "BigDecimal", object, null);
    }

    public double getDouble(String key) throws JSONException {
        final Object object = this.get(key);
        if (object instanceof Number) {
            return ((Number) object).doubleValue();
        }
        try {
            return Double.parseDouble(object.toString());
        } catch (Exception e) {
            throw wrongValueFormatException(key, "double", object, e);
        }
    }

    public float getFloat(String key) throws JSONException {
        final Object object = this.get(key);
        if (object instanceof Number) {
            return ((Number) object).floatValue();
        }
        try {
            return Float.parseFloat(object.toString());
        } catch (Exception e) {
            throw wrongValueFormatException(key, "float", object, e);
        }
    }

    public Number getNumber(String key) throws JSONException {
        Object object = this.get(key);
        try {
            if (object instanceof Number) {
                return (Number) object;
            }
            return stringToNumber(object.toString());
        } catch (Exception e) {
            throw wrongValueFormatException(key, "number", object, e);
        }
    }

    public int getInt(String key) throws JSONException {
        final Object object = this.get(key);
        if (object instanceof Number) {
            return ((Number) object).intValue();
        }
        try {
            return Integer.parseInt(object.toString());
        } catch (Exception e) {
            throw wrongValueFormatException(key, "int", object, e);
        }
    }

    public JSONArray getJSONArray(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof JSONArray) {
            return (JSONArray) object;
        }
        throw wrongValueFormatException(key, "JSONArray", object, null);
    }

    public NanoJSON getJSONObject(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof NanoJSON) {
            return (NanoJSON) object;
        }
        throw wrongValueFormatException(key, "JSONObject", object, null);
    }

    public long getLong(String key) throws JSONException {
        final Object object = this.get(key);
        if (object instanceof Number) {
            return ((Number) object).longValue();
        }
        try {
            return Long.parseLong(object.toString());
        } catch (Exception e) {
            throw wrongValueFormatException(key, "long", object, e);
        }
    }

    public String getString(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof String) {
            return (String) object;
        }
        throw wrongValueFormatException(key, "string", object, null);
    }

    public boolean has(String key) {
        return this.map.containsKey(key);
    }

    public NanoJSON increment(String key) throws JSONException {
        Object value = this.opt(key);
        if (value == null) {
            this.put(key, 1);
        } else if (value instanceof Integer) {
            this.put(key, (Integer) value + 1);
        } else if (value instanceof Long) {
            this.put(key, (Long) value + 1L);
        } else if (value instanceof BigInteger) {
            this.put(key, ((BigInteger) value).add(BigInteger.ONE));
        } else if (value instanceof Float) {
            this.put(key, (Float) value + 1.0f);
        } else if (value instanceof Double) {
            this.put(key, (Double) value + 1.0d);
        } else if (value instanceof BigDecimal) {
            this.put(key, ((BigDecimal) value).add(BigDecimal.ONE));
        } else {
            throw new JSONException("Unable to increment [" + quote(key) + "].");
        }
        return this;
    }

    public boolean isNull(String key) {
        return NanoJSON.NULL.equals(this.opt(key));
    }

    public Iterator<String> keys() {
        return this.keySet().iterator();
    }

    public Set<String> keySet() {
        return this.map.keySet();
    }

    protected Set<Entry<String, Object>> entrySet() {
        return this.map.entrySet();
    }

    public int length() {
        return this.map.size();
    }

    public void clear() {
        this.map.clear();
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public JSONArray names() {
        if (this.map.isEmpty()) {
            return null;
        }
        return new JSONArray(this.map.keySet());
    }

    public Object opt(String key) {
        return key == null ? null : this.map.get(key);
    }

    public <E extends Enum<E>> E optEnum(Class<E> clazz, String key) {
        return this.optEnum(clazz, key, null);
    }

    public <E extends Enum<E>> E optEnum(Class<E> clazz, String key, E defaultValue) {
        try {
            Object val = this.opt(key);
            if (NULL.equals(val)) {
                return defaultValue;
            }
            if (clazz.isAssignableFrom(val.getClass())) {
                @SuppressWarnings("unchecked") E myE = (E) val;
                return myE;
            }
            return Enum.valueOf(clazz, val.toString());
        } catch (IllegalArgumentException | NullPointerException e) {
            return defaultValue;
        }
    }

    public boolean optBoolean(String key) {
        return this.optBoolean(key, false);
    }

    public boolean optBoolean(String key, boolean defaultValue) {
        Object val = this.opt(key);
        if (NULL.equals(val)) {
            return defaultValue;
        }
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        try {
            return this.getBoolean(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public BigDecimal optBigDecimal(String key, BigDecimal defaultValue) {
        Object val = this.opt(key);
        return objectToBigDecimal(val, defaultValue);
    }

    public BigInteger optBigInteger(String key, BigInteger defaultValue) {
        Object val = this.opt(key);
        return objectToBigInteger(val, defaultValue);
    }

    public double optDouble(String key) {
        return this.optDouble(key, Double.NaN);
    }

    public double optDouble(String key, double defaultValue) {
        Number val = this.optNumber(key);
        if (val == null) {
            return defaultValue;
        }
        return val.doubleValue();
    }

    public float optFloat(String key) {
        return this.optFloat(key, Float.NaN);
    }

    public float optFloat(String key, float defaultValue) {
        Number val = this.optNumber(key);
        if (val == null) {
            return defaultValue;
        }
        return val.floatValue();
    }

    public int optInt(String key) {
        return this.optInt(key, 0);
    }

    public int optInt(String key, int defaultValue) {
        final Number val = this.optNumber(key, null);
        if (val == null) {
            return defaultValue;
        }
        return val.intValue();
    }

    public JSONArray optJSONArray(String key) {
        Object o = this.opt(key);
        return o instanceof JSONArray ? (JSONArray) o : null;
    }

    public NanoJSON optJSONObject(String key) {
        return this.optJSONObject(key, null);
    }

    public NanoJSON optJSONObject(String key, NanoJSON defaultValue) {
        Object object = this.opt(key);
        return object instanceof NanoJSON ? (NanoJSON) object : defaultValue;
    }

    public long optLong(String key) {
        return this.optLong(key, 0);
    }

    public long optLong(String key, long defaultValue) {
        final Number val = this.optNumber(key, null);
        if (val == null) {
            return defaultValue;
        }
        return val.longValue();
    }

    public Number optNumber(String key) {
        return this.optNumber(key, null);
    }

    public Number optNumber(String key, Number defaultValue) {
        Object val = this.opt(key);
        if (NULL.equals(val)) {
            return defaultValue;
        }
        if (val instanceof Number) {
            return (Number) val;
        }
        try {
            return stringToNumber(val.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public String optString(String key) {
        return this.optString(key, "");
    }

    public String optString(String key, String defaultValue) {
        Object object = this.opt(key);
        return NULL.equals(object) ? defaultValue : object.toString();
    }

    private void populateMap(Object dto) {
        Class<?> clazz = dto.getClass();
        try {
            Field[] allFields = clazz.getDeclaredFields();
            for (Field field : allFields) {
                if (shouldIgnore(field)) {
                    continue;
                }
                field.setAccessible(true);
                Object value = field.get(dto);
                if (value != null) {
                    this.put(field.getName(), value);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void populateMap(Object bean, Set<Object> objectsRecord) {
        if (objectsRecord == null) {
            objectsRecord = Collections.newSetFromMap(new IdentityHashMap<>());
        }
        Class<?> klass = bean.getClass();
        boolean includeSuperClass = klass.getClassLoader() != null;
        Method[] methods = includeSuperClass ? klass.getMethods() : klass.getDeclaredMethods();
        for (final Method method : methods) {
            final int modifiers = method.getModifiers();
            if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers) && method.getParameterTypes().length == 0 && !method.isBridge() && method.getReturnType() != Void.TYPE && isValidMethodName(method.getName())) {
                final String key = getKeyNameFromMethod(method);
                if (key != null && !key.isEmpty()) {
                    try {
                        final Object result = method.invoke(bean);
                        if (result != null) {
                            if (objectsRecord.contains(result)) {
                                throw recursivelyDefinedObjectException(key);
                            }
                            objectsRecord.add(result);
                            this.map.put(key, wrap(result, objectsRecord));
                            objectsRecord.remove(result);
                            if (result instanceof Closeable) {
                                try {
                                    ((Closeable) result).close();
                                } catch (IOException ignore) {
                                }
                            }
                        }
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ignore) {
                    }
                }
            }
        }
    }

    public NanoJSON put(String key, boolean value) throws JSONException {
        return this.put(key, value ? Boolean.TRUE : Boolean.FALSE);
    }

    public NanoJSON put(String key, Collection<?> value) throws JSONException {
        return this.put(key, new JSONArray(value));
    }

    public NanoJSON put(String key, double value) throws JSONException {
        return this.put(key, Double.valueOf(value));
    }

    public NanoJSON put(String key, float value) throws JSONException {
        return this.put(key, Float.valueOf(value));
    }

    public NanoJSON put(String key, int value) throws JSONException {
        return this.put(key, Integer.valueOf(value));
    }

    public NanoJSON put(String key, long value) throws JSONException {
        return this.put(key, Long.valueOf(value));
    }

    public NanoJSON put(String key, Map<?, ?> value) throws JSONException {
        return this.put(key, new NanoJSON(value));
    }

    public NanoJSON put(String key, Object value) throws JSONException {
        if (key == null) {
            throw new NullPointerException("Null key.");
        }
        if (value != null) {
            testValidity(value);
            this.map.put(key, value);
        } else {
            this.remove(key);
        }
        return this;
    }

    public NanoJSON putOnce(String key, Object value) throws JSONException {
        if (key != null && value != null) {
            if (this.opt(key) != null) {
                throw new JSONException("Duplicate key \"" + key + "\"");
            }
            return this.put(key, value);
        }
        return this;
    }

    public NanoJSON putOpt(String key, Object value) throws JSONException {
        if (key != null && value != null) {
            return this.put(key, value);
        }
        return this;
    }

    public Object query(String jsonPointer) {
        return query(new JSONPointer(jsonPointer));
    }

    public Object query(JSONPointer jsonPointer) {
        return jsonPointer.queryFrom(this);
    }

    public Object optQuery(String jsonPointer) {
        return optQuery(new JSONPointer(jsonPointer));
    }

    public Object optQuery(JSONPointer jsonPointer) {
        try {
            return jsonPointer.queryFrom(this);
        } catch (JSONPointerException e) {
            return null;
        }
    }

    public Object remove(String key) {
        return this.map.remove(key);
    }

    public boolean similar(Object other) {
        try {
            if (!(other instanceof NanoJSON)) {
                return false;
            }
            if (!this.keySet().equals(((NanoJSON) other).keySet())) {
                return false;
            }
            for (final Entry<String, ?> entry : this.entrySet()) {
                String name = entry.getKey();
                Object valueThis = entry.getValue();
                Object valueOther = ((NanoJSON) other).get(name);
                if (valueThis == valueOther) {
                    continue;
                }
                if (valueThis == null) {
                    return false;
                }
                if (valueThis instanceof NanoJSON) {
                    if (!((NanoJSON) valueThis).similar(valueOther)) {
                        return false;
                    }
                } else if (valueThis instanceof JSONArray) {
                    if (!((JSONArray) valueThis).similar(valueOther)) {
                        return false;
                    }
                } else if (valueThis instanceof Number && valueOther instanceof Number) {
                    if (!isNumberSimilar((Number) valueThis, (Number) valueOther)) {
                        return false;
                    }
                } else if (valueThis instanceof JSONString && valueOther instanceof JSONString) {
                    if (!((JSONString) valueThis).toJSONString().equals(((JSONString) valueOther).toJSONString())) {
                        return false;
                    }
                } else if (!valueThis.equals(valueOther)) {
                    return false;
                }
            }
            return true;
        } catch (Throwable exception) {
            return false;
        }
    }

    public JSONArray toJSONArray(JSONArray names) throws JSONException {
        if (names == null || names.isEmpty()) {
            return null;
        }
        JSONArray ja = new JSONArray();
        for (int i = 0; i < names.length(); i += 1) {
            ja.put(this.opt(names.getString(i)));
        }
        return ja;
    }

    @Override
    public String toString() {
        try {
            return this.toString(0);
        } catch (Exception e) {
            return null;
        }
    }

    public String toString(int indentFactor) throws JSONException {
        StringWriter w = new StringWriter();
        synchronized (w.getBuffer()) {
            return this.write(w, indentFactor, 0).toString();
        }
    }

    public Writer write(Writer writer) throws JSONException {
        return this.write(writer, 0, 0);
    }

    public Writer write(Writer writer, int indentFactor, int indent) throws JSONException {
        try {
            boolean needsComma = false;
            final int length = this.length();
            writer.write('{');
            if (length == 1) {
                final Entry<String, ?> entry = this.entrySet().iterator().next();
                final String key = entry.getKey();
                writer.write(quote(key));
                writer.write(':');
                if (indentFactor > 0) {
                    writer.write(' ');
                }
                try {
                    writeValue(writer, entry.getValue(), indentFactor, indent);
                } catch (Exception e) {
                    throw new JSONException("Unable to write JSONObject value for key: " + key, e);
                }
            } else if (length != 0) {
                final int newIndent = indent + indentFactor;
                for (final Entry<String, ?> entry : this.entrySet()) {
                    if (needsComma) {
                        writer.write(',');
                    }
                    if (indentFactor > 0) {
                        writer.write('\n');
                    }
                    indent(writer, newIndent);
                    final String key = entry.getKey();
                    writer.write(quote(key));
                    writer.write(':');
                    if (indentFactor > 0) {
                        writer.write(' ');
                    }
                    try {
                        writeValue(writer, entry.getValue(), indentFactor, newIndent);
                    } catch (Exception e) {
                        throw new JSONException("Unable to write JSONObject value for key: " + key, e);
                    }
                    needsComma = true;
                }
                if (indentFactor > 0) {
                    writer.write('\n');
                }
                indent(writer, indent);
            }
            writer.write('}');
            return writer;
        } catch (IOException exception) {
            throw new JSONException(exception);
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> results = new HashMap<>();
        for (Entry<String, Object> entry : this.entrySet()) {
            Object value;
            if (entry.getValue() == null || NULL.equals(entry.getValue())) {
                value = null;
            } else if (entry.getValue() instanceof NanoJSON) {
                value = ((NanoJSON) entry.getValue()).toMap();
            } else if (entry.getValue() instanceof JSONArray) {
                value = ((JSONArray) entry.getValue()).toList();
            } else {
                value = entry.getValue();
            }
            results.put(entry.getKey(), value);
        }
        return results;
    }

    @SuppressWarnings("ALL")
    private static final class Null {
        @Override
        protected final Object clone() {
            return this;
        }

        @Override
        public boolean equals(Object object) {
            return object == null || object == this;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            return "null";
        }
    }
}
