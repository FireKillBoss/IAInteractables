package me.FireKillGrib.iAInteractables.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import me.FireKillGrib.iAInteractables.Plugin;
import me.FireKillGrib.iAInteractables.data.FurnaceInstance;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InstanceManager {
    private final Map<String, FurnaceInstance> instances = new ConcurrentHashMap<>();
    private final File dataFile;
    public InstanceManager() {
        dataFile = new File(Plugin.getInstance().getDataFolder(), "instances.yml");
        load();
    }
    public FurnaceInstance getFurnaceInstance(Location loc, String furnaceType) {
        String key = locationKey(loc);
        return instances.computeIfAbsent(key, k -> new FurnaceInstance(loc, furnaceType));
    }
    public void removeInstance(Location loc) {
        instances.remove(locationKey(loc));
    }
    private String locationKey(Location loc) {
        return loc.getWorld().getName() + ";" + 
            loc.getBlockX() + ";" + 
            loc.getBlockY() + ";" + 
            loc.getBlockZ();
    }
    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        int index = 0;
        for (FurnaceInstance instance : instances.values()) {
            ConfigurationSection section = config.createSection("instances." + index);
            instance.save(section);
            index++;
        }
        try {
            config.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void load() {
        if (!dataFile.exists()) return;
        instances.clear();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection instancesSection = config.getConfigurationSection("instances");
        if (instancesSection == null) return;
        for (String key : instancesSection.getKeys(false)) {
            ConfigurationSection section = instancesSection.getConfigurationSection(key);
            if (section == null) continue;
            String worldName = section.getString("world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) continue;
            Location loc = new Location(
                world,
                section.getInt("x"),
                section.getInt("y"),
                section.getInt("z")
            );
            String type = section.getString("type");
            UUID id = UUID.fromString(section.getString("id"));
            FurnaceInstance instance = new FurnaceInstance(loc, type, id);
            instance.setCookProgress(section.getInt("cookProgress", 0));
            instance.setCooking(section.getBoolean("isCooking", false));
            ConfigurationSection slotsSection = section.getConfigurationSection("slots");
            if (slotsSection != null) {
                for (String slotKey : slotsSection.getKeys(false)) {
                    ItemStack item = slotsSection.getItemStack(slotKey);
                    instance.setSlot(slotKey.charAt(0), item);
                }
            }
            ItemStack result = section.getItemStack("result");
            if (result != null) {
                instance.setResult(result);
            }
            instances.put(locationKey(loc), instance);
        }
    }
    public Collection<FurnaceInstance> getAllInstances() {
        return instances.values();
    }
}
