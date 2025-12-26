package me.FireKillGrib.iAInteractables.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class ProgressBarConfig {
    private final int stages;
    private final ItemStack[] stageItems;
    public ItemStack getItemForProgress(int percentage) {
        return getItemForProgress(percentage, 0, 100);
    }
    public ItemStack getItemForProgress(int percentage, int currentTicks, int totalTicks) {
        int stage = (int) Math.floor((double) percentage / 100 * stages);
        if (stage >= stages) stage = stages - 1;
        if (stage < 0) stage = 0;
        ItemStack item = stageItems[stage].clone();
        ItemMeta meta = item.getItemMeta();
        int ticksLeft = totalTicks - currentTicks;
        int secondsLeft = ticksLeft / 20;
        if (meta != null && meta.hasDisplayName()) {
            Component displayName = meta.displayName();
            if (displayName != null) {
                String text = LegacyComponentSerializer.legacySection().serialize(displayName);
                text = replacePlaceholders(text, percentage, currentTicks, totalTicks, secondsLeft);
                meta.displayName(LegacyComponentSerializer.legacySection().deserialize(text));
            }
        }
        if (meta != null && meta.hasLore()) {
            List<Component> lore = meta.lore();
            if (lore != null) {
                List<Component> newLore = lore.stream()
                    .map(line -> {
                        String text = LegacyComponentSerializer.legacySection().serialize(line);
                        text = replacePlaceholders(text, percentage, currentTicks, totalTicks, secondsLeft);
                        return LegacyComponentSerializer.legacySection().deserialize(text);
                    })
                    .collect(Collectors.toList());
                meta.lore(newLore);
            }
        }
        item.setItemMeta(meta);
        return item;
    }
    private String replacePlaceholders(String text, int percentage, int currentTicks, 
                                        int totalTicks, int secondsLeft) {
        return text
            .replace("%progress%", percentage + "%")
            .replace("%ticks%", String.valueOf(currentTicks))
            .replace("%total_ticks%", String.valueOf(totalTicks))
            .replace("%seconds_left%", String.valueOf(secondsLeft));
    }
    public static ProgressBarConfig fromConfig(ConfigurationSection section) {
        if (section == null) return getDefault();
        int stages = section.getInt("stages", 5);
        if (stages != 5 && stages != 10 && stages != 20) {
            stages = 5;
        }
        ItemStack[] items = new ItemStack[stages];
        for (int i = 0; i < stages; i++) {
            String key = String.valueOf(i);
            if (section.contains(key)) {
                items[i] = loadItem(section.getConfigurationSection(key));
            } else {
                items[i] = getDefaultItem(i, stages);
            }
        }
        return new ProgressBarConfig(stages, items);
    }
    private static ItemStack loadItem(ConfigurationSection section) {
        String materialStr = section.getString("material", "GRAY_STAINED_GLASS_PANE");
        Material material = Material.matchMaterial(materialStr);
        if (material == null) material = Material.GRAY_STAINED_GLASS_PANE;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (section.contains("name")) {
            String name = section.getString("name");
            meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(name));
        }
        if (section.contains("lore")) {
            List<Component> lore = section.getStringList("lore").stream()
                .map(line -> LegacyComponentSerializer.legacyAmpersand().deserialize(line))
                .collect(Collectors.toList());
            meta.lore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }
    private static ItemStack getDefaultItem(int stage, int totalStages) {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        String text = "§7Progress: " + ((stage * 100) / totalStages) + "%";
        meta.displayName(LegacyComponentSerializer.legacySection().deserialize(text));
        item.setItemMeta(meta);
        return item;
    }
    public static ProgressBarConfig getDefault() {
        return new ProgressBarConfig(5, new ItemStack[]{
            createItem(Material.RED_STAINED_GLASS_PANE, "§cNo Progress"),
            createItem(Material.ORANGE_STAINED_GLASS_PANE, "§6Progress: %progress%"),
            createItem(Material.YELLOW_STAINED_GLASS_PANE, "§eProgress: %progress%"),
            createItem(Material.LIME_STAINED_GLASS_PANE, "§aProgress: %progress%"),
            createItem(Material.GREEN_STAINED_GLASS_PANE, "§2Done!")
        });
    }
    private static ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(LegacyComponentSerializer.legacySection().deserialize(name));
        item.setItemMeta(meta);
        return item;
    }
}
