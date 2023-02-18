package nano.http.bukkit.internal;

import nano.http.d2.console.Logger;
import nano.http.d2.consts.Mime;
import nano.http.d2.consts.Status;
import nano.http.d2.core.Response;
import nano.http.d2.serve.ServeProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Bukkit_Router implements ServeProvider {
    public final List<Bukkit_Node> nodes = new ArrayList<>();

    public void load(File dir) {
        try {
            if (!dir.exists()) {
                throw new FileNotFoundException("Directory not found.");
            }
            if (!dir.isDirectory()) {
                throw new FileNotFoundException("Not a directory : " + dir.getName() + " Do not place files in the plugin folder!");
            }
            File desc = new File(dir, "plugin.properties");
            if (!desc.exists()) {
                throw new FileNotFoundException("plugin.properties not found.");
            }
            Properties pr = new Properties();
            pr.load(new FileReader(desc));

            if (!pr.containsKey("main")) {
                throw new UnsupportedOperationException("plugin.properties does not contain main class.");
            }
            if (!pr.containsKey("jar")) {
                throw new UnsupportedOperationException("plugin.properties does not contain jar.");
            }
            if (!pr.containsKey("uri")) {
                throw new UnsupportedOperationException("plugin.properties does not contain uri.");
            }
            if (!pr.containsKey("name")) {
                throw new UnsupportedOperationException("plugin.properties does not contain name.");
            }

            File jar = new File(dir, pr.getProperty("jar"));
            if (!jar.exists()) {
                throw new FileNotFoundException("Jar not found.");
            }
            ClassLoader pcl = new URLClassLoader(new URL[]{jar.toURI().toURL()});
            addNode(pr.getProperty("uri"), pcl, pr.getProperty("main"), pr.getProperty("name"), dir);
            Logger.info("Plugin " + pr.getProperty("name") + " loaded successfully.");
        } catch (Exception e) {
            Logger.error("Plugin loaded unsuccessfully due to an exception.", e);
        }
    }

    private void addNode(String uri, ClassLoader classLoader, String classPath, String name, File dir) throws Exception {
        if (uri.length() < 3) {
            throw new UnsupportedOperationException("URI must be at least 3 characters long.");
        }
        for (Bukkit_Node node : nodes) {
            if (node.uri.equals(uri)) {
                throw new UnsupportedOperationException("Node already exists:" + uri);
            }
            if (node.name.equals(name)) {
                throw new UnsupportedOperationException("Plugin already loaded:" + name);
            }
        }
        Bukkit_Node pre_node = new Bukkit_Node(uri, classLoader, classPath, name);
        pre_node.onEnable(name, dir, uri);
        nodes.add(pre_node);
    }

    @Override
    public Response serve(String uri, String method, Properties header, Properties parms, Properties files) {
        Bukkit_Node best = null;
        for (Bukkit_Node node : nodes) {
            if (uri.startsWith(node.uri)) {
                if (best != null) {
                    if (best.uri.length() > node.uri.length()) {
                        continue;
                    }
                }
                best = node;
            }
        }
        Response response;
        if (best != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(uri);
            sb.delete(0, best.uri.length());
            response = best.serve(sb.toString(), method, header, parms, files);
        } else {
            response = null;
        }
        if (response != null) {
            return response;
        }
        for (Bukkit_Node node : nodes) {
            response = node.fallback(uri, method, header, parms, files);
            if (response != null) {
                return response;
            }
        }
        return new Response(Status.HTTP_NOTFOUND, Mime.MIME_PLAINTEXT, "404 Not Found - BukkitHTTP");
    }

    public boolean removeNode(String name) {
        for (Bukkit_Node node : nodes) {
            if (node.name.equals(name)) {
                try {
                    node.onDisable();
                } catch (Exception e) {
                    Logger.error("Error while disabling plugin. Enforcing clean-up.", e);
                }
                try {
                    if (node.classLoader instanceof URLClassLoader) {
                        ((URLClassLoader) node.classLoader).close();
                    }
                } catch (Exception e) {
                    Logger.error("Error while closing plugin classloader. Enforcing un-registration.", e);
                }
                nodes.remove(node);
                System.gc();
                return true;
            }
        }
        return false;
    }
}

