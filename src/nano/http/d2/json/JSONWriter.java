package nano.http.d2.json;

import java.util.Collection;
import java.util.Map;

public class JSONWriter {
    public static String valueToString(Object value) throws JSONException {
        if (value == null) {
            return "null";
        }
        if (value instanceof JSONString) {
            String object;
            try {
                object = ((JSONString) value).toJSONString();
            } catch (Exception e) {
                throw new JSONException(e);
            }
            if (object != null) {
                return object;
            }
            throw new JSONException("Bad value from toJSONString: null");
        }
        if (value instanceof Number) {
            final String numberAsString = NanoJSON.numberToString((Number) value);
            if (NanoJSON.NUMBER_PATTERN.matcher(numberAsString).matches()) {
                return numberAsString;
            }
            return NanoJSON.quote(numberAsString);
        }
        if (value instanceof Boolean || value instanceof NanoJSON || value instanceof JSONArray) {
            return value.toString();
        }
        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            return new NanoJSON(map).toString();
        }
        if (value instanceof Collection) {
            Collection<?> coll = (Collection<?>) value;
            return new JSONArray(coll).toString();
        }
        if (value.getClass().isArray()) {
            return new JSONArray(value).toString();
        }
        if (value instanceof Enum<?>) {
            return NanoJSON.quote(((Enum<?>) value).name());
        }
        return NanoJSON.quote(value.toString());
    }
}
