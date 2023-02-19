package nano.http.d2.database.internal;

import nano.http.d2.console.Console;
import nano.http.d2.console.Logger;
import nano.http.d2.database.Transfer;

import java.io.*;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("IOStreamConstructor")
public class MapSerl<K, V> {
    public void toFile(String f, Map<K, V> m) throws Exception {
        Set<K> Entities = m.keySet();
        Object[] KeyStorage = new Object[m.size()];
        Object[] ValueStorage = new Object[m.size()];
        int i = 0;
        for (K now : Entities) {
            KeyStorage[i] = now;
            ValueStorage[i++] = m.get(now);
        }
        SerlItem item = new SerlItem(KeyStorage, ValueStorage);
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
        oos.writeObject(item);
        oos.close();
    }

    @SuppressWarnings("unchecked")
    public ConcurrentHashMap<K, V> fromFile(String f, Class<?> clazz, ClassLoader context) throws Exception {
        boolean changed = false;
        ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f)) {
            @Override
            protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                try {
                    return context.loadClass(desc.getName());
                } catch (ClassNotFoundException e) {
                    return super.resolveClass(desc);
                }
            }
        };
        SerlItem item = (SerlItem) ois.readObject();
        for (int i = 0; i < item.key.length; i++) {
            if (clazz != null) {
                if (item.value[i].getClass().getName().equals(clazz.getName())) {
                    map.put((K) item.key[i], (V) item.value[i]);
                } else {
                    if (!changed) {
                        changed = true;
                        Logger.warning("Are you sure that you want to convert the database? (yes/no)");
                        Logger.warning("This operation is not reversible.");
                        Logger.warning("Cast " + item.value[i].getClass().getName() + " -> " + clazz.getName());
                        if (!"yes".equalsIgnoreCase(Console.await())) {
                            Logger.error("Aborted.");
                            System.exit(1);
                        }
                    }
                    map.put((K) item.key[i], (V) Transfer.copy(item.value[i], clazz.getConstructor().newInstance()));
                }
            } else {
                map.put((K) item.key[i], (V) item.value[i]);
            }
        }
        ois.close();
        return map;
    }
}
