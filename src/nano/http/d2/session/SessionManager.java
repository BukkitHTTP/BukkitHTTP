package nano.http.d2.session;

import nano.http.d2.consts.Mime;
import nano.http.d2.consts.Status;
import nano.http.d2.core.Response;

import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class SessionManager {
    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private static final Random random = new Random();

    public static Session getSession(Properties header) {
        if (!header.containsKey("cookie")) {
            return null;
        }
        String cookie = header.getProperty("cookie");
        int sessionIndex = cookie.indexOf("session=");
        if (sessionIndex == -1) {
            return null;
        }
        sessionIndex += 8;
        int semicolonIndex = cookie.indexOf(";", sessionIndex);
        if (semicolonIndex == -1) {
            semicolonIndex = cookie.length();
        }
        String sessionValue = cookie.substring(sessionIndex, semicolonIndex).trim();
        return getOrCreateSession(sessionValue);
    }

    public static Response validateOrDie(Session session, String permission, Response denied) {
        if (session != null) {
            if (permission == null) {
                return null;
            }
            if (session.hasPermissions(permission)) {
                return null;
            }
        }
        denied = denied == null ? new Response(Status.HTTP_FORBIDDEN, Mime.MIME_PLAINTEXT, "Access Denied!\nTry:\n1)Enable Cookie\n2)Refresh The Page\n3)Contact Admin") : denied;
        return denied;
    }

    public static String unusedSessionName() {
        while (true) {
            String ses = "Bukkit_" + (random.nextInt(89999999) + 10000000);
            if (!sessions.containsKey(ses)) {
                return ses;
            }
        }
    }

    public static void gc() {
        if (random.nextInt(1000) != 0) {
            return;
        }
        Set<String> entries = sessions.keySet();
        for (String entry : entries) {
            if (System.currentTimeMillis() - sessions.get(entry).lastAccess > 3600000L) { //1 hour
                sessions.remove(entry);
            }
        }
    }

    public static Session getOrCreateSession(String sessionValue) {
        if (sessions.containsKey(sessionValue)) {
            Session session = sessions.get(sessionValue);
            session.lastAccess = System.currentTimeMillis();
            return session;
        }
        gc();
        Session session = new Session();
        sessions.put(sessionValue, session);
        return session;
    }
}
