package me.FireKillGrib.iAInteractables.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.xenondevs.invui.item.ItemProvider;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ItemBuilder {
    private final ItemStack item;
    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
    }
    public ItemBuilder(ItemStack item) {
        this.item = item.clone();
    }
    public ItemBuilder setDisplayName(String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(name);
            meta.displayName(component);
            item.setItemMeta(meta);
        }
        return this;
    }
    public ItemBuilder setLore(String... lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<Component> componentLore = Arrays.stream(lore)
                .map(line -> LegacyComponentSerializer.legacyAmpersand().deserialize(line))
                .collect(Collectors.toList());
            meta.lore(componentLore);
            item.setItemMeta(meta);
        }
        return this;
    }
    public ItemBuilder setLore(List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<Component> componentLore = lore.stream()
                .map(line -> LegacyComponentSerializer.legacyAmpersand().deserialize(line))
                .collect(Collectors.toList());
            meta.lore(componentLore);
            item.setItemMeta(meta);
        }
        return this;
    }
    public ItemStack build() {
        return item;
    }
    public ItemProvider getItemProvider() {
        return new xyz.xenondevs.invui.item.builder.ItemBuilder(item);
    }
}
