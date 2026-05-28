package nano.http.d2.csv;

import java.util.ArrayList;
import java.util.List;

public class CSV {
    public static List<List<String>> parse(String csv) {
        List<List<String>> result = new ArrayList<>();
        if (csv == null || csv.isEmpty()) {
            return result;
        }
        char[] chars = csv.toCharArray();
        List<String> currentRow = new ArrayList<>();
        char[] fieldBuf = new char[chars.length];
        int fieldLen = 0;

        boolean inQuotes = false;
        boolean lastCharWasComma = false;
        boolean hasField = false;

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < chars.length && chars[i + 1] == '"') {
                        fieldBuf[fieldLen++] = '"';
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    fieldBuf[fieldLen++] = c;
                }
                lastCharWasComma = false;
            } else {
                if (c == '"') {
                    // Start quoting only if it's the beginning of a field (compliant with RFC 4180)
                    if (fieldLen == 0 && !hasField) {
                        inQuotes = true;
                    } else {
                        // Otherwise treat it as a literal quote inside unquoted text
                        fieldBuf[fieldLen++] = c;
                    }
                    hasField = true;
                    lastCharWasComma = false;
                } else if (c == ',') {
                    currentRow.add(new String(fieldBuf, 0, fieldLen));
                    fieldLen = 0;
                    hasField = false; // Reset for the next field
                    lastCharWasComma = true;
                } else if (c == '\r') {
                    // Ignore carriage returns outside of quotes
                } else if (c == '\n') {
                    if (hasField || lastCharWasComma || fieldLen > 0) {
                        currentRow.add(new String(fieldBuf, 0, fieldLen));
                    }
                    if (!currentRow.isEmpty()) {
                        result.add(currentRow);
                        currentRow = new ArrayList<>();
                    }
                    fieldLen = 0;
                    hasField = false;
                    lastCharWasComma = false;
                } else {
                    fieldBuf[fieldLen++] = c;
                    hasField = true;
                    lastCharWasComma = false;
                }
            }
        }
        if (hasField || lastCharWasComma || fieldLen > 0) {
            currentRow.add(new String(fieldBuf, 0, fieldLen));
        }
        if (!currentRow.isEmpty()) {
            result.add(currentRow);
        }

        return result;
    }
}
