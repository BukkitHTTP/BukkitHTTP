package nano.http.d2.json;

public class JSONWriter {
    public static String valueToString(Object value) throws JSONException {
        if (value == null) {
            return "null";
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
        if (value instanceof Enum<?>) {
            return NanoJSON.quote(((Enum<?>) value).name());
        }
        return NanoJSON.quote(value.toString());
    }
}
