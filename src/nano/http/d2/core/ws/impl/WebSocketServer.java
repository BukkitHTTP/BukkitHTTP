package nano.http.d2.core.ws.impl;

import nano.http.d2.console.Logger;
import nano.http.d2.core.ws.WebSocket;

import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("JavaReflectionMemberAccess")
public class WebSocketServer {
    private static final Map<String, Class<? extends WebSocket>> registered = new ConcurrentHashMap<>();
    public static ExecutorService executor = Executors.newCachedThreadPool();

    static {
        try {
            Class<?> cls = Executors.class;
            Method m = cls.getMethod("newVirtualThreadExecutor");
            ExecutorService executorService = (ExecutorService) m.invoke(null);
            if (executorService != null) {
                executor = executorService;
                Logger.info("Java 16+ detected, using virtual thread executor for WebSockets :)");
            }
        } catch (Exception ignored) {
        }
    }

    // Just demo as for now
    public static boolean checkWsProtocol(Properties header, String method, Socket socket, Properties parms, String uri) {
        if (!"GET".equals(method)) {
            return false;
        }
        String upgrade = header.getProperty("upgrade");
        if (!"websocket".equals(upgrade)) {
            return false;
        }
        String key = header.getProperty("sec-websocket-key");
        Class<? extends WebSocket> cls = registered.get(uri);
        if (cls == null) {
            return false;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(key.getBytes(StandardCharsets.UTF_8));
            md.update(("258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes(StandardCharsets.UTF_8));
            String accept = Base64.getEncoder().encodeToString(md.digest());
            String response = "HTTP/1.1 101 Switching Protocols\r\n" +
                    "Upgrade: websocket\r\n" +
                    "Connection: Upgrade\r\n" +
                    "Sec-WebSocket-Accept: " + accept + "\r\n" +
                    "\r\n";
            socket.getOutputStream().write(response.getBytes(StandardCharsets.UTF_8));
            WebSocket webSocket = cls.getConstructor(Socket.class).newInstance(socket);
            webSocket.checkParms(parms);
            executor.execute(webSocket);
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    public static void register(String path, Class<? extends WebSocket> cls) {
        registered.put(path, cls);
    }

    public static void unregister(String path) {
        registered.remove(path);
    }
}
