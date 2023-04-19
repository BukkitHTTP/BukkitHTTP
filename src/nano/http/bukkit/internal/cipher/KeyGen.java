package nano.http.bukkit.internal.cipher;

import nano.http.d2.console.Console;
import nano.http.d2.console.Logger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Random;

public class KeyGen implements Runnable {
    private static final String KEY = "YOUR_KEY_HERE";
    private static final Random rdm = new Random();
    private static String hwid = null;

    private static String safeBase64(String origin) {
        String random = String.valueOf(100000 + rdm.nextInt(900000));
        byte[] msg = origin.getBytes(StandardCharsets.UTF_8);
        byte[] pswd = random.getBytes(StandardCharsets.UTF_8);
        CipheredClassLoader.decrypt(msg, pswd);
        String result = random + java.util.Base64.getEncoder().encodeToString(msg);
        byte[] msg2 = result.getBytes(StandardCharsets.UTF_8);
        byte[] pswd2 = KEY.getBytes(StandardCharsets.UTF_8);
        CipheredClassLoader.decrypt(msg2, pswd2);
        return java.util.Base64.getEncoder().encodeToString(msg2).replace("=", "*");
    }

    private static String deSafeBase64(String origin) {
        byte[] msg = java.util.Base64.getDecoder().decode(origin.replace("*", "="));
        byte[] pswd = KEY.getBytes(StandardCharsets.UTF_8);
        CipheredClassLoader.decrypt(msg, pswd);
        String result = new String(msg, StandardCharsets.UTF_8);
        byte[] msg2 = java.util.Base64.getDecoder().decode(result.substring(6));
        byte[] pswd2 = result.substring(0, 6).getBytes(StandardCharsets.UTF_8);
        CipheredClassLoader.decrypt(msg2, pswd2);
        return new String(msg2, StandardCharsets.UTF_8);
    }

    public static String hwid() {
        if (hwid == null) {
            try {
                hwid = System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("COMPUTERNAME") + System.getProperty("user.name") + System.getProperty("os.name") + System.getProperty("os.arch") + System.getProperty("os.version");
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(hwid.getBytes());
                StringBuilder hexString = new StringBuilder();
                byte[] byteData = md.digest();
                for (byte aByteData : byteData) {
                    String hex = Integer.toHexString(0xff & aByteData);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                hwid = hexString.toString();
            } catch (Exception e) {
                hwid = "092211155e8bbdd1b829f8f67233fc71";
            }
        }
        return hwid;
    }

    public static String generate(String hwid, int days, String key) {
        String real = hwid + ";" + (System.currentTimeMillis() + days * 86400000L) + ";" + key;
        return safeBase64(real);
    }

    public static String getKey(String key) {
        try {
            String real = deSafeBase64(key);
            String[] split = real.split(";");
            long time = Long.parseLong(split[1]);
            if (split.length != 3) {
                return "X";
            }
            if (time < System.currentTimeMillis()) {
                Logger.error("License expired!");
                return "X";
            }
            if (!split[0].equals(hwid())) {
                Logger.error("HWID mismatch!");
                return "X";
            }
            Logger.info("License valid! (Expires in " + (time - System.currentTimeMillis()) / 86400000L + " days)");
            return split[2];
        } catch (Exception e) {
            Logger.info("License invalid!");
            return "X";
        }
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void run() {
        Logger.info("Please specify the HWID:");
        String hwid = Console.await();
        Logger.info("Please specify the duration (in days):");
        int days = Integer.parseInt(Console.await());
        Logger.info("Please specify the key:");
        String key = Console.await();
        Logger.info("The key is: " + generate(hwid, days, key).replace("=", "\\="));
    }
}
