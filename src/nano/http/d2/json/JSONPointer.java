package nano.http.d2.json;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;

public class JSONPointer {
    private static final String ENCODING = "utf-8";
    private final List<String> refTokens;

    public JSONPointer(final String pointer) {
        if (pointer == null) {
            throw new NullPointerException("pointer cannot be null");
        }
        if (pointer.isEmpty() || pointer.equals("#")) {
            this.refTokens = Collections.emptyList();
            return;
        }
        String refs;
        if (pointer.startsWith("#/")) {
            refs = pointer.substring(2);
            try {
                refs = URLDecoder.decode(refs, ENCODING);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        } else if (pointer.startsWith("/")) {
            refs = pointer.substring(1);
        } else {
            throw new IllegalArgumentException("a JSON pointer should start with '/' or '#/'");
        }
        this.refTokens = new ArrayList<>();
        int slashIdx = -1;
        int prevSlashIdx;
        do {
            prevSlashIdx = slashIdx + 1;
            slashIdx = refs.indexOf('/', prevSlashIdx);
            if (prevSlashIdx == slashIdx || prevSlashIdx == refs.length()) {
                this.refTokens.add("");
            } else if (slashIdx >= 0) {
                final String token = refs.substring(prevSlashIdx, slashIdx);
                this.refTokens.add(unescape(token));
            } else {
                final String token = refs.substring(prevSlashIdx);
                this.refTokens.add(unescape(token));
            }
        } while (slashIdx >= 0);
    }

    private static String unescape(String token) {
        return token.replace("~1", "/").replace("~0", "~");
    }

    private static Object readByIndexToken(Object current, String indexToken) throws JSONPointerException {
        try {
            int index = Integer.parseInt(indexToken);
            JSONArray currentArr = (JSONArray) current;
            if (index >= currentArr.length()) {
                throw new JSONPointerException(format("index %s is out of bounds - the array has %d elements", indexToken, currentArr.length()));
            }
            try {
                return currentArr.get(index);
            } catch (JSONException e) {
                throw new JSONPointerException("Error reading value at index position " + index, e);
            }
        } catch (NumberFormatException e) {
            throw new JSONPointerException(format("%s is not an array index", indexToken), e);
        }
    }

    private static String escape(String token) {
        return token.replace("~", "~0").replace("/", "~1");
    }

    public Object queryFrom(Object document) throws JSONPointerException {
        if (this.refTokens.isEmpty()) {
            return document;
        }
        Object current = document;
        for (String token : this.refTokens) {
            if (current instanceof NanoJSON) {
                current = ((NanoJSON) current).opt(unescape(token));
            } else if (current instanceof JSONArray) {
                current = readByIndexToken(current, token);
            } else {
                throw new JSONPointerException(format("value [%s] is not an array or object therefore its key %s cannot be resolved", current, token));
            }
        }
        return current;
    }

    @Override
    public String toString() {
        StringBuilder rval = new StringBuilder();
        for (String token : this.refTokens) {
            rval.append('/').append(escape(token));
        }
        return rval.toString();
    }
}
