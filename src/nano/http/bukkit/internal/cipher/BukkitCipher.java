package nano.http.bukkit.internal.cipher;

import nano.http.d2.console.Console;
import nano.http.d2.console.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class BukkitCipher implements Runnable {
    static {
        Logger.info(">>--- THIS IS A PRIVATE BUILD, DO NOT DISTRIBUTE! ---<<");
        Logger.info("Your HWID is:   " + KeyGen.hwid());
        Logger.info(">>--- THIS IS A PRIVATE BUILD, DO NOT DISTRIBUTE! ---<<");
    }

    @Override
    @SuppressWarnings({"DataFlowIssue", "IOStreamConstructor"})
    public void run() {
        Logger.info("Please enter the path of the input jar file: ");
        String inputJarPath = Console.await();
        Logger.info("Please enter the password: ");
        byte[] password = Console.await().getBytes();
        File inputJarFile = new File(inputJarPath);
        File outputJarFile = new File(inputJarPath.replace(".jar", "-redacted.jar"));

        try (JarFile inputJar = new JarFile(inputJarFile); JarOutputStream outputJar = new JarOutputStream(new FileOutputStream(outputJarFile))) {
            Enumeration<JarEntry> entries = inputJar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                InputStream input = inputJar.getInputStream(entry);
                byte[] data = CipheredClassLoader.readAllBytes(input);
                CipheredClassLoader.decrypt(data, password);
                if (data.length > 4) {
                    if (data[0] == (byte) 0xCA && data[1] == (byte) 0xFE && data[2] == (byte) 0xBA && data[3] == (byte) 0xBE) {
                        // You're not cracking with this, not at least without modifying the source code. :P
                        data = new byte[data.length];
                    }
                }
                JarEntry outputEntry;
                if (entry.getName().endsWith(".class")) {
                    outputEntry = new JarEntry(CipheredClassLoader.process(entry.getName()) + "/");
                    // Magic: Make the entry a directory. And make the crack kid doubt himself.
                } else {
                    if (entry.getName().endsWith("/")) {
                        continue;
                    }
                    outputEntry = new JarEntry(entry.getName());
                }
                outputJar.putNextEntry(outputEntry);
                outputJar.write(data);
            }
            Logger.info("The jar file has been redacted successfully.");
        } catch (Exception e) {
            Logger.error("An error occurred while redacting the jar file: ", e);
        }
    }
}
