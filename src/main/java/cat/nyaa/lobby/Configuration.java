package cat.nyaa.lobby;

import cat.nyaa.nyaacore.configuration.PluginConfigure;
import org.bukkit.plugin.java.JavaPlugin;

public class Configuration extends PluginConfigure {

    @Serializable
    public String language = "en_US";
    @Serializable(name = "team_limit")
    public int teamLimit;


    @Override
    protected JavaPlugin getPlugin() {
        return LobbyPlugin.plugin;
    }
}
