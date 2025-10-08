package nano.http.d2.ai.mcp;

import nano.http.d2.json.NanoJSON;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class SSERPC {
    private final URL addr;
    private final Scanner scanner;
    private final int[] idh = {114513};
    private boolean closed = false;

    public SSERPC(String addr) throws IOException {
        try {
            URL u = new URL(addr);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "text/event-stream");
            conn.setDoOutput(true);
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("HTTP error code: " + responseCode);
            }

            InputStream is = conn.getInputStream();
            scanner = new Scanner(is, StandardCharsets.UTF_8);
            this.addr = new URL(u, nextEvent(false));
        } catch (Throwable t) {
            throw new IOException(t);
        }
    }

    public NanoJSON sendJsonRPCAndWaitForResp(String method, NanoJSON params) throws IOException {
        sendJsonRPCUnsafe(method, params, true);
        while (true) {
            String response = nextEvent();
            if (response == null) {
                throw new IOException("No response from server");
            }
            try {
                NanoJSON json = new NanoJSON(response);
                if (json.getInt("id") == idh[0]) {
                    return json.getJSONObject("result");
                }
            } catch (Throwable t) {
                return null;
            }
        }
    }

    private String nextEvent() throws IOException {
        return nextEvent(true);
    }

    private String nextEvent(boolean json) throws IOException {
        if (closed) {
            throw new IOException("SSE is closed");
        }
        try {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("data: {") || (!json && line.startsWith("data: "))) {
                    return line.substring(6).trim();
                }
            }
            close();
            return null;
        } catch (Throwable t) {
            close();
            throw new IOException(t);
        }
    }


    public void sendJsonRPCUnsafe(String method, NanoJSON params, boolean id) throws IOException {
        if (closed) {
            throw new IOException("SSE is closed");
        }
        NanoJSON rpc = new NanoJSON();
        rpc.put("jsonrpc", "2.0");
        rpc.put("method", method);
        if (params != null) {
            rpc.put("params", params);

        }
        if (id) {
            rpc.put("id", ++idh[0]);
        }
        String rpcStr = rpc.toString();
        try {
            HttpURLConnection conn = (HttpURLConnection) addr.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.getOutputStream().write(rpcStr.getBytes(StandardCharsets.UTF_8));
            int responseCode = conn.getResponseCode();
            if (!(responseCode >= 200 && responseCode < 300)) {
                throw new IOException("HTTP error code: " + responseCode);
            }
        } catch (Throwable t) {
            close();
            throw new IOException(t);
        }
    }

    public void close() {
        if (!closed) {
            scanner.close();
            closed = true;
        }
    }
}
