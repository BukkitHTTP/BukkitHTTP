package nano.http.d2.hooks.impls;

import nano.http.d2.console.Logger;
import nano.http.d2.hooks.interfaces.SocketHookProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultSock implements SocketHookProvider {
    // NOTE: strike() is called from request threads while Accept() is called
    // from the accept thread, so this shared map must be concurrent, and the
    // read-modify-write sequences must be atomic (compute()).
    static final Map<String, Conn> map = new ConcurrentHashMap<>();
    // Only touched from the (single) accept thread inside Accept(), plain map is fine.
    private final Map<String, Conn> blackList = new HashMap<>();

    public static void strike(String ip, int weight) {
        map.compute(ip, (k, conn) -> {
            if (conn == null || conn.expire < System.currentTimeMillis()) {
                conn = new Conn();
            }
            conn.count += weight;
            return conn;
        });
    }

    @Override
    public boolean Accept(String ip) {
        long now = System.currentTimeMillis();
        Conn blocked = blackList.get(ip);
        if (blocked != null) {
            if (blocked.expire < now) {
                blackList.remove(ip);
                Logger.warning("IP " + ip + " has been unblocked by the NanoFirewall.");
                return true;
            }
            return false;
        }
        boolean[] accept = {false};
        map.compute(ip, (k, conn) -> {
            if (conn == null || conn.expire < now) {
                conn = new Conn();
            }
            conn.count++;
            if (conn.count > 25) {
                Logger.warning("IP " + ip + " has been blocked by the NanoFirewall.");
                Conn block = new Conn();
                block.expire = now + 2 * 60 * 60 * 1000L;   // 2 hours
                blackList.put(ip, block);
                return null; // remove from map
            }
            accept[0] = true;
            return conn;
        });
        return accept[0];
    }
}

class Conn {
    long expire = System.currentTimeMillis() + 10000;
    int count = 1;
}
