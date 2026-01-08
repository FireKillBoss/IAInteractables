package me.FireKillGrib.iAInteractables.menu;

import me.FireKillGrib.iAInteractables.Plugin;
import me.FireKillGrib.iAInteractables.data.Furnace;
import me.FireKillGrib.iAInteractables.data.FurnaceRecipe;
import me.FireKillGrib.iAInteractables.managers.FurnaceController;
import me.FireKillGrib.iAInteractables.utils.ChatUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.SlotElement;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;
import java.util.*;

public class FurnaceGUI {
    private final Furnace furnace;
    private final FurnaceController controller;
    private Window window;
    private Gui gui;
    private BukkitTask updateTask;
    private int progressGuiSlotIndex = -1;
    public FurnaceGUI(Furnace furnace, org.bukkit.Location location, FurnaceController controller) {
        this.furnace = furnace;
        this.controller = controller;
    }
    public void open(Player player) {
        gui = createGui();
        window = Window.single()
                .setTitle(new AdventureComponentWrapper(ChatUtil.color(furnace.getTitle())))
                .setGui(gui)
                .addCloseHandler(this::onClose)
                .build(player);
        window.open();
        startProgressUpdater();
    }
    private Gui createGui() {
        String[] cleanStructure = furnace.getStructure().stream()
                .map(row -> row.replace(" ", ""))
                .toArray(String[]::new);
        Gui.Builder.Normal guiBuilder = Gui.normal()
                .setStructure(cleanStructure);
        Map<Character, Integer> controllerStructure = controller.getStructure();
        Set<Character> processedChars = new HashSet<>();
        int guiSlotCounter = 0;
        for (String row : cleanStructure) {
            for (char c : row.toCharArray()) {
                if (c == 'P') {
                    progressGuiSlotIndex = guiSlotCounter;
                }
                if (!processedChars.contains(c)) {
                    processedChars.add(c);
                    if (c == 'X') {
                        guiBuilder.addIngredient('X', furnace.getFiller());
                    } else if (c == 'P') {
                        guiBuilder.addIngredient('P', new SimpleItem(
                                furnace.getProgressBar().getItemForProgress(0)
                        ));
                    } else {
                        Integer inventorySlot = controllerStructure.get(c);
                        if (inventorySlot != null) {
                            guiBuilder.addIngredient(c,
                                    new SlotElement.InventorySlotElement(
                                            controller.getInventory(), inventorySlot));
                        } else {
                            guiBuilder.addIngredient(c, new SimpleItem(new xyz.xenondevs.invui.item.builder.ItemBuilder(Material.AIR)));
                        }
                    }
                }
                guiSlotCounter++;
            }
        }
        return guiBuilder.build();
    }
    private void startProgressUpdater() {
        updateTask = Plugin.getInstance().getServer().getScheduler()
                .runTaskTimer(Plugin.getInstance(), this::updateProgressBar, 0L, 5L);
    }
    private void updateProgressBar() {
        if (gui == null || progressGuiSlotIndex == -1) return;
        if (!controller.isCooking()) {
            ItemStack progressItem = furnace.getProgressBar().getItemForProgress(0);
            gui.setItem(progressGuiSlotIndex, new SimpleItem(progressItem));
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
        gui.setItem(progressGuiSlotIndex, new SimpleItem(progressItem));
    }
    private void onClose() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }
    public void forceClose() {
        onClose();
        if (window != null) {
            window.close();
        }
    }
}