package nano.http.d2.database.csv;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class Localizer {
    private final Map<String, String> map = new HashMap<>();

    public void add(String from, String to) {
        map.put(from, to);
    }

    public String transform(String str) {
        if (map.containsKey(str)) {
            return map.get(str);
        }
        return str;
    }
}
