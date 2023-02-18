package nano.http.d2.console;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

public class Logger {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final boolean release = false;
    private static OutputStreamWriter osw;

    static {
        try {
            File f = new File("server.log");
            FileOutputStream fos;
            if (!f.exists()) {
                if (!f.createNewFile()) {
                    throw new Exception("Can not create log file.");
                }
                fos = new FileOutputStream(f);
            } else {
                fos = new FileOutputStream(f, true);
            }
            osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            Runtime.getRuntime().addShutdownHook(new Thread(Logger::flush));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[FATAL] Can not init Log5j.");
            System.exit(-1);
        }
    }

    public static void info(String str) {
        System.out.println("[INFO] " + str);
        try {
            osw.write("[" + simpleDateFormat.format(System.currentTimeMillis()) + "] [INFO] ");
            osw.write(str);
            osw.write("\n");
        } catch (Exception ignored) {
        }
    }

    public static void warning(String str) {
        System.err.println("[WARNING] " + str);
        try {
            osw.write("[" + simpleDateFormat.format(System.currentTimeMillis()) + "] [WARNING] ");
            osw.write(str);
            osw.write("\n");
        } catch (Exception ignored) {
        }
    }

    public static void error(String str) {
        System.err.println("[ERROR] " + str);
        try {
            osw.write("[" + simpleDateFormat.format(System.currentTimeMillis()) + "] [ERROR] ");
            osw.write(str);
            osw.write("\n");
        } catch (Exception ignored) {
        }
    }

    public static void error(String str, Exception e) {
        System.err.println("[ERROR] " + str);
        e.printStackTrace();
        try {
            osw.write("[" + simpleDateFormat.format(System.currentTimeMillis()) + "] [ERROR] ");
            osw.write(str);
            osw.write("\n");
            e.printStackTrace(new PrintWriter(osw));
        } catch (Exception ignored) {
        }
    }

    public static void debug(String str) {
        if (release) {
            return;
        }
        System.out.println("[DEBUG] " + str);
        try {
            osw.write("[" + simpleDateFormat.format(System.currentTimeMillis()) + "] [DEBUG] ");
            osw.write(str);
            osw.write("\n");
        } catch (Exception ignored) {
        }
    }

    public static void flush() {
        try {
            osw.flush();
        } catch (Exception ignored) {
        }
    }
}
