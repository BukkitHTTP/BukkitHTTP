package nano.http.d2.qr;

import java.io.FileOutputStream;

public class Test {
    public static void main(String[] args) throws Exception {
        // Example usage of the QRCode class
        QrCode qr = QrCode.encodeText("你好，世界！", QrCode.Ecc.LOW);
        FileOutputStream fos = new FileOutputStream("qr.jpg");
        fos.write(qr.asJpeg());
        fos.close();
        Runtime.getRuntime().exec("explorer qr.jpg");
    }
}
