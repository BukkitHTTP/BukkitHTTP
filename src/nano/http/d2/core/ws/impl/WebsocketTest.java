package nano.http.d2.core.ws.impl;

public class WebsocketTest {
    public static void main(String[] args) throws Exception {
        byte[] bytes = new byte[]{
                (byte) 0x89, 0x05, 0x48, 0x65, 0x6C, 0x6C, 0x6F
        };
//        byte[] bytes = new byte[]{
//                (byte) 0x88, 0x02, 0x03, (byte) 0xE8
//        };
        WebSocketMachine machine = new WebSocketMachine();
        for (byte b : bytes) {
            WebSocketResult result = machine.update(b);
            if (result != null) {
                System.out.println(result.type);
                for (byte b2 : result.binary) {
                    System.out.printf("%02X ", b2);
                }
            }
        }
    }
}
