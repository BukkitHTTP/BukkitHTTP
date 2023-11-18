package nano.http.d2.core.ws;

import nano.http.d2.core.ws.impl.WebSocketConstructor;
import nano.http.d2.core.ws.impl.WebSocketMachine;
import nano.http.d2.core.ws.impl.WebSocketResult;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

public abstract class WebSocket implements Runnable {
    private final Socket socket;
    private final WebSocketMachine machine = new WebSocketMachine();
    private OutputStream output;
    private boolean isClosed = false;

    public WebSocket(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            InputStream input = socket.getInputStream();
            output = socket.getOutputStream();
            while (true) {
                int b = input.read();
                if (b == -1) {
                    onError_();
                }
                WebSocketResult result = machine.update((byte) b);
                if (result != null) {
                    switch (result.type) {
                        case 9:
                            output.write(WebSocketConstructor.constructPongFrame(result.binary));
                            break;
                        case 8:
                            output.write(WebSocketConstructor.constructCloseFrame(result.binary));
                            onClose_();
                            return;
                        case 1:
                            onMessage(new String(result.binary));
                            break;
                    }
                }
            }
        } catch (Exception e) {
            onError_();
        }
    }

    private void onError_() {
        if (!isClosed) {
            isClosed = true;
            closeForcibly();
            onError();
            onClose();
        }
    }

    private void onClose_() {
        if (!isClosed) {
            isClosed = true;
            closeForcibly();
            onClose();
        }
    }

    // onClose() is called when the WebSocket is closed by any reason (Normal or abnormal).
    // It is called only once.
    public abstract void onClose();

    // onError() is called when the WebSocket is closed by abnormal reason.
    // It is called only once.
    public abstract void onError();

    public abstract void onMessage(String message);

    public abstract void onBinaryMessage(byte[] bytes);

    public void checkParms(Properties parms) {
    }

    public void send(String message) {
        if (isClosed) {
            return;
        }
        try {
            output.write(WebSocketConstructor.constructStringFrame(message));
        } catch (Exception e) {
            onError_();
        }
    }

    public void send(byte[] bytes) {
        if (isClosed) {
            return;
        }
        try {
            output.write(WebSocketConstructor.constructBinaryFrame(bytes));
        } catch (Exception e) {
            onError_();
        }
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void close() {
        try {
            output.write(WebSocketConstructor.constructCloseFrame(new byte[0]));
            onClose_();
        } catch (Exception e) {
            onError_();
        }
    }

    private void closeForcibly() {
        try {
            socket.close();
        } catch (Exception ignored) {
        }
    }
}
