package nano.http.d2.json;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@SuppressWarnings("unused")
public class JSONArray implements Iterable<Object> {
    private final ArrayList<Object> myArrayList;

    public JSONArray() {
        this.myArrayList = new ArrayList<>();
    }

    public JSONArray(JSONTokener x) throws JSONException {
        this();
        if (x.nextClean() != '[') {
            throw x.syntaxError("A JSONArray text must start with '['");
        }
        char nextChar = x.nextClean();
        if (nextChar == 0) {
            throw x.syntaxError("Expected a ',' or ']'");
        }
        if (nextChar != ']') {
            x.back();
            for (; ; ) {
                if (x.nextClean() == ',') {
                    x.back();
                    this.myArrayList.add(NanoJSON.NULL);
                } else {
                    x.back();
                    this.myArrayList.add(x.nextValue());
                }
                switch (x.nextClean()) {
                    case ',':
                        nextChar = x.nextClean();
                        if (nextChar == 0) {
                            throw x.syntaxError("Expected a ',' or ']'");
                        }
                        if (nextChar == ']') {
                            return;
                        }
                        x.back();
                        break;
                    case ']':
                        return;
                    case 0:
                    default:
                        throw x.syntaxError("Expected a ',' or ']'");
                }
            }
        }
    }

    public JSONArray(String source) throws JSONException {
        this(new JSONTokener(source));
    }

    public JSONArray(Collection<?> collection) {
        if (collection == null) {
            this.myArrayList = new ArrayList<>();
        } else {
            this.myArrayList = new ArrayList<>(collection.size());
            this.addAll(collection, true);
        }
    }

    public JSONArray(Iterable<?> iter) {
        this();
        if (iter == null) {
            return;
        }
        this.addAll(iter, true);
    }

    public JSONArray(JSONArray array) {
        if (array == null) {
            this.myArrayList = new ArrayList<>();
        } else {
            this.myArrayList = new ArrayList<>(array.myArrayList);
        }
    }

    public JSONArray(Object array) throws JSONException {
        this();
        if (!array.getClass().isArray()) {
            throw new JSONException("JSONArray initial value should be a string or collection or array.");
        }
        this.addAll(array, true);
    }

    public JSONArray(int initialCapacity) throws JSONException {
        if (initialCapacity < 0) {
            throw new JSONException("JSONArray initial capacity cannot be negative.");
        }
        this.myArrayList = new ArrayList<>(initialCapacity);
    }

    private static JSONException wrongValueFormatException(int idx, String valueType, Object value, Throwable cause) {
        if (value == null) {
            return new JSONException("JSONArray[" + idx + "] is not a " + valueType + " (null).", cause);
        }
        if (value instanceof Map || value instanceof Iterable || value instanceof NanoJSON) {
            return new JSONException("JSONArray[" + idx + "] is not a " + valueType + " (" + value.getClass() + ").", cause);
        }
        return new JSONException("JSONArray[" + idx + "] is not a " + valueType + " (" + value.getClass() + " : " + value + ").", cause);
    }

    @Override
    public Iterator<Object> iterator() {
        return this.myArrayList.iterator();
    }

    public Object get(int index) throws JSONException {
        Object object = this.opt(index);
        if (object == null) {
            throw new JSONException("JSONArray[" + index + "] not found.");
        }
        return object;
    }

    public boolean getBoolean(int index) throws JSONException {
        Object object = this.get(index);
        if (object.equals(Boolean.FALSE) || (object instanceof String && ((String) object).equalsIgnoreCase("false"))) {
            return false;
        } else if (object.equals(Boolean.TRUE) || (object instanceof String && ((String) object).equalsIgnoreCase("true"))) {
            return true;
        }
        throw wrongValueFormatException(index, "boolean", object, null);
    }

    public double getDouble(int index) throws JSONException {
        final Object object = this.get(index);
        if (object instanceof Number) {
            return ((Number) object).doubleValue();
        }
        try {
            return Double.parseDouble(object.toString());
        } catch (Exception e) {
            throw wrongValueFormatException(index, "double", object, e);
        }
    }

    public float getFloat(int index) throws JSONException {
        final Object object = this.get(index);
        if (object instanceof Number) {
            return ((Number) object).floatValue();
        }
        try {
            return Float.parseFloat(object.toString());
        } catch (Exception e) {
            throw wrongValueFormatException(index, "float", object, e);
        }
    }

    public Number getNumber(int index) throws JSONException {
        Object object = this.get(index);
        try {
            if (object instanceof Number) {
                return (Number) object;
            }
            return NanoJSON.stringToNumber(object.toString());
        } catch (Exception e) {
            throw wrongValueFormatException(index, "number", object, e);
        }
    }

    public <E extends Enum<E>> E getEnum(Class<E> clazz, int index) throws JSONException {
        E val = optEnum(clazz, index);
        if (val == null) {
            throw wrongValueFormatException(index, "enum of type " + NanoJSON.quote(clazz.getSimpleName()), opt(index), null);
        }
        return val;
    }

    public BigDecimal getBigDecimal(int index) throws JSONException {
        Object object = this.get(index);
        BigDecimal val = NanoJSON.objectToBigDecimal(object, null);
        if (val == null) {
            throw wrongValueFormatException(index, "BigDecimal", object, null);
        }
        return val;
    }

    public BigInteger getBigInteger(int index) throws JSONException {
        Object object = this.get(index);
        BigInteger val = NanoJSON.objectToBigInteger(object, null);
        if (val == null) {
            throw wrongValueFormatException(index, "BigInteger", object, null);
        }
        return val;
    }

    public int getInt(int index) throws JSONException {
        final Object object = this.get(index);
        if (object instanceof Number) {
            return ((Number) object).intValue();
        }
        try {
            return Integer.parseInt(object.toString());
        } catch (Exception e) {
            throw wrongValueFormatException(index, "int", object, e);
        }
    }

    public JSONArray getJSONArray(int index) throws JSONException {
        Object object = this.get(index);
        if (object instanceof JSONArray) {
            return (JSONArray) object;
        }
        throw wrongValueFormatException(index, "JSONArray", object, null);
    }

    public NanoJSON getJSONObject(int index) throws JSONException {
        Object object = this.get(index);
        if (object instanceof NanoJSON) {
            return (NanoJSON) object;
        }
        throw wrongValueFormatException(index, "JSONObject", object, null);
    }

    public long getLong(int index) throws JSONException {
        final Object object = this.get(index);
        if (object instanceof Number) {
            return ((Number) object).longValue();
        }
        try {
            return Long.parseLong(object.toString());
        } catch (Exception e) {
            throw wrongValueFormatException(index, "long", object, e);
        }
    }

    public String getString(int index) throws JSONException {
        Object object = this.get(index);
        if (object instanceof String) {
            return (String) object;
        }
        throw wrongValueFormatException(index, "String", object, null);
    }

    public boolean isNull(int index) {
        return NanoJSON.NULL.equals(this.opt(index));
    }

    public String join(String separator) throws JSONException {
        int len = this.length();
        if (len == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(NanoJSON.valueToString(this.myArrayList.get(0)));
        for (int i = 1; i < len; i++) {
            sb.append(separator).append(NanoJSON.valueToString(this.myArrayList.get(i)));
        }
        return sb.toString();
    }

    public int length() {
        return this.myArrayList.size();
    }

    public void clear() {
        this.myArrayList.clear();
    }

    public Object opt(int index) {
        return (index < 0 || index >= this.length()) ? null : this.myArrayList.get(index);
    }

    public boolean optBoolean(int index) {
        return this.optBoolean(index, false);
    }

    public boolean optBoolean(int index, boolean defaultValue) {
        try {
            return this.getBoolean(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public double optDouble(int index) {
        return this.optDouble(index, Double.NaN);
    }

    public double optDouble(int index, double defaultValue) {
        final Number val = this.optNumber(index, null);
        if (val == null) {
            return defaultValue;
        }
        return val.doubleValue();
    }

    public float optFloat(int index) {
        return this.optFloat(index, Float.NaN);
    }

    public float optFloat(int index, float defaultValue) {
        final Number val = this.optNumber(index, null);
        if (val == null) {
            return defaultValue;
        }
        return val.floatValue();
    }

    public int optInt(int index) {
        return this.optInt(index, 0);
    }

    public int optInt(int index, int defaultValue) {
        final Number val = this.optNumber(index, null);
        if (val == null) {
            return defaultValue;
        }
        return val.intValue();
    }

    public <E extends Enum<E>> E optEnum(Class<E> clazz, int index) {
        return this.optEnum(clazz, index, null);
    }

    public <E extends Enum<E>> E optEnum(Class<E> clazz, int index, E defaultValue) {
        try {
            Object val = this.opt(index);
            if (NanoJSON.NULL.equals(val)) {
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

    public BigInteger optBigInteger(int index, BigInteger defaultValue) {
        Object val = this.opt(index);
        return NanoJSON.objectToBigInteger(val, defaultValue);
    }

    public BigDecimal optBigDecimal(int index, BigDecimal defaultValue) {
        Object val = this.opt(index);
        return NanoJSON.objectToBigDecimal(val, defaultValue);
    }

    public JSONArray optJSONArray(int index) {
        Object o = this.opt(index);
        return o instanceof JSONArray ? (JSONArray) o : null;
    }

    public NanoJSON optJSONObject(int index) {
        Object o = this.opt(index);
        return o instanceof NanoJSON ? (NanoJSON) o : null;
    }

    public long optLong(int index) {
        return this.optLong(index, 0);
    }

    public long optLong(int index, long defaultValue) {
        final Number val = this.optNumber(index, null);
        if (val == null) {
            return defaultValue;
        }
        return val.longValue();
    }

    public Number optNumber(int index) {
        return this.optNumber(index, null);
    }

    public Number optNumber(int index, Number defaultValue) {
        Object val = this.opt(index);
        if (NanoJSON.NULL.equals(val)) {
            return defaultValue;
        }
        if (val instanceof Number) {
            return (Number) val;
        }
        if (val instanceof String) {
            try {
                return NanoJSON.stringToNumber((String) val);
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public String optString(int index) {
        return this.optString(index, "");
    }

    public String optString(int index, String defaultValue) {
        Object object = this.opt(index);
        return NanoJSON.NULL.equals(object) ? defaultValue : object.toString();
    }

    public JSONArray put(boolean value) {
        return this.put(value ? Boolean.TRUE : Boolean.FALSE);
    }

    public JSONArray put(Collection<?> value) {
        return this.put(new JSONArray(value));
    }

    public JSONArray put(double value) throws JSONException {
        return this.put(Double.valueOf(value));
    }

    public JSONArray put(float value) throws JSONException {
        return this.put(Float.valueOf(value));
    }

    public JSONArray put(int value) {
        return this.put(Integer.valueOf(value));
    }

    public JSONArray put(long value) {
        return this.put(Long.valueOf(value));
    }

    public JSONArray put(Map<?, ?> value) {
        return this.put(new NanoJSON(value));
    }

    public JSONArray put(Object value) {
        NanoJSON.testValidity(value);
        this.myArrayList.add(value);
        return this;
    }

    public JSONArray put(int index, boolean value) throws JSONException {
        return this.put(index, value ? Boolean.TRUE : Boolean.FALSE);
    }

    public JSONArray put(int index, Collection<?> value) throws JSONException {
        return this.put(index, new JSONArray(value));
    }

    public JSONArray put(int index, double value) throws JSONException {
        return this.put(index, Double.valueOf(value));
    }

    public JSONArray put(int index, float value) throws JSONException {
        return this.put(index, Float.valueOf(value));
    }

    public JSONArray put(int index, int value) throws JSONException {
        return this.put(index, Integer.valueOf(value));
    }

    public JSONArray put(int index, long value) throws JSONException {
        return this.put(index, Long.valueOf(value));
    }

    public JSONArray put(int index, Map<?, ?> value) throws JSONException {
        this.put(index, new NanoJSON(value));
        return this;
    }

    public JSONArray put(int index, Object value) throws JSONException {
        if (index < 0) {
            throw new JSONException("JSONArray[" + index + "] not found.");
        }
        if (index < this.length()) {
            NanoJSON.testValidity(value);
            this.myArrayList.set(index, value);
            return this;
        }
        if (index == this.length()) {
            return this.put(value);
        }
        this.myArrayList.ensureCapacity(index + 1);
        while (index != this.length()) {
            this.myArrayList.add(NanoJSON.NULL);
        }
        return this.put(value);
    }

    public JSONArray putAll(Collection<?> collection) {
        this.addAll(collection, false);
        return this;
    }

    public JSONArray putAll(Iterable<?> iter) {
        this.addAll(iter, false);
        return this;
    }

    public JSONArray putAll(JSONArray array) {
        this.myArrayList.addAll(array.myArrayList);
        return this;
    }

    public JSONArray putAll(Object array) throws JSONException {
        this.addAll(array, false);
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

    public Object remove(int index) {
        return index >= 0 && index < this.length() ? this.myArrayList.remove(index) : null;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean similar(Object other) {
        if (!(other instanceof JSONArray)) {
            return false;
        }
        int len = this.length();
        if (len != ((JSONArray) other).length()) {
            return false;
        }
        for (int i = 0; i < len; i += 1) {
            Object valueThis = this.myArrayList.get(i);
            Object valueOther = ((JSONArray) other).myArrayList.get(i);
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
                if (!NanoJSON.isNumberSimilar((Number) valueThis, (Number) valueOther)) {
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
    }

    public NanoJSON toJSONObject(JSONArray names) throws JSONException {
        if (names == null || names.isEmpty() || this.isEmpty()) {
            return null;
        }
        NanoJSON jo = new NanoJSON(names.length());
        for (int i = 0; i < names.length(); i += 1) {
            jo.put(names.getString(i), this.opt(i));
        }
        return jo;
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
        StringWriter sw = new StringWriter();
        synchronized (sw.getBuffer()) {
            return this.write(sw, indentFactor, 0).toString();
        }
    }

    public Writer write(Writer writer) throws JSONException {
        return this.write(writer, 0, 0);
    }

    public Writer write(Writer writer, int indentFactor, int indent) throws JSONException {
        try {
            boolean needsComma = false;
            int length = this.length();
            writer.write('[');
            if (length == 1) {
                try {
                    NanoJSON.writeValue(writer, this.myArrayList.get(0), indentFactor, indent);
                } catch (Exception e) {
                    throw new JSONException("Unable to write JSONArray value at index: 0", e);
                }
            } else if (length != 0) {
                final int newIndent = indent + indentFactor;
                for (int i = 0; i < length; i += 1) {
                    if (needsComma) {
                        writer.write(',');
                    }
                    if (indentFactor > 0) {
                        writer.write('\n');
                    }
                    NanoJSON.indent(writer, newIndent);
                    try {
                        NanoJSON.writeValue(writer, this.myArrayList.get(i), indentFactor, newIndent);
                    } catch (Exception e) {
                        throw new JSONException("Unable to write JSONArray value at index: " + i, e);
                    }
                    needsComma = true;
                }
                if (indentFactor > 0) {
                    writer.write('\n');
                }
                NanoJSON.indent(writer, indent);
            }
            writer.write(']');
            return writer;
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    public List<Object> toList() {
        List<Object> results = new ArrayList<>(this.myArrayList.size());
        for (Object element : this.myArrayList) {
            if (element == null || NanoJSON.NULL.equals(element)) {
                results.add(null);
            } else if (element instanceof JSONArray) {
                results.add(((JSONArray) element).toList());
            } else if (element instanceof NanoJSON) {
                results.add(((NanoJSON) element).toMap());
            } else {
                results.add(element);
            }
        }
        return results;
    }

    public boolean isEmpty() {
        return this.myArrayList.isEmpty();
    }

    private void addAll(Collection<?> collection, boolean wrap) {
        this.myArrayList.ensureCapacity(this.myArrayList.size() + collection.size());
        if (wrap) {
            for (Object o : collection) {
                this.put(NanoJSON.wrap(o));
            }
        } else {
            for (Object o : collection) {
                this.put(o);
            }
        }
    }

    private void addAll(Iterable<?> iter, boolean wrap) {
        if (wrap) {
            for (Object o : iter) {
                this.put(NanoJSON.wrap(o));
            }
        } else {
            for (Object o : iter) {
                this.put(o);
            }
        }
    }

    private void addAll(Object array, boolean wrap) throws JSONException {
        if (array.getClass().isArray()) {
            int length = Array.getLength(array);
            this.myArrayList.ensureCapacity(this.myArrayList.size() + length);
            if (wrap) {
                for (int i = 0; i < length; i += 1) {
                    this.put(NanoJSON.wrap(Array.get(array, i)));
                }
            } else {
                for (int i = 0; i < length; i += 1) {
                    this.put(Array.get(array, i));
                }
            }
        } else if (array instanceof JSONArray) {
            this.myArrayList.addAll(((JSONArray) array).myArrayList);
        } else if (array instanceof Collection) {
            this.addAll((Collection<?>) array, wrap);
        } else if (array instanceof Iterable) {
            this.addAll((Iterable<?>) array, wrap);
        } else {
            throw new JSONException("JSONArray initial value should be a string or collection or array.");
        }
    }
}