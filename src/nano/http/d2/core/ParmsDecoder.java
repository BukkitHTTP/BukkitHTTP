package nano.http.d2.core;

import nano.http.d2.utils.Encoding;

import java.util.Properties;
import java.util.StringTokenizer;

public class ParmsDecoder {
    /**
     * Decodes the percent encoding scheme. <br/>
     * For example: "an+example%20string" -> "an example string"
     * Modified - added support for Chinese characters
     */
    public static String decodePercent(String str) {
        return Encoding.deURL(str);
    }

    /**
     * Decodes parameters in percent-encoded URI-format
     * ( e.g. "name=Jack%20Daniels&pass=Single%20Malt" ) and
     * adds them to given Properties. NOTE: this doesn't support multiple
     * identical keys due to the simplicity of Properties -- if you need multiples,
     * you might want to replace the Properties with a Hashtable of Vectors or such.
     */
    public static void decodeParms(String parms, Properties p) {
        if (parms == null) {
            return;
        }
        if (parms.startsWith("{")) {
            p.put("json", parms);
            return;
        }
        StringTokenizer st = new StringTokenizer(parms, "&");
        while (st.hasMoreTokens()) {
            String e = st.nextToken();
            int sep = e.indexOf('=');
            String dec = decodePercent((sep >= 0) ? e.substring(0, sep) : e);
            if (dec != null) {
                p.put(dec.trim(), (sep >= 0) ? decodePercent(e.substring(sep + 1)) : "");
            }
        }
    }
}
