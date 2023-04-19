package nano.http.d2.serve;

import nano.http.d2.consts.Mime;
import nano.http.d2.consts.Status;
import nano.http.d2.core.Response;
import nano.http.d2.utils.Misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class FileServer {
    public static Response serveFile(String uri, Properties header, File homeDir,
                                     boolean allowDirectoryListing) {
        return serveFile(uri, "", header, homeDir, allowDirectoryListing);
    }

    /**
     * Serves file from homeDir and its subdirectories (only).
     * Uses only URI, ignores all headers and HTTP parameters.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static Response serveFile(String uri, String preUri, Properties header, File homeDir,
                                     boolean allowDirectoryListing) {
        // Make sure we won't die of an exception later
        if (!homeDir.isDirectory()) {
            return new Response(Status.HTTP_INTERNALERROR, Mime.MIME_PLAINTEXT, "INTERNAL ERRROR: serveFile(): given homeDir is not a directory.");
        }

        // Remove URL arguments
        uri = uri.trim().replace(File.separatorChar, '/');
        if (uri.indexOf('?') >= 0) {
            uri = uri.substring(0, uri.indexOf('?'));
        }

        // Prohibit getting out of current directory
        if (uri.contains("..")) {
            return new Response(Status.HTTP_FORBIDDEN, Mime.MIME_PLAINTEXT, "FORBIDDEN: You know why.");
        }
        File f = new File(homeDir, uri);
        if (!f.exists()) {
            return new Response(Status.HTTP_NOTFOUND, Mime.MIME_PLAINTEXT, "Error 404, file not found.");
        }
        // List the directory, if necessary
        if (f.isDirectory()) {
            String fullUri = preUri + uri;
            // Browsers get confused without '/' after the
            // directory, send a redirect.
            if (!fullUri.endsWith("/")) {
                fullUri += "/";
                Response r = new Response(Status.HTTP_REDIRECT, Mime.MIME_HTML,
                        "<html><body>Redirected: <a href=\"" + fullUri + "\">" +
                                fullUri + "</a></body></html>");
                r.addHeader("Location", fullUri);
                return r;
            }

            // First try index.html and index.htm
            if (new File(f, "index.html").exists()) {
                f = new File(homeDir, uri + "/index.html");
            } else if (new File(f, "index.htm").exists()) {
                f = new File(homeDir, uri + "/index.htm");
            } else if (allowDirectoryListing) {
                // No index file, list the directory
                String[] files = f.list();
                StringBuilder msg = new StringBuilder("<html><body><h1>Directory " + uri + "</h1><br/>");

                if (uri.length() > 1) {
                    String u = fullUri.substring(0, fullUri.length() - 1);
                    int slash = u.lastIndexOf('/');
                    if (slash >= 0) {
                        msg.append("<b><a href=\"").append(fullUri, 0, slash + 1).append("\">..</a></b><br/>");
                    }
                }

                if (files != null) {
                    for (int i = 0; i < files.length; ++i) {
                        File curFile = new File(f, files[i]);
                        boolean dir = curFile.isDirectory();
                        if (dir) {
                            msg.append("<b>");
                            files[i] += "/";
                        }

                        msg.append("<a href=\"").append(Misc.encodeUri(fullUri + files[i])).append("\">").append(files[i]).append("</a>");

                        // Show file size
                        if (curFile.isFile()) {
                            long len = curFile.length();
                            msg.append(" &nbsp;<font size=2>(");
                            if (len < 1024) {
                                msg.append(len).append(" bytes");
                            } else if (len < 1024 * 1024) {
                                msg.append(len / 1024).append(".").append(len % 1024 / 10 % 100).append(" KB");
                            } else {
                                msg.append(len / (1024 * 1024)).append(".").append(len % (1024 * 1024) / 10 % 100).append(" MB");
                            }

                            msg.append(")</font>");
                        }
                        msg.append("<br/>");
                        if (dir) {
                            msg.append("</b>");
                        }
                    }
                }
                msg.append("<br/>Powered by NanoHTTPd2</body></html>");
                return new Response(Status.HTTP_OK, Mime.MIME_HTML, msg.toString());
            } else {
                return new Response(Status.HTTP_FORBIDDEN, Mime.MIME_PLAINTEXT, "FORBIDDEN: No directory listing.");
            }
        }

        try {
            // Get MIME type from file name extension, if possible
            String mime = null;
            int dot = f.getCanonicalPath().lastIndexOf('.');
            if (dot >= 0) {
                mime = Misc.theMimeTypes.get(f.getCanonicalPath().substring(dot + 1).toLowerCase());
            }
            if (mime == null) {
                mime = Mime.MIME_DEFAULT_BINARY;
            }

            // Support (simple) skipping:
            long startFrom = 0;
            String range = header.getProperty("range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    if (minus > 0) {
                        range = range.substring(0, minus);
                    }
                    try {
                        startFrom = Long.parseLong(range);
                    } catch (Exception ignored) {
                    }
                }
            }

            FileInputStream fis = new FileInputStream(f);
            fis.skip(startFrom);
            Response r = new Response(Status.HTTP_OK, mime, fis);
            r.addHeader("Content-length", String.valueOf(f.length() - startFrom));
            r.addHeader("Content-range", startFrom + "-" +
                    (f.length() - 1) + "/" + f.length());
            return r;
        } catch (IOException ioe) {
            return new Response(Status.HTTP_FORBIDDEN, Mime.MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
        }
    }
}
