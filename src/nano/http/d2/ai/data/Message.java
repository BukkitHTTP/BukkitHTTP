package nano.http.d2.ai.data;

import nano.http.d2.database.internal.SerlClz;

import java.io.IOException;

@SerlClz
public class Message {
    public String role;
    public String content;
    public String id = "";
    public String tool = "";
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
        if (trans || !tool.isEmpty() || !id.isEmpty()) {
            throw new IOException("TRANS!");
        }
    }

    @Override
    public String toString() {
        return "Message{" +
                "role='" + role + '\'' +
                ", content='" + content + '\'' +
                (tool.isEmpty() ? "" : ", tool='" + tool + '\'') +
                (id.isEmpty() ? "" : ", tcid='" + id + '\'') +
                '}';
    }
}
