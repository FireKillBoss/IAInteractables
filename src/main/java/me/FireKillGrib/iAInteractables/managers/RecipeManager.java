package me.FireKillGrib.iAInteractables.managers;

import me.FireKillGrib.iAInteractables.Plugin;
import me.FireKillGrib.iAInteractables.data.*;
import me.FireKillGrib.iAInteractables.utils.ItemBuilder;
import me.FireKillGrib.iAInteractables.utils.ItemsBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.item.ItemProvider;
import java.io.File;
import java.util.*;

public class RecipeManager {
    private final List<Furnace> furnaces = new ArrayList<>();
    private final List<Workbench> workbenches = new ArrayList<>();
    public void loadFurnaces() {
    furnaces.clear();
    File folder = new File(Plugin.getInstance().getDataFolder(), "furnaces");
    if (!folder.exists()) return;
    File[] files = folder.listFiles((d, name) -> name.endsWith(".yml"));
    if (files == null) return;
    for (File file : files) {
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        String name = file.getName().replace(".yml", "");
        String title = cfg.getString("title", "&cName is not defined");
        List<String> structure = cfg.getStringList("structure");
        ConfigurationSection fillerSec = cfg.getConfigurationSection("filler");
        ItemProvider filler;
        if (fillerSec != null) {
            String materialStr = fillerSec.getString("material", "BLACK_STAINED_GLASS_PANE");
            String displayName = fillerSec.getString("name", "");
            List<String> lore = fillerSec.getStringList("lore");
            ItemStack fillerItem;
            if (materialStr.startsWith("ia-") || materialStr.contains(":")) {
                fillerItem = new ItemsBuilder(materialStr).build();
            } else {
                try {
                    fillerItem = new ItemStack(Material.valueOf(materialStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    Plugin.getInstance().getLogger().warning(
                        "Unknown material '" + materialStr + "' in file " + file.getName()
                    );
                    fillerItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                }
            }
            ItemBuilder builder = new ItemBuilder(fillerItem);
            if (displayName != null && !displayName.isEmpty()) {
                builder.setDisplayName(displayName);
            }
            if (lore != null && !lore.isEmpty()) {
                builder.setLore(lore);
            }
            filler = builder.getItemProvider();
            
        } else {
            filler = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setDisplayName("")
                .getItemProvider();
        }
        Set<FurnaceRecipe> recipes = new HashSet<>();
        ConfigurationSection recipesSec = cfg.getConfigurationSection("recipes");
        if (recipesSec != null) {
            for (String key : recipesSec.getKeys(false)) {
                ConfigurationSection s = recipesSec.getConfigurationSection(key);
                if (s == null) continue;
                int cookTime = s.getInt("cook-time", 100);
                ConfigurationSection resultSec = s.getConfigurationSection("result");
                ItemStack result = null;
                if (resultSec != null) {
                    String material = resultSec.getString("material");
                    int amount = resultSec.getInt("amount", 1);
                    if (material != null) {
                        try {
                            if (material.startsWith("ia-") || material.contains(":")) {
                                result = new ItemsBuilder(material).build();
                                result.setAmount(amount);
                            } else {
                                result = new ItemStack(Material.valueOf(material.toUpperCase()), amount);
                            }
                        } catch (IllegalArgumentException e) {
                            Plugin.getInstance().getLogger().warning(
                                "Unknown result material '" + material + 
                                "' in recept " + key + " file " + file.getName()
                            );
                        }
                    }
                }
                if (result == null) continue;
                Map<Character, Set<ItemStack>> raws = new HashMap<>();
                Map<Character, Set<ItemStack>> fuels = new HashMap<>();
                ConfigurationSection rawsSec = s.getConfigurationSection("raws");
                if (rawsSec != null) {
                    for (String slotKey : rawsSec.getKeys(false)) {
                        char ch = slotKey.charAt(0);
                        Set<ItemStack> set = new HashSet<>();
                        List<String> materials = rawsSec.isList(slotKey) 
                            ? rawsSec.getStringList(slotKey) 
                            : Collections.singletonList(rawsSec.getString(slotKey));
                        for (String mat : materials) {
                            if (mat == null || mat.isEmpty()) continue;
                            try {
                                if (mat.startsWith("ia-") || mat.contains(":")) {
                                    set.add(new ItemsBuilder(mat).build());
                                } else {
                                    set.add(new ItemStack(Material.valueOf(mat.toUpperCase())));
                                }
                            } catch (IllegalArgumentException e) {
                                Plugin.getInstance().getLogger().warning(
                                    "Unknown material '" + mat + 
                                    "' in raws." + slotKey + " recept " + key + " file " + file.getName()
                                );
                            }
                        }
                        if (!set.isEmpty()) {
                            raws.put(ch, set);
                        }
                    }
                }
                ConfigurationSection fuelsSec = s.getConfigurationSection("fuels");
                if (fuelsSec != null) {
                    for (String slotKey : fuelsSec.getKeys(false)) {
                        char ch = slotKey.charAt(0);
                        Set<ItemStack> set = new HashSet<>();
                        List<String> materials = fuelsSec.isList(slotKey) 
                            ? fuelsSec.getStringList(slotKey) 
                            : Collections.singletonList(fuelsSec.getString(slotKey));
                        for (String mat : materials) {
                            if (mat == null || mat.isEmpty()) continue;
                            try {
                                if (mat.startsWith("ia-") || mat.contains(":")) {
                                    set.add(new ItemsBuilder(mat).build());
                                } else {
                                    set.add(new ItemStack(Material.valueOf(mat.toUpperCase())));
                                }
                            } catch (IllegalArgumentException e) {
                                Plugin.getInstance().getLogger().warning(
                                    "Unknown material '" + mat + 
                                    "' in fuels." + slotKey + " recept " + key + " file " + file.getName()
                                );
                            }
                        }
                        if (!set.isEmpty()) {
                            fuels.put(ch, set);
                        }
                    }
                }
                recipes.add(new FurnaceRecipe(result, cookTime, raws, fuels));
            }
        }
            FurnaceEffects effects = FurnaceEffects.fromConfig(cfg.getConfigurationSection("effects"));
            furnaces.add(new Furnace(name, title, structure, filler, recipes, effects));
        }
    }
    public void loadWorkbenches() {
    workbenches.clear();
    File folder = new File(Plugin.getInstance().getDataFolder(), "workbenches");
    if (!folder.exists()) return;
    File[] files = folder.listFiles((d, name) -> name.endsWith(".yml"));
    if (files == null) return;
    for (File file : files) {
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        String name = file.getName().replace(".yml", "");
        String title = cfg.getString("title", "&cName is not defined");
        List<String> structure = cfg.getStringList("structure");
        ConfigurationSection fillerSec = cfg.getConfigurationSection("filler");
        ItemProvider filler;
        if (fillerSec != null) {
            String materialStr = fillerSec.getString("material", "BLACK_STAINED_GLASS_PANE");
            String displayName = fillerSec.getString("name", "");
            List<String> lore = fillerSec.getStringList("lore");
            ItemStack fillerItem;
            if (materialStr.startsWith("ia-") || materialStr.contains(":")) {
                fillerItem = new ItemsBuilder(materialStr).build();
            } else {
                try {
                    fillerItem = new ItemStack(Material.valueOf(materialStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    Plugin.getInstance().getLogger().warning(
                        "Unknown material '" + materialStr + "' in file " + file.getName()
                    );
                    fillerItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                }
            }
            ItemBuilder builder = new ItemBuilder(fillerItem);
            if (displayName != null && !displayName.isEmpty()) {
                builder.setDisplayName(displayName);
            }
            if (lore != null && !lore.isEmpty()) {
                builder.setLore(lore);
            }
            filler = builder.getItemProvider();
        } else {
            filler = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setDisplayName("")
                .getItemProvider();
            }
            Set<WorkbenchRecipe> recipes = new HashSet<>();
            ConfigurationSection recipesSec = cfg.getConfigurationSection("recipes");
            if (recipesSec != null) {
                for (String key : recipesSec.getKeys(false)) {
                    ConfigurationSection s = recipesSec.getConfigurationSection(key);
                    if (s == null) continue;
                    ConfigurationSection resultSec = s.getConfigurationSection("result");
                    ItemStack result = null;
                    if (resultSec != null) {
                        String material = resultSec.getString("material");
                        int amount = resultSec.getInt("amount", 1);
                        if (material != null) {
                            try {
                                if (material.startsWith("ia-") || material.contains(":")) {
                                    result = new ItemsBuilder(material).build();
                                    result.setAmount(amount);
                                } else {
                                    result = new ItemStack(Material.valueOf(material.toUpperCase()), amount);
                                }
                            } catch (IllegalArgumentException e) {
                                Plugin.getInstance().getLogger().warning(
                                    "Unknown material result '" + material + 
                                    "' in recept " + key + " file " + file.getName()
                                );
                            }
                        }
                    }
                    if (result == null) continue;
                    Map<Integer, ItemStack> ingredients = new HashMap<>();
                    for (String slotKey : s.getKeys(false)) {
                        if (slotKey.equals("result")) continue;
                        try {
                            int slot = Integer.parseInt(slotKey);
                            ConfigurationSection ingredientSec = s.getConfigurationSection(slotKey);
                            if (ingredientSec != null) {
                                String material = ingredientSec.getString("material");
                                int amount = ingredientSec.getInt("amount", 1);
                                if (material != null) {
                                    try {
                                        ItemStack item;
                                        if (material.startsWith("ia-") || material.contains(":")) {
                                            item = new ItemsBuilder(material).build();
                                        } else {
                                            item = new ItemStack(Material.valueOf(material.toUpperCase()));
                                        }
                                        item.setAmount(amount);
                                        ingredients.put(slot, item);
                                    } catch (IllegalArgumentException e) {
                                        Plugin.getInstance().getLogger().warning(
                                            "Unknown material '" + material + 
                                            "' in slot " + slot + " recept " + key + " file " + file.getName()
                                        );
                                    }
                                }
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    recipes.add(new WorkbenchRecipe(result, ingredients));
                }
            }
            WorkbenchEffects effects = WorkbenchEffects.fromConfig(cfg.getConfigurationSection("effects"));
            workbenches.add(new Workbench(name, title, structure, filler, recipes, effects));
        }
    }
    public List<Furnace> getFurnaces() {
        return furnaces;
    }
    public List<Workbench> getWorkbenches() {
        return workbenches;
    }
    public Furnace getFurnace(String name) {
        return furnaces.stream()
            .filter(f -> f.getName().equals(name))
            .findFirst()
            .orElse(null);
    }
    public Workbench getWorkbench(String name) {
        return workbenches.stream()
            .filter(w -> w.getName().equals(name))
            .findFirst()
            .orElse(null);
    }
    public void clearAll() {
        furnaces.clear();
        workbenches.clear();
    }
    public void reload() {
        clearAll();
        loadFurnaces();
        loadWorkbenches();
    }
}