package me.FireKillGrib.iAInteractables.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import dev.lone.itemsadder.api.CustomStack;

public class ItemsBuilder {
    private final ItemStack itemStack;
    private final ItemMeta meta;
    public ItemsBuilder(String materialName){
        if (materialName.contains("ia-")){
            CustomStack stack = CustomStack.getInstance(materialName.replace("ia-",""));
            if (stack == null) itemStack = new ItemStack(Material.STONE);
            else itemStack = stack.getItemStack();
            meta = itemStack.getItemMeta();
            return;
        }
        Material material = Material.matchMaterial(materialName.toUpperCase());
        if (material == null) material = Material.STONE;
        itemStack = new ItemStack(material);
        meta = itemStack.getItemMeta();
    }
    public ItemsBuilder setAmount(int amount){
        itemStack.setAmount(amount);
        return this;
    }
    public ItemsBuilder setName(String value){
        meta.displayName(ChatUtil.color(value));
        itemStack.setItemMeta(meta);
        return this;
    }
    public ItemsBuilder setName(String value, Map<String,String> args){
        for (String key : args.keySet()) {
            value = value.replace(key, args.get(key));
        }
        meta.displayName(ChatUtil.color(value));
        itemStack.setItemMeta(meta);
        return this;
    }
    public ItemsBuilder setLore(List<String> value){
        List<Component> list = new ArrayList<>();
        for (String text: value){
            list.add(ChatUtil.color(text));
        }
        meta.lore(list);
        itemStack.setItemMeta(meta);
        return this;
    }
    public ItemsBuilder setLore(List<String> value, Map<String,String> args){
        List<Component> list = new ArrayList<>();
        for (String text: value){
            for (String key : args.keySet()) {
                text = text.replace(key, args.get(key));
            }
            list.add(ChatUtil.color(text));
        }
        meta.lore(list);
        itemStack.setItemMeta(meta);
        return this;
    }
    public ItemsBuilder setCustomModelData(int value){
        meta.setCustomModelData(value);
        itemStack.setItemMeta(meta);
        return this;
    }
    public ItemsBuilder setPlayerHead(UUID uuid){
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        meta.setOwningPlayer(player);
        itemStack.setItemMeta(meta);
        return this;
    }
    public ItemsBuilder addEnchant(Enchantment value, int level){
        meta.addEnchant(value,level,true);
        itemStack.setItemMeta(meta);
        return this;
    }
    public ItemsBuilder removeEnchant(Enchantment value){
        meta.removeEnchant(value);
        itemStack.setItemMeta(meta);
        return this;
    }
    public ItemsBuilder addFlag(ItemFlag... value){
        meta.addAttributeModifier(Attribute.ARMOR, new AttributeModifier(NamespacedKey.minecraft(UUID.randomUUID().toString()),1, AttributeModifier.Operation.ADD_NUMBER));
        meta.addItemFlags(value);
        itemStack.setItemMeta(meta);
        return this;
    }
    public ItemsBuilder removeFlag(ItemFlag... value){
        meta.removeItemFlags(value);
        itemStack.setItemMeta(meta);
        return this;
    }
    public ItemsBuilder addOrRemoveAllFlags(Boolean value){
        if (value){
            meta.addItemFlags(ItemFlag.values());
            meta.addAttributeModifier(Attribute.ARMOR, new AttributeModifier(NamespacedKey.minecraft(UUID.randomUUID().toString()),1, AttributeModifier.Operation.ADD_NUMBER));
        }
        else {
            meta.removeItemFlags(ItemFlag.values());
        }
        itemStack.setItemMeta(meta);
        return this;
    }
    public ItemStack build(){
        return itemStack;
    }
}
