package nano.http.bukkit.test;

import nano.http.bukkit.api.BukkitServerProvider;
import nano.http.bukkit.api.debug.DebugMain;
import nano.http.d2.core.Response;

import java.io.File;
import java.util.Properties;

public class Test extends BukkitServerProvider {
    public static void main(String[] args) throws Exception {
        DebugMain.debug(Test.class, "a");
    }

    @Override
    public void onEnable(String name, File dir, String uri) {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public Response serve(String uri, String method, Properties header, Properties parms, Properties files) {
        return null;
    }

    @Override
    public Response fallback(String uri, String method, Properties header, Properties parms, Properties files) {
        return new Response(new Bean("John Smith", 22));
    }
}
