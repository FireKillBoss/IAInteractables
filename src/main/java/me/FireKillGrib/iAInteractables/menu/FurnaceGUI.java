package me.FireKillGrib.iAInteractables.menu;

import me.FireKillGrib.iAInteractables.Plugin;
import me.FireKillGrib.iAInteractables.data.*;
import me.FireKillGrib.iAInteractables.managers.FurnaceController;
import me.FireKillGrib.iAInteractables.utils.ChatUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;
import java.util.*;

public class FurnaceGUI {
    private final Furnace furnace;
    private final Location location;
    private final FurnaceController controller;
    private Window window;
    private Gui gui;
    private BukkitTask updateTask;
    public FurnaceGUI(Furnace furnace, Location location, FurnaceController controller) {
        this.furnace = furnace;
        this.location = location;
        this.controller = controller;
    }
    public void open(Player player) {
        gui = createGui();
        window = Window.single()
            .setTitle(new AdventureComponentWrapper(ChatUtil.color(furnace.getTitle())))
            .setGui(gui)
            .setCloseHandlers(Collections.singletonList(() -> onClose()))
            .build(player);
        window.open();
        startProgressUpdater();
    }
    private Gui createGui() {
        Gui.Builder.Normal guiBuilder = Gui.normal()
            .setStructure(furnace.getStructure().toArray(new String[0]));
        Map<Character, Integer> structure = controller.getStructure();
        Set<Character> processedChars = new HashSet<>();
        for (String row : furnace.getStructure()) {
            for (char c : row.toCharArray()) {
                if (c == ' ') continue;
                if (!processedChars.contains(c)) {
                    processedChars.add(c);
                    if (c == 'X') {
                        guiBuilder.addIngredient('X', furnace.getFiller());
                    } else if (c == 'P') {
                        guiBuilder.addIngredient('P', new SimpleItem(
                            new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                                .setDisplayName("§7Waiting...")
                        ));
                    } else {
                        Integer inventorySlot = structure.get(c);
                        if (inventorySlot != null) {
                            guiBuilder.addIngredient(c, 
                                new xyz.xenondevs.invui.gui.SlotElement.InventorySlotElement(
                                    controller.getInventory(), inventorySlot));
                        }
                    }
                }
            }
        }
        return guiBuilder.build();
    }
    private void startProgressUpdater() {
        updateTask = Plugin.getInstance().getServer().getScheduler()
            .runTaskTimer(Plugin.getInstance(), () -> {
                if (controller.isCooking()) {
                    updateProgressBar();
                }
            }, 0L, 1L);
    }
    private void updateProgressBar() {
        if (!controller.isCooking()) return;
        FurnaceRecipe recipe = controller.getCurrentRecipe();
        if (recipe == null) return;
        int cookingProgress = controller.getCookingProgress();
        int totalTime = recipe.getCookTimeTicks();
        int percentage = (int) ((float) cookingProgress / totalTime * 100);
        Material barMaterial;
        if (percentage < 20)  barMaterial = Material.RED_STAINED_GLASS_PANE;
        else if (percentage < 40)  barMaterial = Material.ORANGE_STAINED_GLASS_PANE;
        else if (percentage < 60)  barMaterial = Material.YELLOW_STAINED_GLASS_PANE;
        else if (percentage < 80)  barMaterial = Material.LIME_STAINED_GLASS_PANE;
        else barMaterial = Material.GREEN_STAINED_GLASS_PANE;
        Integer progressSlot = findProgressSlot();
        if (progressSlot == null) return;
        ItemBuilder builder = new ItemBuilder(barMaterial)
            .setDisplayName("§eProgress: §6" + percentage + "%")
            .addLoreLines(
                "§7" + cookingProgress + " §8/§7 " + totalTime + " ticks",
                "§7" + (totalTime - cookingProgress) / 20 + " seconds left"
            );
        gui.setItem(progressSlot, new SimpleItem(builder));
    }
    private Integer findProgressSlot() {
        int index = 0;
        for (String row : furnace.getStructure()) {
            for (char c : row.toCharArray()) {
                if (c == ' ') continue;
                if (c == 'P') return index;
                index++;
            }
        }
        return null;
    }
    private void onClose() {
        if (updateTask != null) {
            updateTask.cancel();
        }
    }
    public void forceClose() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        if (window != null) {
            window.close();
        }
    }
}
