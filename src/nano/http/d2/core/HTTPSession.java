package nano.http.d2.core;

import nano.http.d2.console.Logger;
import nano.http.d2.consts.Mime;
import nano.http.d2.consts.Status;
import nano.http.d2.core.thread.NanoPool;
import nano.http.d2.core.ws.impl.WebSocketServer;
import nano.http.d2.hooks.HookManager;
import nano.http.d2.serve.ServeProvider;
import nano.http.d2.session.SessionManager;
import nano.http.d2.utils.Encoding;
import nano.http.d2.utils.Misc;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Handles one session, i.e. parses the HTTP request
 * and returns the response.
 */
public class HTTPSession implements Runnable {
    private final Socket mySocket;
    private final ServeProvider myServer;
    public boolean isHighDemand = false;

    public HTTPSession(Socket s, ServeProvider server) {
        mySocket = s;
        myServer = server;
        NanoPool.submit(this);
    }

    public void run() {
        try {
            if (isHighDemand) {
                sendError(Status.HTTP_BADGATEWAY, "Server is busy - NanoHTTP");
            }
            InputStream is = mySocket.getInputStream();
            if (is == null) {
                return;
            }

            // Read the first 8192 bytes.
            // The full header should fit in here.
            // Apache's default header limit is 8KB.
            int bufsize = 8192;
            byte[] buf = new byte[bufsize];
            int rlen = is.read(buf, 0, bufsize);
            if (rlen <= 0) {
                return;
            }

            // Create a BufferedReader for parsing the header.
            ByteArrayInputStream hbis = new ByteArrayInputStream(buf, 0, rlen);
            BufferedReader hin = new BufferedReader(new InputStreamReader(hbis, StandardCharsets.UTF_8));
            Properties pre = new Properties();
            Properties parms = new Properties();
            Properties header = new Properties();
            Properties files = new Properties();

            // Decode the header into parms and header java properties
            decodeHeader(hin, pre, parms, header);
            String method = pre.getProperty("method");
            String uri = pre.getProperty("uri");

            // Logger.info(method + " " + uri + " (" + mySocket.getInetAddress().getHostAddress() + ")");
            // TODO Just a bookmark.
            long size = 0x7FFFFFFFFFFFFFFFL;
            String contentLength = header.getProperty("content-length");
            if (contentLength != null) {
                try {
                    size = Integer.parseInt(contentLength);
                } catch (Exception ignored) {
                }
            }

            // We are looking for the byte separating header from body.
            // It must be the last byte of the first two sequential new lines.
            int splitbyte = 0;
            boolean sbfound = false;
            while (splitbyte < rlen) {
                if (buf[splitbyte] == '\r' && buf[++splitbyte] == '\n' && buf[++splitbyte] == '\r' && buf[++splitbyte] == '\n') {
                    sbfound = true;
                    break;
                }
                splitbyte++;
            }
            splitbyte++;

            // Write the part of body already read to ByteArrayOutputStream f
            ByteArrayOutputStream f = new ByteArrayOutputStream();
            if (splitbyte < rlen) {
                f.write(buf, splitbyte, rlen - splitbyte);
            }

            // While Firefox sends on the first read all the data fitting
            // our buffer, Chrome and Opera sends only the headers even if
            // there is data for the body. So we do some magic here to find
            // out whether we have already consumed part of body, if we
            // have reached the end of the data to be sent or we should
            // expect the first byte of the body at the next read.
            if (splitbyte < rlen) {
                size -= rlen - splitbyte + 1;
            } else if (!sbfound || size == 0x7FFFFFFFFFFFFFFFL) {
                size = 0;
            }

            // Now read all the body and write it to f
            buf = new byte[512];
            while (rlen >= 0 && size > 0) {
                rlen = is.read(buf, 0, 512);
                size -= rlen;
                if (rlen > 0) {
                    f.write(buf, 0, rlen);
                }
            }

            // Get the raw body as a byte []
            byte[] fbuf = f.toByteArray();

            // Create a BufferedReader for easily reading it as string.
            ByteArrayInputStream bin = new ByteArrayInputStream(fbuf);
            String encoding = header.getProperty("content-encoding");
            BufferedReader br = null;
            if (encoding != null) {
                if (encoding.equalsIgnoreCase("gzip")) {
                    br = new BufferedReader(new InputStreamReader(new GZIPInputStream(bin)));
                } else {
                    sendError(Status.HTTP_BADREQUEST, "BAD REQUEST: Content encoding " + encoding + " is not supported. Expected gzip or none.");
                }
            } else {
                br = new BufferedReader(new InputStreamReader(bin));
            }
            assert br != null;

            // If the method is POST, there may be parameters
            // in data section, too, read it:
            if ("POST".equalsIgnoreCase(method)) {
                String contentType = "";
                String contentTypeHeader = header.getProperty("content-type");
                if (contentTypeHeader == null) {
                    sendError(Status.HTTP_BADREQUEST, "BAD REQUEST: Content-type is missing");
                }
                assert contentTypeHeader != null;
                StringTokenizer st = new StringTokenizer(contentTypeHeader, "; ");
                if (st.hasMoreTokens()) {
                    contentType = st.nextToken();
                }
                if ("multipart/form-data".equalsIgnoreCase(contentType)) {
                    // Handle multipart/form-data
                    if (!st.hasMoreTokens()) {
                        sendError(Status.HTTP_BADREQUEST, "BAD REQUEST: Content type is multipart/form-data but boundary missing. Usage: GET /example/file.html");
                    }
                    String boundaryExp = st.nextToken();
                    st = new StringTokenizer(boundaryExp, "=");
                    if (st.countTokens() != 2) {
                        sendError(Status.HTTP_BADREQUEST, "BAD REQUEST: Content type is multipart/form-data but boundary syntax error. Usage: GET /example/file.html");
                    }
                    st.nextToken();
                    String boundary = st.nextToken();

                    decodeMultipartData(boundary, fbuf, br, parms, files, uri);
                } else {
                    // Handle application/x-www-form-urlencoded and application/json
                    StringBuilder postLine = new StringBuilder();
                    char[] pbuf = new char[512];
                    int read = br.read(pbuf);
                    while (read >= 0 && !postLine.toString().endsWith("\r\n")) {
                        postLine.append(String.valueOf(pbuf, 0, read));
                        read = br.read(pbuf);
                    }
                    postLine = new StringBuilder(postLine.toString().trim());
                    decodeParms(postLine.toString(), parms);
                }
            }

            if (WebSocketServer.checkWsProtocol(header, method, mySocket, parms, uri)) {
                return;
            }

            // Ok, now do the serve()
            Response r = HookManager.requestHook.serve(uri, method, header, parms, files, myServer, mySocket.getInetAddress().getHostAddress());
            if (r == null) {
                sendError(Status.HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: Serve() returned a null response.");
            } else {
                if (!header.containsKey("cookie") || !header.getProperty("cookie").contains("session=")) {
                    r.addHeader("Set-Cookie", "session=" + SessionManager.unusedSessionName() + "; path=/");
                }
                sendResponse(r.status, r.mimeType, r.header, r.data);
            }
            br.close();
            mySocket.close();
        } catch (IOException ioe) {
            try {
                sendError(Status.HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            } catch (Throwable ignored) {
            }
        } catch (InterruptedException ie) {
            // Thrown by sendError, ignore and exit the thread.
        }
    }

    /**
     * Decodes the sent headers and loads the data into
     * java Properties' key - value pairs
     */
    private void decodeHeader(BufferedReader in, Properties pre, Properties parms, Properties header)
            throws InterruptedException {
        try {
            // Read the request line
            String inLine = in.readLine();
            if (inLine == null) {
                return;
            }
            StringTokenizer st = new StringTokenizer(inLine);
            if (!st.hasMoreTokens()) {
                sendError(Status.HTTP_BADREQUEST, "BAD REQUEST: Syntax error. Usage: GET /example/file.html");
            }

            String method = st.nextToken();
            pre.put("method", method);

            if (!st.hasMoreTokens()) {
                sendError(Status.HTTP_BADREQUEST, "BAD REQUEST: Missing URI. Usage: GET /example/file.html");
            }

            String uri = st.nextToken();

            // Decode parameters from the URI
            int qmi = uri.indexOf('?');
            if (qmi >= 0) {
                decodeParms(uri.substring(qmi + 1), parms);
                uri = decodePercent(uri.substring(0, qmi));
            } else {
                uri = decodePercent(uri);
            }

            // If there's another token, it's protocol version,
            // followed by HTTP headers. Ignore version but parse headers.
            // NOTE: this now forces header names lowercase since they are
            // case-insensitive and vary by client.
            if (st.hasMoreTokens()) {
                String line = in.readLine();
                while (line != null && line.trim().length() > 0) {
                    int p = line.indexOf(':');
                    if (p >= 0) {
                        header.put(line.substring(0, p).trim().toLowerCase(), line.substring(p + 1).trim());
                    }
                    line = in.readLine();
                }
            }

            pre.put("uri", uri);
        } catch (IOException ioe) {
            sendError(Status.HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
        }
    }

    /**
     * Decodes the Multipart Body data and put it
     * into java Properties' key - value pairs.
     */
    private void decodeMultipartData(String boundary, byte[] fbuf, BufferedReader in, Properties parms, Properties files, String uri)
            throws InterruptedException {
        try {
            int[] bpositions = getBoundaryPositions(fbuf, boundary.getBytes());
            int boundarycount = 1;
            String mpline = in.readLine();
            while (mpline != null) {
                if (!mpline.contains(boundary)) {
                    sendError(Status.HTTP_BADREQUEST, "BAD REQUEST: Content type is multipart/form-data but next chunk does not start with boundary. Usage: GET /example/file.html");
                }
                boundarycount++;
                Properties item = new Properties();
                mpline = in.readLine();
                while (mpline != null && mpline.trim().length() > 0) {
                    int p = mpline.indexOf(':');
                    if (p != -1) {
                        item.put(mpline.substring(0, p).trim().toLowerCase(), mpline.substring(p + 1).trim());
                    }
                    mpline = in.readLine();
                }
                if (mpline != null) {
                    String contentDisposition = item.getProperty("content-disposition");
                    if (contentDisposition == null) {
                        sendError(Status.HTTP_BADREQUEST, "BAD REQUEST: Content type is multipart/form-data but no content-disposition info found. Usage: GET /example/file.html");
                    }
                    assert contentDisposition != null;
                    StringTokenizer st = new StringTokenizer(contentDisposition, "; ");
                    Properties disposition = new Properties();
                    while (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        int p = token.indexOf('=');
                        if (p != -1) {
                            disposition.put(token.substring(0, p).trim().toLowerCase(), token.substring(p + 1).trim());
                        }
                    }
                    String pname = disposition.getProperty("name");
                    pname = pname.substring(1, pname.length() - 1);

                    StringBuilder value = new StringBuilder();
                    if (item.getProperty("content-type") == null) {
                        while (mpline != null && !mpline.contains(boundary)) {
                            mpline = in.readLine();
                            if (mpline != null) {
                                int d = mpline.indexOf(boundary);
                                if (d == -1) {
                                    value.append(mpline);
                                } else {
                                    value.append(mpline, 0, d - 2);
                                }
                            }
                        }
                    } else {
                        if (boundarycount > bpositions.length) {
                            sendError(Status.HTTP_INTERNALERROR, "Error processing request");
                        }
                        int offset = stripMultipartHeaders(fbuf, bpositions[boundarycount - 2]);
                        if (HookManager.fileHook.Accept(pname, uri)) {
                            String path = saveTmpFile(fbuf, offset, bpositions[boundarycount - 1] - offset - 4);
                            files.put(pname, path);
                        }
                        value = new StringBuilder(disposition.getProperty("filename"));
                        value = new StringBuilder(value.substring(1, value.length() - 1));
                        do {
                            mpline = in.readLine();
                        } while (mpline != null && !mpline.contains(boundary));
                    }
                    parms.put(pname, value.toString());
                }
            }
        } catch (IOException ioe) {
            sendError(Status.HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
        }
    }

    /**
     * Find the byte positions where multipart boundaries start.
     */
    public int[] getBoundaryPositions(byte[] b, byte[] boundary) {
        int matchcount = 0;
        int matchbyte = -1;
        Vector<Integer> matchbytes = new Vector<>();
        for (int i = 0; i < b.length; i++) {
            if (b[i] == boundary[matchcount]) {
                if (matchcount == 0) {
                    matchbyte = i;
                }
                matchcount++;
                if (matchcount == boundary.length) {
                    matchbytes.addElement(matchbyte);
                    matchcount = 0;
                    matchbyte = -1;
                }
            } else {
                i -= matchcount;
                matchcount = 0;
                matchbyte = -1;
            }
        }
        int[] ret = new int[matchbytes.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = matchbytes.elementAt(i);
        }
        return ret;
    }

    /**
     * Retrieves the content of a sent file and saves it
     * to a temporary file.
     * The full path to the saved file is returned.
     */
    @SuppressWarnings("IOStreamConstructor")
    private String saveTmpFile(byte[] b, int offset, int len) throws IOException {
        String path = "";
        if (len > 0) {
            if (len > 1024 * 1024 * 10) { // 10MB
                throw new IOException("File too big, sized " + len + " bytes. Expected less than 10MB.");
            }
            String tmpdir = System.getProperty("java.io.tmpdir");
            try {
                File temp = File.createTempFile("NanoHTTPd", "", new File(tmpdir));
                OutputStream fstream = new FileOutputStream(temp);
                fstream.write(b, offset, len);
                fstream.close();
                path = temp.getAbsolutePath();
            } catch (Exception e) { // Catch exception if any
                throw new IOException(e.getMessage());
            }
        }
        return path;
    }

    /**
     * It returns the offset separating multipart file headers
     * from the file's data.
     */
    private int stripMultipartHeaders(byte[] b, int offset) {
        int i;
        for (i = offset; i < b.length; i++) {
            if (b[i] == '\r' && b[++i] == '\n' && b[++i] == '\r' && b[++i] == '\n') {
                break;
            }
        }
        return i + 1;
    }

    /**
     * Decodes the percent encoding scheme. <br/>
     * For example: "an+example%20string" -> "an example string"
     * Modified - added support for Chinese characters
     */
    private String decodePercent(String str) {
        return Encoding.deURL(str);
    }

    /**
     * Decodes parameters in percent-encoded URI-format
     * ( e.g. "name=Jack%20Daniels&pass=Single%20Malt" ) and
     * adds them to given Properties. NOTE: this doesn't support multiple
     * identical keys due to the simplicity of Properties -- if you need multiples,
     * you might want to replace the Properties with a Hashtable of Vectors or such.
     */
    private void decodeParms(String parms, Properties p) {
        if (parms == null) {
            return;
        }
        if (parms.startsWith("{")) {
            p.put("json", parms);
            return;
        }
        StringTokenizer st = new StringTokenizer(parms, "&");
        while (st.hasMoreTokens()) {
            String e = st.nextToken();
            int sep = e.indexOf('=');
            String dec = decodePercent((sep >= 0) ? e.substring(0, sep) : e);
            if (dec != null) {
                p.put(dec.trim(), (sep >= 0) ? decodePercent(e.substring(sep + 1)) : "");
            }
        }
    }

    /**
     * Returns an error message as a HTTP response and
     * throws InterruptedException to stop further request processing.
     */
    private void sendError(String status, String msg) throws InterruptedException {
        sendResponse(status, Mime.MIME_PLAINTEXT, null, new ByteArrayInputStream(msg.getBytes()));
        throw new InterruptedException();
    }

    /**
     * Sends given response to the socket.
     */
    private void sendResponse(String status, String mime, Properties header, InputStream data) {
        try {
            if (status == null) {
                throw new Error("sendResponse(): Status can't be null.");
            }

            OutputStream out = mySocket.getOutputStream();
            PrintWriter pw = new PrintWriter(out);
            pw.print("HTTP/1.1 " + status + " \r\n");

            if (mime != null) {
                pw.print("Content-Type: " + mime + "\r\n");
            }

            if (header == null || header.getProperty("Date") == null) {
                pw.print("Date: " + Misc.gmtFrmt.format(new Date()) + "\r\n");
            }

            if (header != null) {
                Enumeration<Object> e = header.keys();
                while (e.hasMoreElements()) {
                    String key = (String) e.nextElement();
                    String value = header.getProperty(key);
                    pw.print(key + ": " + value + "\r\n");
                }
            }

            pw.print("\r\n");
            pw.flush();

            if (data != null) {
                byte[] buff = new byte[2048];
                while (true) {
                    int read = data.read(buff, 0, 2048);
                    if (read <= 0) {
                        break;
                    }
                    out.write(buff, 0, read);
                }
            }
            out.flush();
            out.close();
            if (data != null) {
                data.close();
            }
        } catch (IOException ioe) {
            // Couldn't write? No can do.
            try {
                mySocket.close();
            } catch (Exception ignored) {
            }
        }
    }
}
