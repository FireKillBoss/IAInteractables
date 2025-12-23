package me.FireKillGrib.iAInteractables.managers;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FurnaceDataManager {
    private final File dataFolder;
    public FurnaceDataManager(File pluginFolder) {
        this.dataFolder = new File(pluginFolder, "furnaces_data");
        dataFolder.mkdirs();
    }
    public void save(Location location, VirtualInventory inventory, int cookingProgress) {
        File file = getFile(location);
        YamlConfiguration config = new YamlConfiguration();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                config.set("items." + i, item);
            }
        }
        config.set("cooking-progress", cookingProgress);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Map<String, Object> load(Location location) {
        File file = getFile(location);
        if (!file.exists()) {
            return null;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        Map<String, Object> data = new HashMap<>();
        Map<Integer, ItemStack> items = new HashMap<>();
        if (config.contains("items")) {
            for (String key : config.getConfigurationSection("items").getKeys(false)) {
                int slot = Integer.parseInt(key);
                ItemStack item = config.getItemStack("items." + key);
                items.put(slot, item);
            }
        }
        data.put("items", items);
        data.put("cooking-progress", config.getInt("cooking-progress", 0));
        return data;
    }
    private File getFile(Location loc) {
        String name = loc.getWorld().getName() + "_" +
                    loc.getBlockX() + "_" +
                    loc.getBlockY() + "_" +
                    loc.getBlockZ() + ".yml";
        return new File(dataFolder, name);
    }
}
