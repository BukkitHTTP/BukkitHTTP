package nano.http.d2.core;

import nano.http.bukkit.internal.BukkitStop;
import nano.http.d2.console.Logger;
import nano.http.d2.hooks.HookManager;
import nano.http.d2.serve.ServeProvider;

import java.net.ServerSocket;
import java.net.Socket;

public class NanoHTTPd {
    private final Thread myThread;
    private final ServerSocket myServerSocket;


    public NanoHTTPd(int port, ServeProvider server) throws Exception {
        myServerSocket = SocketFactory.createServerSocket(port);
        myThread = new Thread(() -> {
            try {
                while (true) {
                    Socket s = myServerSocket.accept();
                    if (!HookManager.socketHook.Accept(s.getInetAddress().getHostAddress())) {
                        s.close();
                        continue;
                    }
                    new HTTPSession(s, server);
                }
            } catch (Throwable e) {
                if (BukkitStop.isStopping) {
                    Logger.info("ND2: Server is closed...");
                } else {
                    Logger.error("ND2: Server is force closed...", e);
                }
            }
        });
        myThread.setName("NanoHTTP-Server-" + port);
        myThread.start();
    }

    public void stop() {
        try {
            myServerSocket.close();
            myThread.join();
        } catch (Exception ignored) {
        }
    }
}
