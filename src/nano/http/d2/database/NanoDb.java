package nano.http.d2.database;


import nano.http.d2.console.Logger;
import nano.http.d2.database.internal.SerlBridge;
import nano.http.d2.database.internal.SerlBridgeResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Supplier;

public class NanoDb<T extends Object> {
    private static final SerlBridge bridge = new SerlBridge("LET ME IN");
    private final File file;
    private final File tempFile;
    private final T impl;
    private final long saveIntervalMs;
    private long lastSaveTime;

    public NanoDb(ClassLoader classLoader, File file, Supplier<T> supplier, long saveIntervalMs) {
        this.file = file;
        this.tempFile = new File(file.getAbsolutePath() + ".tmp");
        this.saveIntervalMs = saveIntervalMs;
        if (file.exists()) {
            try {
                SerlBridgeResult result = bridge.deserialize(new FileInputStream(file).readAllBytes(), classLoader);
                impl = (T) result.obj;
                if (result.isDirty) {
                    Logger.warning("Database file isn't clean, creating backup. Mitigated?");
                    File backupFile = new File(file.getAbsolutePath() + ".bak");
                    if (file.renameTo(backupFile)) {
                        Logger.info("Backup created: " + backupFile.getAbsolutePath());
                    } else {
                        throw new IOException("Failed to create backup file: " + backupFile.getAbsolutePath());
                    }
                }
            } catch (Exception ex) {
                Logger.error("Failed to load database from file: " + file.getAbsolutePath(), ex);
                throw new ExceptionInInitializerError(ex);
            }
        } else {
            impl = supplier.get();
        }
    }

    public synchronized void save() throws IOException {
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(bridge.serialize(impl));
        fos.close();
        if (file.exists() && !file.delete()) {
            throw new IOException("Failed to delete old database file: " + file.getAbsolutePath());
        }
        if (!tempFile.renameTo(file)) {
            throw new IOException("Failed to rename temp file to database file: " + file.getAbsolutePath());
        }
    }

    public synchronized void tryAutoSave() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSaveTime >= saveIntervalMs) {
            try {
                save();
                lastSaveTime = currentTime;
            } catch (IOException e) {
                Logger.error("Auto-save failed for database file: " + file.getAbsolutePath(), e);
            }
        }
    }

    public T getImpl() {
        return impl;
    }

    public T getImplAndAutoSave() {
        tryAutoSave();
        return impl;
    }
}
