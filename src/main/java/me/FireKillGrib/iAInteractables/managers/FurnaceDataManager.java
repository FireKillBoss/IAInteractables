package me.FireKillGrib.iAInteractables.managers;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FurnaceDataManager {
    private final File dataFolder;
    public FurnaceDataManager(File dataFolder) {
        this.dataFolder = dataFolder;
        new File(dataFolder, "furnaces").mkdirs();
    }
    public void save(Location location, VirtualInventory inventory, int cookingProgress) {
        YamlConfiguration config = new YamlConfiguration();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                config.set("items." + i, item);
            }
        }
        config.set("cooking-progress", cookingProgress);
    }
    public Map<String, Object> load(Location location) {
        File file = new File(dataFolder, "furnaces/" + locationToString(location) + ".yml");
        if (!file.exists()) {
            return null;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        Map<String, Object> data = new HashMap<>();
        Map<Integer, ItemStack> items = new HashMap<>();
        if (config.contains("items")) {
            for (String key : config.getConfigurationSection("items").getKeys(false)) {
                items.put(Integer.parseInt(key), config.getItemStack("items." + key));
            }
        }
        data.put("items", items);
        data.put("cooking-progress", config.getInt("cooking-progress", 0));
        return data;
    }
    public void delete(Location location) {
    }
    private String locationToString(Location loc) {
        return loc.getWorld().getName() + "_" + 
                loc.getBlockX() + "_" + 
                loc.getBlockY() + "_" + 
                loc.getBlockZ();
    }
}
