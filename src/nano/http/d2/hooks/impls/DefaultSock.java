package nano.http.d2.hooks.impls;

import nano.http.d2.console.Logger;
import nano.http.d2.hooks.interfaces.SocketHookProvider;

import java.util.HashMap;
import java.util.Map;

public class DefaultSock implements SocketHookProvider {
    private final Map<String, Con> map = new HashMap<>();
    private final Map<String, Con> blackList = new HashMap<>();

    @Override
    public boolean Accept(String ip) {
        if (blackList.containsKey(ip)) {
            Con con = blackList.get(ip);
            if (con.expire < System.currentTimeMillis()) {
                blackList.remove(ip);
                Logger.warning("IP " + ip + " has been unblocked by the NanoFirewall.");
                return true;
            }
            return false;
        }
        if (!map.containsKey(ip)) {
            map.put(ip, new Con());
        }
        Con con = map.get(ip);
        if (con.expire < System.currentTimeMillis()) {
            map.remove(ip);
            return true;
        }
        con.count++;
        if (con.count > 15) {
            Logger.warning("IP " + ip + " has been blocked by the NanoFirewall.");
            map.remove(ip);
            Con block = new Con();
            block.expire = System.currentTimeMillis() + 2 * 60 * 60 * 1000L;   // 2 hours
            blackList.put(ip, block);
            return false;
        }
        map.put(ip, con);
        return true;
    }
}

class Con {
    long expire = System.currentTimeMillis() + 10000;
    int count = 1;
}
