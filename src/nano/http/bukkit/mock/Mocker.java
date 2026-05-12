package nano.http.bukkit.mock;

import org.bukkit.plugin.java.JavaPlugin;

public class Mocker extends JavaPlugin {
    @Override
    public void onLoad() {
        InjectedHandler.debug = true;
        InjectRegistry.doInject();
    }
}
