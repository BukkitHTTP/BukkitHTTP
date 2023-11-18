package nano.http.d2.ws.impl;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public class WebSocketServer {
    // Just demo as for now
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(81);
        while (true) {
            try (Socket socket = serverSocket.accept()) {
                InputStream input = socket.getInputStream();
                Scanner scanner = new Scanner(input);
                List<String> lines = new ArrayList<>();
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.isEmpty()) {
                        break;
                    }
                    lines.add(line);
                }
                if (lines.size() < 3) {
                    throw new Exception("Invalid request. Not a HTTP request.");
                }
                String[] firstLine = lines.get(0).split(" ");
                if (firstLine.length != 3) {
                    throw new Exception("Invalid request. Not a HTTP header.");
                }
                String method = firstLine[0];
                String path = firstLine[1];
                if (!method.equals("GET")) {
                    throw new Exception("Invalid request. Only GET is supported.");
                }
                HashMap<String, String> headers = new HashMap<>();
                for (String s : lines) {
                    String[] kv = s.split(":");
                    if (kv.length == 2) {
                        headers.put(kv[0].trim().toLowerCase(), kv[1].trim());
                    }
                }
                if (!headers.containsKey("sec-websocket-key")) {
                    throw new Exception("Invalid request. Not a WebSocket request.");
                }
                System.out.println("WebSocket request: " + path);
                String key = headers.get("sec-websocket-key");
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                md.update(key.getBytes(StandardCharsets.UTF_8));
                md.update(("258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes(StandardCharsets.UTF_8));
                String accept = new String(Base64.getEncoder().encodeToString(md.digest()));
                System.out.println("Accept: " + accept);
                String response = "HTTP/1.1 101 Switching Protocols\r\n" +
                        "Upgrade: websocket\r\n" +
                        "Connection: Upgrade\r\n" +
                        "Sec-WebSocket-Accept: " + accept + "\r\n" +
                        "\r\n";
                socket.getOutputStream().write(response.getBytes(StandardCharsets.UTF_8));
                new DebugWebSocket(socket).run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
