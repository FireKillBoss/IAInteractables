package me.FireKillGrib.iAInteractables.data;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class FurnaceInstance {
    private final Location location;
    private final String furnaceType;
    private final UUID id;
    private final Map<Character, ItemStack> slots = new HashMap<>();
    private ItemStack result;
    private int cookProgress = 0;
    private FurnaceRecipe activeRecipe = null;
    private boolean cooking = false;
    public FurnaceInstance(Location location, String furnaceType) {
        this.location = location;
        this.furnaceType = furnaceType;
        this.id = UUID.randomUUID();
    }
    public FurnaceInstance(Location location, String furnaceType, UUID id) {
        this.location = location;
        this.furnaceType = furnaceType;
        this.id = id;
    }
    public void setResult(ItemStack result) {
        this.result = result;
    }
    public void setCookProgress(int cookProgress) {
        this.cookProgress = cookProgress;
    }
    public void setActiveRecipe(FurnaceRecipe activeRecipe) {
        this.activeRecipe = activeRecipe;
    }
    public void setCooking(boolean cooking) {
        this.cooking = cooking;
    }
    public boolean isCooking() {
        return cooking;
    }
    public void setSlot(char slot, ItemStack item) {
        if (item == null || item.getType().isAir()) {
            slots.remove(slot);
        } else {
            slots.put(slot, item.clone());
        }
    }
    public ItemStack getSlot(char slot) {
        return slots.get(slot);
    }
    public void save(ConfigurationSection section) {
        section.set("id", id.toString());
        section.set("type", furnaceType);
        section.set("world", location.getWorld().getName());
        section.set("x", location.getBlockX());
        section.set("y", location.getBlockY());
        section.set("z", location.getBlockZ());
        section.set("cookProgress", cookProgress);
        section.set("isCooking", cooking);
        ConfigurationSection slotsSection = section.createSection("slots");
        for (Map.Entry<Character, ItemStack> entry : slots.entrySet()) {
            slotsSection.set(String.valueOf(entry.getKey()), entry.getValue());
        }
        if (result != null) {
            section.set("result", result);
        }
    }
}
