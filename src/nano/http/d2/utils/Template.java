package nano.http.d2.utils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Template {
    private final String template;

    private Template(String template) {
        this.template = template;
    }

    public Template(InputStream is) {
        try {
            template = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public Template replace(String key, String value) {
        return new Template(template.replace("{{" + key + "}}", value));
    }

    @Override
    public String toString() {
        return template;
    }
}
