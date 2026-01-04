package me.FireKillGrib.iAInteractables.managers;

import dev.lone.itemsadder.api.CustomStack;
import me.FireKillGrib.iAInteractables.Plugin;
import me.FireKillGrib.iAInteractables.data.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
    private boolean isLoading = true; 
    public FurnaceController(Furnace furnace, Location location) {
        this.furnace = furnace;
        this.location = location;
        this.inventory = new VirtualInventory(null, 54);
        this.structure = new HashMap<>();
        parseStructure();
        loadData();
        isLoading = false; 
        inventory.setPostUpdateHandler(event -> {
            if (isLoading) return; 
            if (!isCooking) {
                checkAndStartCooking();
            }
            saveAsync();
        });
        checkAndStartCooking();
    }
    public Location getLocation() {
        return location;
    }
    private Location getCenterLocation() {
        return location.clone().add(0.5, 0.5, 0.5);
    }
    private void saveAsync() {
        if (isLoading) return;
        Plugin.getInstance().getFurnaceDataManager().saveAsync(location, inventory, cookingProgress);
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
        Map<String, Object> savedData = Plugin.getInstance().getFurnaceDataManager().load(location);
        if (savedData != null) {
            @SuppressWarnings("unchecked")
            Map<Integer, ItemStack> items = (Map<Integer, ItemStack>) savedData.get("items");
            if (items != null) {
                for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
                    inventory.setItem(null, entry.getKey(), entry.getValue());
                }
            }
            cookingProgress = (int) savedData.getOrDefault("cooking-progress", 0);
        }
    }
    public VirtualInventory getInventory() { return inventory; }
    public boolean isCooking() { return isCooking; }
    public FurnaceRecipe getCurrentRecipe() { return currentRecipe; }
    public int getCookingProgress() { return cookingProgress; }
    public int getProgressPercentage() {
        if (currentRecipe == null || currentRecipe.getCookTimeTicks() == 0) return 0;
        return (int) ((float) cookingProgress / currentRecipe.getCookTimeTicks() * 100);
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
            Map<Character, Integer> matchedSlots = matchRecipe(recipe);
            if (matchedSlots != null) {
                startCooking(recipe, matchedSlots);
                return;
            }
        }
    }
    private Map<Character, Integer> matchRecipe(FurnaceRecipe recipe) {
        Map<Character, Integer> matchedSlots = new HashMap<>();
        if (!checkGroup(recipe.getRaws(), matchedSlots)) return null;
        if (!checkGroup(recipe.getFuels(), matchedSlots)) return null;
        return matchedSlots;
    }
    private boolean checkGroup(Map<Character, Set<ItemStack>> group, Map<Character, Integer> matchedSlots) {
        for (Map.Entry<Character, Set<ItemStack>> entry : group.entrySet()) {
            char slotChar = entry.getKey();
            Integer slotIndex = structure.get(slotChar);
            if (slotIndex == null) return false;
            ItemStack item = inventory.getItem(slotIndex);
            if (item == null || item.getType() == Material.AIR) return false;
            boolean matchFound = false;
            for (ItemStack allowed : entry.getValue()) {
                if (itemsMatch(item, allowed)) {
                    matchFound = true;
                    break;
                }
            }
            if (!matchFound) return false;
            matchedSlots.put(slotChar, slotIndex);
        }
        return true;
    }
    private boolean itemsMatch(ItemStack item, ItemStack allowed) {
        if (item == null || allowed == null) return false;
        CustomStack customItem = CustomStack.byItemStack(item);
        CustomStack customAllowed = CustomStack.byItemStack(allowed);
        if (customItem != null && customAllowed != null) {
            return customItem.getNamespacedID().equals(customAllowed.getNamespacedID());
        } else if (customItem == null && customAllowed == null) {
            return item.getType() == allowed.getType();
        } else {
            return false;
        }
    }
    private boolean checkIngredientsPresent(FurnaceRecipe recipe, Map<Character, Integer> slots) {
        for (Map.Entry<Character, Integer> entry : slots.entrySet()) {
            char key = entry.getKey();
            int slotIdx = entry.getValue();
            ItemStack item = inventory.getItem(slotIdx);
            if (item == null || item.getType() == Material.AIR) return false;
            Set<ItemStack> allowedRaws = recipe.getRaws().get(key);
            if (allowedRaws != null) {
                boolean matches = false;
                for (ItemStack allowed : allowedRaws) {
                    if (itemsMatch(item, allowed)) {
                        matches = true;
                        break;
                    }
                }
                if (!matches) return false;
                continue;
            }
            Set<ItemStack> allowedFuels = recipe.getFuels().get(key);
            if (allowedFuels != null) {
                boolean matches = false;
                for (ItemStack allowed : allowedFuels) {
                    if (itemsMatch(item, allowed)) {
                        matches = true;
                        break;
                    }
                }
                if (!matches) return false;
                continue;
            }
        }
        return true;
    }
    private void startCooking(FurnaceRecipe recipe, Map<Character, Integer> slots) {
        if (isCooking) return;
        isCooking = true;
        currentRecipe = recipe;
        if (cookingProgress >= recipe.getCookTimeTicks()) cookingProgress = 0;
        playStartEffects();
        cookingTask = Bukkit.getScheduler().runTaskTimer(Plugin.getInstance(), () -> {
            if (!checkIngredientsPresent(recipe, slots)) {
                cancelCooking();
                return;
            }
            cookingProgress++;
            if (furnace.getEffects() != null && cookingProgress % furnace.getEffects().getCookingInterval() == 0) {
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
        final Integer resultSlot = structure.get('R');
        ItemStack resultToAdd = recipe.getResult().clone();
        if (resultSlot != null) {
            ItemStack existing = inventory.getItem(resultSlot);
            if (existing != null && existing.getType() != Material.AIR) {
                if (!itemsMatch(existing, resultToAdd) || existing.getAmount() + resultToAdd.getAmount() > existing.getMaxStackSize()) {
                    cookingProgress = recipe.getCookTimeTicks(); 
                    saveAsync();
                    return; 
                }
            }
        }
        for (Integer slotIdx : slots.values()) {
            ItemStack item = inventory.getItem(slotIdx);
            if (item != null) {
                item.setAmount(item.getAmount() - 1);
                if (item.getAmount() <= 0) {
                    inventory.setItem(null, slotIdx, null);
                } else {
                    inventory.setItem(null, slotIdx, item);
                }
            }
        }
        if (resultSlot != null) {
            ItemStack existing = inventory.getItem(resultSlot);
            if (existing == null || existing.getType() == Material.AIR) {
                inventory.setItem(null, resultSlot, resultToAdd);
            } else {
                existing.setAmount(existing.getAmount() + resultToAdd.getAmount());
                inventory.setItem(null, resultSlot, existing);
            }
        }
        playCompleteEffects();
        cookingProgress = 0;
        saveAsync();
        checkAndStartCooking();
    }
    private void playStartEffects() {
        if (furnace.getEffects() == null) return;
        if (furnace.getEffects().getOnStart() != null) {
            furnace.getEffects().getOnStart().play(getCenterLocation());
        }
    }
    private void playCookingEffects() {
        if (furnace.getEffects() == null) return;
        if (furnace.getEffects().getOnCooking() != null) {
            furnace.getEffects().getOnCooking().play(getCenterLocation());
        }
    }
    private void playCompleteEffects() {
        if (furnace.getEffects() == null) return;
        if (furnace.getEffects().getOnComplete() != null) {
            furnace.getEffects().getOnComplete().play(getCenterLocation());
        }
    }
    private void cancelCooking() {
        if (cookingTask != null) {
            cookingTask.cancel();
            cookingTask = null;
        }
        isCooking = false;
        currentRecipe = null;
        cookingProgress = 0;
        saveAsync();
    }
    public void shutdown() {
        if (cookingTask != null) cookingTask.cancel();
        Plugin.getInstance().getFurnaceDataManager().saveSync(location, inventory, cookingProgress);
    }
}