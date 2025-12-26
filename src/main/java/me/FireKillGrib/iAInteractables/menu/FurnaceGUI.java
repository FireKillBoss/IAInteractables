package me.FireKillGrib.iAInteractables.menu;

import me.FireKillGrib.iAInteractables.Plugin;
import me.FireKillGrib.iAInteractables.data.*;
import me.FireKillGrib.iAInteractables.managers.FurnaceController;
import me.FireKillGrib.iAInteractables.utils.ChatUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.Gui;
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
                            furnace.getProgressBar().getItemForProgress(0)
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
                updateProgressBar();
            }, 0L, 1L);
    }
    private void updateProgressBar() {
        Integer progressSlot = findProgressSlot();
        if (progressSlot == null) return;
        if (!controller.isCooking()) {
            ItemStack progressItem = furnace.getProgressBar().getItemForProgress(0);
            gui.setItem(progressSlot, new SimpleItem(progressItem));
            return;
        }
        FurnaceRecipe recipe = controller.getCurrentRecipe();
        if (recipe == null) return;
        int percentage = controller.getProgressPercentage();
        int currentTicks = controller.getCookingProgress();
        int totalTicks = recipe.getCookTimeTicks();
        ItemStack progressItem = furnace.getProgressBar().getItemForProgress(
            percentage, 
            currentTicks, 
            totalTicks
        );
        gui.setItem(progressSlot, new SimpleItem(progressItem));
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
