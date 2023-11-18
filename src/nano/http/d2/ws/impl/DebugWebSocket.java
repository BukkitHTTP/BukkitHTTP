package nano.http.d2.ws.impl;

import nano.http.d2.ws.WebSocket;

import java.net.Socket;

public class DebugWebSocket extends WebSocket {
    public DebugWebSocket(Socket socket) {
        super(socket);
    }

    @Override
    public void onClose() {
        System.out.println("onClose");
    }

    @Override
    public void onError() {
        System.out.println("onError");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("onMessage: " + message);
        switch (message) {
            default:
                break;
            case "close":
                this.close();
                break;
            case "ping":
                this.send("pong");
                break;
            case "binary":
                this.send(new byte[]{0x01, 0x02, 0x03});
                break;
        }
    }

    @Override
    public void onBinaryMessage(byte[] bytes) {
        System.out.println("onBinaryMessage");
    }
}
