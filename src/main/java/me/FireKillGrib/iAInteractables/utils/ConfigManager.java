package me.FireKillGrib.iAInteractables.utils;

import java.io.File;
import me.FireKillGrib.iAInteractables.Plugin;

public class ConfigManager {
    public static void createFolder(String name) {
        File pluginFolder = Plugin.getInstance().getDataFolder();
        File targetFolder = new File(pluginFolder, name);
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
            Plugin.getInstance().saveResource(name+"/default.yml", false);
        }
    }
}
