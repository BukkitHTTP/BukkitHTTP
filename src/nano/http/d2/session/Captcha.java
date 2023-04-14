package nano.http.d2.session;

import nano.http.d2.console.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Random;

@SuppressWarnings("unused")
public class Captcha {
    private static final Random rdm = new Random();
    @SuppressWarnings("SpellCheckingInspection")
    private static final String chars = "23456789abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ";
    private static Font definedFont;

    // O0o 1lI Are not included, because they are easy to confuse
    static {
        try {
            //noinspection DataFlowIssue
            definedFont = Font.createFont(Font.TRUETYPE_FONT, Captcha.class.getResourceAsStream("/META-INF/text.ttf")).deriveFont((float) 30);
            //[Font: https://699pic.com/subject/gongyiziti.html] [License granted by 699pic.com | Commercial use allowed | Modification disallowed]
        } catch (Exception e) {
            Logger.info("Failed to load font. Using default font.");
            definedFont = new Font(new BufferedImage(1, 1, 1).getGraphics().getFont().getName(), Font.PLAIN, 30);
        }
    }

    private static String getRandomChar() {
        String str = "";
        if (rdm.nextInt(3) == 1) {
            if (rdm.nextInt(2) == 1) {
                str = str + (char) ('a' + rdm.nextInt(26));
            } else {
                str = str + (char) ('A' + rdm.nextInt(26));
            }
            return str;
        }
        if (rdm.nextInt(3) == 1) {
            str = str + (char) ('0' + rdm.nextInt(10));
            return str;
        }
        int highPos;
        int lowPos;
        highPos = (176 + Math.abs(rdm.nextInt(39)));
        lowPos = (161 + Math.abs(rdm.nextInt(93)));
        byte[] b = new byte[2];
        b[0] = (byte) highPos;
        b[1] = (byte) lowPos;
        try {
            str = new String(b, "GBK");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public static byte[] drawImage(String s) {
        try {
            int w = 40 + (25 * s.length());
            int h = 60;
            BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics paint = bufferedImage.getGraphics();

            //底色
            paint.setColor(new Color(255 - rdm.nextInt(100), 255 - rdm.nextInt(100), 255 - rdm.nextInt(100)));
            paint.fillRect(0, 0, w, h);

            //干扰字背景
            for (int o = 0; o < 3; o++) {
                definedFont = definedFont.deriveFont((float) (25 + rdm.nextInt(15)));
                paint.setFont(definedFont);
                paint.setColor(new Color(rdm.nextInt(255), rdm.nextInt(255), rdm.nextInt(255)));
                paint.drawString(getRandomChar(), rdm.nextInt(w / 3), rdm.nextInt(h));
            }
            for (int o = 0; o < 5; o++) {
                definedFont = definedFont.deriveFont((float) (25 + rdm.nextInt(15)));
                paint.setFont(definedFont);
                paint.setColor(new Color(rdm.nextInt(255), rdm.nextInt(255), rdm.nextInt(255)));
                paint.drawString(getRandomChar(), rdm.nextInt(w), rdm.nextInt(h));
            }

            //正常文本
            definedFont = definedFont.deriveFont((float) 30);
            paint.setFont(definedFont);
            for (int j = 0; j < s.length(); j++) {
                paint.setColor(new Color(rdm.nextInt(55), rdm.nextInt(55), rdm.nextInt(55)));
                paint.drawString(String.valueOf(s.charAt(j)), 10 + 25 * j + rdm.nextInt(3), 30 + rdm.nextInt(13));
            }
            //干扰线
            for (int o = 0; o < 15; o++) {
                paint.setColor(new Color(rdm.nextInt(255), rdm.nextInt(255), rdm.nextInt(255)));
                paint.drawLine(rdm.nextInt(w), rdm.nextInt(h), rdm.nextInt(w), rdm.nextInt(h));
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpeg", baos);
            baos.close();
            return baos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    public static String generateCaptcha() {
        String str = "";
        for (int i = 0; i < 7; i++) {
            //noinspection StringConcatenationInLoop
            str = str + chars.charAt(rdm.nextInt(chars.length()));
            // Only 7 characters. String concatenation is faster than StringBuilder
        }
        return str;
    }

    public static boolean validateCaptcha(Session session, String captcha) {
        if (session == null || captcha == null) {
            return false;
        }
        Object captchaInSession = session.getAttribute("b_captcha");
        if (captchaInSession == null) {
            return false;
        }
        if (captcha.equalsIgnoreCase((String) captchaInSession)) {
            session.removeAttribute("b_captcha");
            return true;
        }
        return false;
    }
}
