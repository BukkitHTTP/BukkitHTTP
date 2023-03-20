package nano.http.d2.hooks.impls;

import nano.http.d2.console.Logger;
import nano.http.d2.hooks.interfaces.SocketHookProvider;

import java.util.HashMap;
import java.util.Map;

public class DefaultSock implements SocketHookProvider {
    private final Map<String, Conn> map = new HashMap<>();
    private final Map<String, Conn> blackList = new HashMap<>();

    @Override
    public boolean Accept(String ip) {
        if (blackList.containsKey(ip)) {
            Conn conn = blackList.get(ip);
            if (conn.expire < System.currentTimeMillis()) {
                blackList.remove(ip);
                Logger.warning("IP " + ip + " has been unblocked by the NanoFirewall.");
                return true;
            }
            return false;
        }
        if (!map.containsKey(ip)) {
            map.put(ip, new Conn());
        }
        Conn conn = map.get(ip);
        if (conn.expire < System.currentTimeMillis()) {
            map.remove(ip);
            return true;
        }
        conn.count++;
        if (conn.count > 25) {
            Logger.warning("IP " + ip + " has been blocked by the NanoFirewall.");
            map.remove(ip);
            Conn block = new Conn();
            block.expire = System.currentTimeMillis() + 2 * 60 * 60 * 1000L;   // 2 hours
            blackList.put(ip, block);
            return false;
        }
        map.put(ip, conn);
        return true;
    }
}

class Conn {
    long expire = System.currentTimeMillis() + 10000;
    int count = 1;
}
