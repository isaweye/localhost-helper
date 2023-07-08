package uk.mqchinee.localhosthelper.utils;

import com.rylinaux.plugman.PlugMan;
import com.rylinaux.plugman.util.PluginUtil;
import org.bukkit.plugin.Plugin;

public class PluginUtils {
    private final PluginUtil util;

    public PluginUtils() {
        this.util = PlugMan.getInstance().getPluginUtil();
    }

    public void reload(String plugin) {
        Plugin pl = util.getPluginByName(plugin);
        util.reload(pl); // reload
    }

    public void disable(String plugin) {
        Plugin pl = util.getPluginByName(plugin);
        util.disable(pl); // disable
    }

}
