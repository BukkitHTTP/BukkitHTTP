package nano.http.d2.ws.impl;

import java.io.ByteArrayOutputStream;

public class WebSocketMachine {
    int state = 0;
    boolean fin = false;
    int opcode = 0;

    private void update0(byte b) {
        fin = (b & 0x80) != 0;
        opcode = b & 0x0F;
        state = 1;
    }

    boolean mask = false;
    long payloadLength = 0;

    private void update1(byte b) {
        mask = (b & 0x80) != 0;
        payloadLength = b & 0x7F;
        if (payloadLength == 126) {
            state = 2;
        } else if (payloadLength == 127) {
            state = 3;
        } else {
            state = mask ? 4 : 5;
        }
    }

    int ptr2 = 0;
    byte[] payloadLength2 = new byte[2];

    private void update2(byte b) {
        payloadLength2[ptr2++] = b;
        if (ptr2 == 2) {
            payloadLength = 256L * (payloadLength2[0] & 0xFF) + (payloadLength2[1] & 0xFF);
            state = mask ? 4 : 5;
        }
    }

    int ptr3 = 0;
    byte[] payloadLength3 = new byte[8];

    private void update3(byte b) {
        payloadLength3[ptr3++] = b;
        if (ptr3 == 8) {
            payloadLength = 0;
            long weight = 1;
            for (int i = 0; i < 8; i++) {
                payloadLength += weight * (payloadLength3[7 - i] & 0xFF);
                weight *= 256;
            }
            state = mask ? 4 : 5;
        }
    }

    int ptr4 = 0;
    byte[] maskingKey = new byte[4];

    private void update4(byte b) {
        maskingKey[ptr4++] = b;
        if (ptr4 == 4) {
            state = 5;
        }
    }

    int ptr5 = 0;
    byte[] payloadData = null;

    private byte[] update5(byte b) {
        if (payloadData == null) {
            if (payloadLength > Integer.MAX_VALUE)
                throw new RuntimeException("Payload too large");
            payloadData = new byte[(int) payloadLength];
        }
        payloadData[ptr5++] = b;
        if (ptr5 == payloadLength) {
            if (mask) {
                for (int i = 0; i < payloadLength; i++) {
                    payloadData[i] ^= maskingKey[i % 4];
                }
            }
            return reset();
        }
        return null;
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    private byte[] reset() {
        state = 0;
        ptr2 = 0;
        ptr3 = 0;
        ptr4 = 0;
        ptr5 = 0;
        try {
            baos.write(payloadData);
        } catch (Exception ignored) {
        }
        payloadData = null;
        if (fin) {
            byte[] data = baos.toByteArray();
            baos.reset();
            return data;
        } else {
            return null;
        }
    }

    public WebSocketResult update(byte b) {
        switch (state) {
            case 0:
                update0(b);
                break;
            case 1:
                update1(b);
                break;
            case 2:
                update2(b);
                break;
            case 3:
                update3(b);
                break;
            case 4:
                update4(b);
                break;
            case 5:
                byte[] data = update5(b);
                if (data != null) {
                    WebSocketResult result = new WebSocketResult();
                    result.type = opcode;
                    result.binary = data;
                    return result;
                }
                break;
        }
        return null;
    }
}
