package nano.http.d2.ai.data;

import nano.http.d2.ai.OpenRouter;
import nano.http.d2.ai.Tool;
import nano.http.d2.database.internal.SerlBridge;
import nano.http.d2.database.internal.SerlClz;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@SerlClz
public class ChatContext {
    private static final SerlBridge bridge = new SerlBridge("LET ME IN");
    public transient volatile boolean cancelled = false;
    public transient List<Tool> tools = new ArrayList<>();

    public String key_override = null;
    public String model = "z-ai/glm-4.6:exacto";
    public boolean noThinking = true;
    public List<Message> messages = new ArrayList<>();
    public transient List<String> imageB64 = null;


    public ChatContext() {
        // Deserialization constructor
    }

    public static ChatContext fromBytes(byte[] data) throws IOException {
        return (ChatContext) bridge.deserialize(data).obj;
    }

    public byte[] toBytes() throws IOException {
        trimTrans();
        return bridge.serialize(this);
    }

    @SuppressWarnings("unused")
    public void __() throws IOException {
        for (Message msg : messages) {
            msg.validate();
        }
    }

    public void trimTrans() {
        List<Message> newMessages = new ArrayList<>();
        for (Message msg : messages) {
            if (!msg.trans) {
                newMessages.add(msg);
            }
        }
        messages = newMessages;
    }

    public void complete() {
        OpenRouter.complete(this, null, true);
    }

    public void complete(Runnable toolPartialCallback) {
        OpenRouter.complete(this, toolPartialCallback, true);
    }

    public void complete(boolean cleanup) {
        OpenRouter.complete(this, null, cleanup);
    }

    public void addImage(byte[] image) {
        if (this.imageB64 == null) {
            this.imageB64 = new ArrayList<>();
        }
        String B64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(image);
        this.imageB64.add(B64);
    }

    public void addImage(File f) {
        try (FileInputStream fis = new FileInputStream(f)) {
            addImage(fis.readAllBytes());
        } catch (Exception ignored) {
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n----------------------------------------\n");
        for (Message m : messages) {
            sb.append(m);
            sb.append("\n----------------------------------------\n");
        }

        return "ChatContext{" + "cancelled=" + cancelled + ", model='" + model + '\'' + ", noThinking=" + noThinking + ", messages=" + sb + ", tools=" + tools + '}';
    }
}
