package nano.http.d2.ai.data;

import nano.http.d2.database.internal.SerlClz;
import nano.http.d2.json.JSONArray;

import java.io.IOException;

@SerlClz
public class Message {
    public String role;
    public String content;
    public transient String tool_calling_id = null;
    public transient JSONArray tool_called = null;
    public boolean trans = false;

    @SuppressWarnings("unused")
    public Message() {
        // Deserialization constructor
    }

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public void validate() throws IOException {
        if (trans) {
            throw new IOException("TRANS!");
        }
    }

    @Override
    public String toString() {
        return "Message{" +
                "role='" + role + '\'' +
                ", content='" + content + '\'' +
                (tool_calling_id == null ? "" : ", tool_calling_id='" + tool_calling_id + '\'') +
                (tool_called == null ? "" : ", tool_called=" + tool_called) +
                (trans ? "TRANS" : "") +
                '}';
    }
}
