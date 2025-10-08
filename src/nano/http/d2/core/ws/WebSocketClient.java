package nano.http.d2.core.ws;

import nano.http.d2.core.ws.impl.WebSocketConstructor;
import nano.http.d2.core.ws.impl.WebSocketMachine;
import nano.http.d2.core.ws.impl.WebSocketResult;

import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class WebSocketClient {
    private static final String base = "GET {URI} HTTP/1.1\r\n" + "Host: {HOST}\r\n" + "Upgrade: websocket\r\n" + "Connection: Upgrade\r\n" + "Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==\r\n" + "Sec-WebSocket-Version: 13\r\n\r\n";
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private boolean isClosed = false;

    public WebSocketClient(String uri) throws IOException {
        this(uri, Proxy.NO_PROXY);
    }

    public WebSocketClient(String uri, Proxy p) throws IOException {
        String[] split = uri.split("://");
        String protocol = split[0];
        boolean ssl = false;
        if (protocol.equalsIgnoreCase("wss")) {
            ssl = true;
        } else if (!protocol.equalsIgnoreCase("ws")) {
            throw new IOException("Invalid protocol: " + protocol);
        }
        String host, path;
        int idx = split[1].indexOf("/");
        if (idx == -1) {
            host = split[1];
            path = "/";
        } else {
            host = split[1].substring(0, idx);
            path = split[1].substring(idx);
        }
        split = host.split(":");
        String ip = split[0];
        int port = split.length == 1 ? (ssl ? 443 : 80) : Integer.parseInt(split[1]);
        Socket socket;
        if (ssl) {
            socket = new Socket(p);
            socket.connect(new InetSocketAddress(ip, port));
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = factory.createSocket(socket, ip, port, true);
        } else {
            socket = new Socket(p);
            socket.connect(new InetSocketAddress(ip, port));
        }
        socket.getOutputStream().write(base.replace("{URI}", path).replace("{HOST}", host).getBytes(StandardCharsets.UTF_8));
        Scanner scanner = new Scanner(socket.getInputStream());
        boolean flag = false;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.isEmpty()) {
                break;
            }
            if (line.trim().equalsIgnoreCase("Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=")) {
                flag = true;
            }
        }
        if (!flag) {
            throw new IOException("WebSocket handshake failed.");
        }
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    public void send(String msg) {
        if (isClosed) {
            throw new IllegalStateException("WebSocket is closed.");
        }
        try {
            byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
            int length = bytes.length;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write((byte) 0x81); // Text frame

            if (length <= 125) {
                outputStream.write((byte) (length | 0x80)); // Set the mask bit
            } else if (length <= 65535) {
                outputStream.write((byte) (126 | 0x80)); // Set the mask bit
                outputStream.write((length >>> 8) & 0xFF);
                outputStream.write(length & 0xFF);
            } else {
                outputStream.write((byte) (127 | 0x80)); // Set the mask bit
                outputStream.write(new byte[]{0, 0, 0, 0}); // 4 high-order bytes set to 0 for lengths in the range of an int
                outputStream.write((length >>> 24) & 0xFF);
                outputStream.write((length >>> 16) & 0xFF);
                outputStream.write((length >>> 8) & 0xFF);
                outputStream.write(length & 0xFF);
            }

            // Masking key
            byte[] maskingKey = new byte[]{0x00, 0x00, 0x00, 0x00};
            outputStream.write(maskingKey);

            for (int i = 0; i < length; i++) {
                outputStream.write((byte) (bytes[i] ^ maskingKey[i % 4])); // Apply the mask
            }

            byte[] data = outputStream.toByteArray();
            this.outputStream.write(data);
        } catch (Exception ex) {
            isClosed = true;
            throw new IllegalStateException("WebSocket is closed.");
        }
    }

    public void sendBinary(byte[] bytes) {
        if (isClosed) {
            throw new IllegalStateException("WebSocket is closed.");
        }
        try {
            int length = bytes.length;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write((byte) 0x82); // Binary frame

            if (length <= 125) {
                outputStream.write((byte) (length | 0x80)); // Set the mask bit
            } else if (length <= 65535) {
                outputStream.write((byte) (126 | 0x80)); // Set the mask bit
                outputStream.write((length >>> 8) & 0xFF);
                outputStream.write(length & 0xFF);
            } else {
                outputStream.write((byte) (127 | 0x80)); // Set the mask bit
                outputStream.write(new byte[]{0, 0, 0, 0}); // 4 high-order bytes set to 0 for lengths in the range of an int
                outputStream.write((length >>> 24) & 0xFF);
                outputStream.write((length >>> 16) & 0xFF);
                outputStream.write((length >>> 8) & 0xFF);
                outputStream.write(length & 0xFF);
            }

            // Masking key
            byte[] maskingKey = new byte[]{0x00, 0x00, 0x00, 0x00};
            outputStream.write(maskingKey);

            for (int i = 0; i < length; i++) {
                outputStream.write((byte) (bytes[i] ^ maskingKey[i % 4])); // Apply the mask
            }

            byte[] data = outputStream.toByteArray();
            this.outputStream.write(data);
        } catch (Exception ex) {
            isClosed = true;
            throw new IllegalStateException("WebSocket is closed.");
        }
    }

    public String read() {
        if (isClosed) {
            return null;
        }
        try {
            WebSocketMachine machine = new WebSocketMachine();
            while (true) {
                int b = inputStream.read();
                if (b == -1) {
                    throw new IOException("WebSocket closed.");
                }
                WebSocketResult wsr = machine.update((byte) b);
                if (wsr != null) {
                    if (wsr.type == 1) {
                        return new String(wsr.binary);
                    }
                    if (wsr.type == 9) {
                        outputStream.write(WebSocketConstructor.constructPongFrame(wsr.binary));
                    }
                    if (wsr.type == 8) {
                        outputStream.write(WebSocketConstructor.constructCloseFrame(wsr.binary));
                        throw new IOException("WebSocket closed.");
                    }
                }
            }
        } catch (Exception ex) {
            isClosed = true;
            return null;
        }
    }

    public byte[] readBinary() {
        if (isClosed) {
            return null;
        }
        try {
            WebSocketMachine machine = new WebSocketMachine();
            while (true) {
                int b = inputStream.read();
                if (b == -1) {
                    throw new IOException("WebSocket closed.");
                }
                WebSocketResult wsr = machine.update((byte) b);
                if (wsr != null) {
                    if (wsr.type == 2) {
                        return wsr.binary;
                    }
                    if (wsr.type == 9) {
                        outputStream.write(WebSocketConstructor.constructPongFrame(wsr.binary));
                    }
                    if (wsr.type == 8) {
                        outputStream.write(WebSocketConstructor.constructCloseFrame(wsr.binary));
                        throw new IOException("WebSocket closed.");
                    }
                }
            }
        } catch (Exception ex) {
            isClosed = true;
            return null;
        }
    }

    public void close() throws IOException {
        if (!isClosed) {
            byte[] statusCodeBytes = new byte[]{(byte) 0x03, (byte) 0xE8};
            outputStream.write(WebSocketConstructor.constructCloseFrame(statusCodeBytes));
            isClosed = true;
        }
    }

    public boolean isClosed() {
        return isClosed;
    }
}
