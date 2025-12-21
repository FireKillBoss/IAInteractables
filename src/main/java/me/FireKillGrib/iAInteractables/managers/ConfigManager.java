package me.FireKillGrib.iAInteractables.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import me.FireKillGrib.iAInteractables.Plugin;
import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private FileConfiguration config;
    private File configFile;
    public ConfigManager() {
        loadConfig();
    }
    public void loadConfig() {
        configFile = new File(Plugin.getInstance().getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            Plugin.getInstance().saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }
    public FileConfiguration getConfig() {
        return config;
    }
    public void reload() {
        loadConfig();
    }
    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
