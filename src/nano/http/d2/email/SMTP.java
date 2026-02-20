package nano.http.d2.email;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SMTP {
    private static final Map<String, String> MX_CACHE = new ConcurrentHashMap<>();

    public static boolean sendPlain(String from, String to, String subject, String body) {
        String domain = to.substring(to.indexOf('@') + 1);
        String mxHosts = MX_CACHE.computeIfAbsent(domain, SMTP::lookupMx);
        return trySend(mxHosts, from, to, subject, body);
    }

    private static String lookupMx(String domain) {
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            DirContext ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes("dns:/" + domain, new String[]{"MX"});
            Attribute attr = attrs.get("MX");
            if (attr == null) throw new IllegalStateException();
            List<MX> list = new ArrayList<>();
            for (int i = 0; i < attr.size(); i++) {
                String[] parts = attr.get(i).toString().split("\\s+");
                if (parts.length == 2) list.add(new MX(Integer.parseInt(parts[0]), parts[1].endsWith(".") ? parts[1].substring(0, parts[1].length() - 1) : parts[1]));
            }
            list.sort(Comparator.comparingInt(a -> a.priority));
            List<String> hosts = new ArrayList<>();
            for (MX mx : list) hosts.add(mx.host);
            return hosts.getFirst();
        } catch (Throwable t) {
            return domain;
        }
    }

    private static boolean trySend(String host, String from, String to, String subject, String body) {
        try (Socket socket = new Socket(host, 25); BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)); PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {
            if (i(in, 220)) return false;
            o(out, "HELO BukkitHTTP");
            if (i(in, 250)) return false;
            o(out, "MAIL FROM:<" + from + ">");
            if (i(in, 250)) return false;
            o(out, "RCPT TO:<" + to + ">");
            if (i(in, 250, 251)) return false;
            o(out, "DATA");
            if (i(in, 354)) return false;
            out.printf("From: <%s>\r\nTo: <%s>\r\nSubject: %s\r\n\r\n%s\r\n.\r\n", from, to, subject, body);
            if (i(in, 250)) return false;
            o(out, "QUIT");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void o(PrintWriter out, String s) {
        out.print(s + "\r\n");
        out.flush();
    }

    private static boolean i(BufferedReader in, int... codes) throws IOException {
        String line;
        int code;
        do {
            line = in.readLine();
            if (line == null || line.length() < 3) return true;
            code = Integer.parseInt(line.substring(0, 3));
        } while (line.length() >= 4 && line.charAt(3) == '-');
        for (int c : codes) if (code == c) return false;
        return true;
    }

    private record MX(int priority, String host) {
    }
}
