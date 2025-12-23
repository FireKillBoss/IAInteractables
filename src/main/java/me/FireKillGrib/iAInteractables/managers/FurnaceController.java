package me.FireKillGrib.iAInteractables.managers;

import me.FireKillGrib.iAInteractables.Plugin;
import me.FireKillGrib.iAInteractables.data.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import java.util.*;

public class FurnaceController {
    private final Furnace furnace;
    private final Location location;
    private final VirtualInventory inventory;
    private final Map<Character, Integer> structure;
    private BukkitTask cookingTask;
    private boolean isCooking = false;
    private FurnaceRecipe currentRecipe;
    private int cookingProgress = 0;
    public FurnaceController(Furnace furnace, Location location) {
        this.furnace = furnace;
        this.location = location;
        this.inventory = new VirtualInventory(null, 54);
        this.structure = new HashMap<>();
        parseStructure();
        inventory.setPostUpdateHandler(event -> {
            if (!isCooking) {
                checkAndStartCooking();
            }
            Plugin.getInstance().getFurnaceDataManager()
                .save(location, inventory, cookingProgress);
        });
        loadData();
    }
    private void parseStructure() {
        int inventorySlotIndex = 0;
        for (String row : furnace.getStructure()) {
            for (char c : row.toCharArray()) {
                if (c == ' ') continue;
                if (c != 'X' && c != 'P') {
                    if (!structure.containsKey(c)) {
                        structure.put(c, inventorySlotIndex);
                        inventorySlotIndex++;
                    }
                }
            }
        }
    }
    public Map<Character, Integer> getStructure() {
        return new HashMap<>(structure);
    }
    private void loadData() {
        Map<String, Object> savedData = Plugin.getInstance()
            .getFurnaceDataManager()
            .load(location);
        if (savedData != null) {
            @SuppressWarnings("unchecked")
            Map<Integer, ItemStack> items = (Map<Integer, ItemStack>) savedData.get("items");
            for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
                inventory.setItem(null, entry.getKey(), entry.getValue());
            }
            cookingProgress = (int) savedData.get("cooking-progress");
            if (cookingProgress > 0) {
                checkAndStartCooking();
            }
        }
    }
    public VirtualInventory getInventory() {
        return inventory;
    }
    public int getProgressPercentage() {
        if (currentRecipe == null) return 0;
        return (int) ((float) cookingProgress / currentRecipe.getCookTimeTicks() * 100);
    }
    public FurnaceRecipe getCurrentRecipe() {
        return currentRecipe;
    }
    public int getCookingProgress() {
        return cookingProgress;
    }
    public boolean isCooking() {
        return isCooking;
    }
    private void checkAndStartCooking() {
        if (isCooking) return;
        Integer resultSlot = structure.get('R');
        if (resultSlot != null) {
            ItemStack resultItem = inventory.getItem(resultSlot);
            if (resultItem != null && resultItem.getAmount() >= resultItem.getMaxStackSize()) {
                return;
            }
        }
        for (FurnaceRecipe recipe : furnace.getRecipes()) {
            Map<Character, Integer> matchedSlots = new HashMap<>();
            boolean matches = true;
            for (Map.Entry<Character, Set<ItemStack>> rawEntry : recipe.getRaws().entrySet()) {
                char slotChar = rawEntry.getKey();
                Set<ItemStack> allowedItems = rawEntry.getValue();
                Integer slotIndex = structure.get(slotChar);
                if (slotIndex == null) {
                    matches = false;
                    break;
                }
                ItemStack item = inventory.getItem(slotIndex);
                if (item == null || item.getType() == Material.AIR) {
                    matches = false;
                    break;
                }
                boolean itemMatches = false;
                for (ItemStack allowed : allowedItems) {
                    if (item.getType() == allowed.getType()) {
                        itemMatches = true;
                        break;
                    }
                }
                if (!itemMatches) {
                    matches = false;
                    break;
                }
                matchedSlots.put(slotChar, slotIndex);
            }
            if (!matches) continue;
            for (Map.Entry<Character, Set<ItemStack>> fuelEntry : recipe.getFuels().entrySet()) {
                char slotChar = fuelEntry.getKey();
                Set<ItemStack> allowedFuels = fuelEntry.getValue();
                Integer slotIndex = structure.get(slotChar);
                if (slotIndex == null) {
                    matches = false;
                    break;
                }
                ItemStack item = inventory.getItem(slotIndex);
                if (item == null || item.getType() == Material.AIR) {
                    matches = false;
                    break;
                }
                boolean fuelMatches = false;
                for (ItemStack allowed : allowedFuels) {
                    if (item.getType() == allowed.getType()) {
                        fuelMatches = true;
                        break;
                    }
                }
                if (!fuelMatches) {
                    matches = false;
                    break;
                }
                matchedSlots.put(slotChar, slotIndex);
            }
            if (matches) {
                startCooking(recipe, matchedSlots);
                return;
            }
        }
    }
    private void startCooking(FurnaceRecipe recipe, Map<Character, Integer> slots) {
        if (isCooking) return;
        isCooking = true;
        currentRecipe = recipe;
        cookingProgress = 0;
        playStartEffects();
        cookingTask = Bukkit.getScheduler().runTaskTimer(Plugin.getInstance(), () -> {
            if (!checkIngredientsPresent(recipe, slots)) {
                cancelCooking();
                return;
            }
            cookingProgress++;
            if (furnace.getEffects() != null && 
                cookingProgress % furnace.getEffects().getCookingInterval() == 0) {
                playCookingEffects();
            }
            if (cookingProgress >= recipe.getCookTimeTicks()) {
                completeCooking(recipe, slots);
            }
        }, 0L, 1L);
    }
    private void completeCooking(FurnaceRecipe recipe, Map<Character, Integer> slots) {
        if (cookingTask != null) {
            cookingTask.cancel();
            cookingTask = null;
        }
        isCooking = false;
        for (Map.Entry<Character, Integer> entry : slots.entrySet()) {
            int inventorySlot = entry.getValue();
            ItemStack item = inventory.getItem(inventorySlot);
            if (item != null && item.getAmount() > 0) {
                item.setAmount(item.getAmount() - 1);
                if (item.getAmount() == 0) {
                    inventory.setItem(null, inventorySlot, null);
                }
            }
        }
        Bukkit.getScheduler().runTask(Plugin.getInstance(), () -> {
            Integer resultSlot = structure.get('R');
            if (resultSlot != null) {
                ItemStack result = recipe.getResult().clone();
                ItemStack existingResult = inventory.getItem(resultSlot);
                if (existingResult == null || existingResult.getType() == Material.AIR) {
                    inventory.setItem(null, resultSlot, result);
                } else if (existingResult.isSimilar(result) && 
                        existingResult.getAmount() + result.getAmount() <= result.getMaxStackSize()) {
                    existingResult.setAmount(existingResult.getAmount() + result.getAmount());
                } else {
                    restoreIngredients(slots);
                    return;
                }
            }
            playCompleteEffects();
            cookingProgress = 0;
            checkAndStartCooking();
        });
    }
    private void restoreIngredients(Map<Character, Integer> slots) {
        for (Map.Entry<Character, Integer> entry : slots.entrySet()) {
            int inventorySlot = entry.getValue();
            ItemStack item = inventory.getItem(inventorySlot);
            if (item != null) {
                item.setAmount(item.getAmount() + 1);
            }
        }
    }
    private void playStartEffects() {
        if (furnace.getEffects() == null) return;
        playEffect(furnace.getEffects().getOnStart());
    }
    private void playCookingEffects() {
        if (furnace.getEffects() == null) return;
        playEffect(furnace.getEffects().getOnCooking());
    }
    private void playCompleteEffects() {
        if (furnace.getEffects() == null) return;
        playEffect(furnace.getEffects().getOnComplete());
    }
    private void playEffect(FurnaceEffects.EffectConfig effect) {
        if (effect == null) return;
        if (location.getWorld() == null) {
            Plugin.getInstance().getLogger().severe(
                "Cannot play effect: world not loaded at " + location);
            return;
        }
        if (!location.getChunk().isLoaded()) {
            location.getChunk().load();
        }
        Bukkit.getScheduler().runTask(Plugin.getInstance(), () -> {
            try {
                if (effect.getParticle() != null) {
                    ParticleConfig particle = effect.getParticle();
                    Location particleLocation = new Location(
                        location.getWorld(),
                        location.getBlockX() + 0.5,
                        location.getBlockY() + 0.7,
                        location.getBlockZ() + 0.5
                    );
                    location.getWorld().spawnParticle(
                        particle.getParticle(),
                        particleLocation,
                        particle.getCount(),
                        particle.getOffsetX(),
                        particle.getOffsetY(),
                        particle.getOffsetZ(),
                        particle.getSpeed()
                    );
                }
                if (effect.getSound() != null) {
                    SoundConfig sound = effect.getSound();
                    Location soundLocation = new Location(
                        location.getWorld(),
                        location.getBlockX() + 0.5,
                        location.getBlockY() + 0.5,
                        location.getBlockZ() + 0.5
                    );
                    location.getWorld().playSound(
                        soundLocation,
                        sound.getSound(),
                        sound.getCategory(),
                        sound.getVolume(),
                        sound.getPitch()
                    );
                    for (Player nearbyPlayer : location.getWorld().getNearbyPlayers(soundLocation, 32)) {
                        nearbyPlayer.playSound(
                            soundLocation,
                            sound.getSound(),
                            sound.getCategory(),
                            sound.getVolume(),
                            sound.getPitch()
                        );
                    }
                }
            } catch (Exception e) {
                Plugin.getInstance().getLogger().severe(
                    "Error playing furnace effect at " + location + ": " + e.getMessage());
            }
        });
    }
    private boolean checkIngredientsPresent(FurnaceRecipe recipe, Map<Character, Integer> slots) {
        for (Map.Entry<Character, Set<ItemStack>> rawEntry : recipe.getRaws().entrySet()) {
            char slotChar = rawEntry.getKey();
            Set<ItemStack> allowedItems = rawEntry.getValue();
            Integer slotIndex = slots.get(slotChar);
            if (slotIndex == null) return false;
            ItemStack item = inventory.getItem(slotIndex);
            if (item == null || item.getType() == Material.AIR) {
                return false;
            }
            boolean matches = false;
            for (ItemStack allowed : allowedItems) {
                if (item.getType() == allowed.getType()) {
                    matches = true;
                    break;
                }
            }
            if (!matches) return false;
        }
        for (Map.Entry<Character, Set<ItemStack>> fuelEntry : recipe.getFuels().entrySet()) {
            char slotChar = fuelEntry.getKey();
            Set<ItemStack> allowedFuels = fuelEntry.getValue();
            Integer slotIndex = slots.get(slotChar);
            if (slotIndex == null) return false;
            ItemStack item = inventory.getItem(slotIndex);
            if (item == null || item.getType() == Material.AIR) {
                return false;
            }
            boolean matches = false;
            for (ItemStack allowed : allowedFuels) {
                if (item.getType() == allowed.getType()) {
                    matches = true;
                    break;
                }
            }
            if (!matches) return false;
        }
        return true;
    }
    private void cancelCooking() {
        if (cookingTask != null) {
            cookingTask.cancel();
            cookingTask = null;
        }
        isCooking = false;
        currentRecipe = null;
        cookingProgress = 0;
        Plugin.getInstance().getFurnaceDataManager()
            .save(location, inventory, cookingProgress);
    }
    public void shutdown() {
        if (cookingTask != null) {
            cookingTask.cancel();
        }
        Plugin.getInstance().getFurnaceDataManager()
            .save(location, inventory, cookingProgress);
    }
}
