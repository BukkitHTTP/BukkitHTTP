package nano.http.d2.ws.impl;

import java.nio.ByteBuffer;

public class WebSocketConstructor {
    public static byte[] constructStringFrame(String message) {
        byte[] bytes = message.getBytes();
        byte[] result;
        if (bytes.length <= 125) {
            result = new byte[bytes.length + 2];
            result[0] = (byte) 0x81;
            result[1] = (byte) bytes.length;
        } else if (bytes.length <= 65535) {
            result = new byte[bytes.length + 4];
            result[0] = (byte) 0x81;
            result[1] = 126;
            ByteBuffer.wrap(result, 2, 2).putShort((short) bytes.length);
        } else {
            result = new byte[bytes.length + 10];
            result[0] = (byte) 0x81;
            result[1] = 127;
            ByteBuffer.wrap(result, 2, 8).putLong(bytes.length);
        }
        System.arraycopy(bytes, 0, result, result.length - bytes.length, bytes.length);
        return result;
    }

    public static byte[] constructBinaryFrame(byte[] bytes) {
        byte[] result;
        if (bytes.length <= 125) {
            result = new byte[bytes.length + 2];
            result[0] = (byte) 0x82;
            result[1] = (byte) bytes.length;
        } else if (bytes.length <= 65535) {
            result = new byte[bytes.length + 4];
            result[0] = (byte) 0x82;
            result[1] = 126;
            ByteBuffer.wrap(result, 2, 2).putShort((short) bytes.length);
        } else {
            result = new byte[bytes.length + 10];
            result[0] = (byte) 0x82;
            result[1] = 127;
            ByteBuffer.wrap(result, 2, 8).putLong(bytes.length);
        }
        System.arraycopy(bytes, 0, result, result.length - bytes.length, bytes.length);
        return result;
    }

    public static byte[] constructPongFrame(byte[] bytes) {
        byte[] result;
        if (bytes.length <= 125) {
            result = new byte[bytes.length + 2];
            result[0] = (byte) 0x8A;
            result[1] = (byte) bytes.length;
        } else if (bytes.length <= 65535) {
            result = new byte[bytes.length + 4];
            result[0] = (byte) 0x8A;
            result[1] = 126;
            ByteBuffer.wrap(result, 2, 2).putShort((short) bytes.length);
        } else {
            result = new byte[bytes.length + 10];
            result[0] = (byte) 0x8A;
            result[1] = 127;
            ByteBuffer.wrap(result, 2, 8).putLong(bytes.length);
        }
        System.arraycopy(bytes, 0, result, result.length - bytes.length, bytes.length);
        return result;
    }

    public static byte[] constructCloseFrame(byte[] bytes) {
        byte[] result;
        if (bytes.length <= 125) {
            result = new byte[bytes.length + 2];
            result[0] = (byte) 0x88;
            result[1] = (byte) bytes.length;
        } else if (bytes.length <= 65535) {
            result = new byte[bytes.length + 4];
            result[0] = (byte) 0x88;
            result[1] = 126;
            ByteBuffer.wrap(result, 2, 2).putShort((short) bytes.length);
        } else {
            result = new byte[bytes.length + 10];
            result[0] = (byte) 0x88;
            result[1] = 127;
            ByteBuffer.wrap(result, 2, 8).putLong(bytes.length);
        }
        System.arraycopy(bytes, 0, result, result.length - bytes.length, bytes.length);
        return result;
    }
}
