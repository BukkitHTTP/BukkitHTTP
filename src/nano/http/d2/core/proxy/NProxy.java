package nano.http.d2.core.proxy;

import nano.http.d2.core.ws.impl.WebSocketServer;
import nano.http.d2.hooks.HookManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

public class NProxy {
    public static boolean checkProxyProtocol(Properties header, String method, Socket socket, Properties parms, String uri) {
        if (method.equalsIgnoreCase("CONNECT")) {
            if (!HookManager.proxyHook.Accept(uri)) {
                return false;
            }
            final Socket[] proxySocket = new Socket[1];
            WebSocketServer.executor.submit(() -> {
                try {
                    String host = uri.split(":")[0];
                    int port = Integer.parseInt(uri.split(":")[1]);
                    proxySocket[0] = new Socket(host, port);
                    InputStream proxyIn = proxySocket[0].getInputStream();
                    OutputStream proxyOut = proxySocket[0].getOutputStream();
                    InputStream socketIn = socket.getInputStream();
                    OutputStream socketOut = socket.getOutputStream();
                    socketOut.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
                    WebSocketServer.executor.submit(() -> {
                        try {
                            byte[] buffer = new byte[4096];
                            int len;
                            while ((len = proxyIn.read(buffer)) != -1) {
                                socketOut.write(buffer, 0, len);
                                socketOut.flush();
                            }
                            throw new Exception("Connection closed");
                        } catch (Exception ex) {
                            try {
                                socket.close();
                                proxySocket[0].close();
                            } catch (IOException ignored) {
                            }
                        }
                    });
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = socketIn.read(buffer)) != -1) {
                        proxyOut.write(buffer, 0, len);
                        proxyOut.flush();
                    }
                    throw new Exception("Connection closed");
                } catch (Exception ex) {
                    try {
                        socket.close();
                        proxySocket[0].close();
                    } catch (IOException ignored) {
                    }
                }
            });
            return true;
        }
        return false;
    }
}
