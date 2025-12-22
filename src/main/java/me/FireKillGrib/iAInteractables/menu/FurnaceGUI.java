package me.FireKillGrib.iAInteractables.menu;

import me.FireKillGrib.iAInteractables.Plugin;
import me.FireKillGrib.iAInteractables.data.*;
import me.FireKillGrib.iAInteractables.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;
import java.util.*;

public class FurnaceGUI {
    private final Furnace furnace;
    private final Location location;
    private Window window;
    private Gui gui;
    private VirtualInventory inventory;
    private final Map<Character, Integer> structure = new HashMap<>();
    private BukkitTask cookingTask;
    private boolean isCooking = false;
    private FurnaceRecipe currentRecipe;
    private int cookingProgress = 0;
    public FurnaceGUI(Furnace furnace, Location location) {
        this.furnace = furnace;
        this.location = location;
    }
    public void open(Player player) {
        gui = createGui();
        window = Window.single()
            .setTitle(new AdventureComponentWrapper(ChatUtil.color(furnace.getTitle()))) // ✅ Обернули в AdventureComponentWrapper
            .setGui(gui)
            .setCloseHandlers(Collections.singletonList(() -> onClose()))
            .build(player);
        window.open();
    }
    private Gui createGui() {
        Gui.Builder.Normal guiBuilder = Gui.normal()
            .setStructure(furnace.getStructure().toArray(new String[0]));
        inventory = new VirtualInventory(null, 54);
        inventory.setPostUpdateHandler(event -> {
            if (!isCooking) {
                checkAndStartCooking();
            }
        });
        int slotIndex = 0;
        for (String row : furnace.getStructure()) {
            String[] chars = row.split(" ");
            for (String charStr : chars) {
                if (charStr.length() == 1) {
                    char c = charStr.charAt(0);
                    if (c != 'X') {
                        structure.put(c, slotIndex);
                        if (c != 'P') {
                            guiBuilder.addIngredient(c, new xyz.xenondevs.invui.gui.SlotElement.InventorySlotElement(inventory, slotIndex));
                        } else {
                            guiBuilder.addIngredient(c, new SimpleItem(
                                new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                                    .setDisplayName("§7Wait...")
                            ));
                        }
                    } else {
                        guiBuilder.addIngredient('X', furnace.getFiller());
                    }
                }
                slotIndex++;
            }
        }
        return guiBuilder.build();
    }
    private Character getCharBySlot(int slot) {
        for (Map.Entry<Character, Integer> entry : structure.entrySet()) {
            if (entry.getValue() == slot) {
                return entry.getKey();
            }
        }
        return null;
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
        updateProgressBar();
        cookingTask = Bukkit.getScheduler().runTaskTimer(Plugin.getInstance(), () -> {
            cookingProgress++;
            updateProgressBar();
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
            updateProgressBar();
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
    private void updateProgressBar() {
        Integer progressSlot = structure.get('P');
        if (progressSlot == null || currentRecipe == null) return;
        int totalTime = currentRecipe.getCookTimeTicks();
        float progress = (float) cookingProgress / totalTime;
        int percentage = (int) (progress * 100);
        Material barMaterial;
        if (percentage < 25) {
            barMaterial = Material.RED_STAINED_GLASS_PANE;
        } else if (percentage < 50) {
            barMaterial = Material.ORANGE_STAINED_GLASS_PANE;
        } else if (percentage < 75) {
            barMaterial = Material.YELLOW_STAINED_GLASS_PANE;
        } else if (percentage < 100) {
            barMaterial = Material.LIME_STAINED_GLASS_PANE;
        } else {
            barMaterial = Material.GREEN_STAINED_GLASS_PANE;
        }
        ItemStack progressItem = new ItemStack(barMaterial);
        ItemBuilder builder = new ItemBuilder(progressItem)
            .setDisplayName("§eProgress: §6" + percentage + "%")
            .addLoreLines(
                "§7" + cookingProgress + " §8/§7 " + totalTime + " ticks",
                "§7" + (totalTime - cookingProgress) / 20 + " seconds left"
            );
        gui.setItem(progressSlot, new SimpleItem(builder));
    }
    private void playStartEffects() {
        if (furnace.getEffects() == null) return;
        FurnaceEffects.EffectConfig effect = furnace.getEffects().getOnStart();
        if (effect == null) return;
        playEffect(effect);
    }
    private void playCookingEffects() {
        if (furnace.getEffects() == null) return;
        FurnaceEffects.EffectConfig effect = furnace.getEffects().getOnCooking();
        if (effect == null) return;
        playEffect(effect);
    }
    private void playCompleteEffects() {
        if (furnace.getEffects() == null) return;
        FurnaceEffects.EffectConfig effect = furnace.getEffects().getOnComplete();
        if (effect == null) return;
        playEffect(effect);
    }
    private void playEffect(FurnaceEffects.EffectConfig effect) {
    if (effect.getParticle() != null) {
        ParticleConfig particle = effect.getParticle();
        try {
            Particle particleType = particle.getParticle(); 
            location.getWorld().spawnParticle(
                particleType,
                location.clone().add(0.5, 1, 0.5),
                particle.getCount(),
                particle.getOffsetX(),
                particle.getOffsetY(),
                particle.getOffsetZ(),
                particle.getSpeed()
            );
        } catch (IllegalArgumentException e) {
            Plugin.getInstance().getLogger().warning("Unknown particle: " + particle.getParticle());
        }
    }
    if (effect.getSound() != null) {
        SoundConfig sound = effect.getSound();
        try {
            Sound soundType = sound.getSound();
            SoundCategory category = sound.getCategory();
            location.getWorld().playSound(
                location,
                soundType,
                category,
                sound.getVolume(),
                sound.getPitch()
            );
        } catch (IllegalArgumentException e) {
            Plugin.getInstance().getLogger().warning("Unknown sound: " + sound.getSound());
        }
    }
}
    private void onClose() {
        if (cookingTask != null) {
            cookingTask.cancel();
            cookingTask = null;
        }
    }
}
