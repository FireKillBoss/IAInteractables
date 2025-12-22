package me.FireKillGrib.iAInteractables.menu;

import me.FireKillGrib.iAInteractables.data.Workbench;
import me.FireKillGrib.iAInteractables.data.WorkbenchRecipe;
import me.FireKillGrib.iAInteractables.utils.ChatUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import xyz.xenondevs.invui.window.Window;
import java.util.*;

public class WorkbenchGUI {
    private final Workbench workbench;
    private Window window;
    private Gui gui;
    private VirtualInventory inventory;
    private final Map<Character, Integer> structure = new HashMap<>();
    public WorkbenchGUI(Workbench workbench) {
        this.workbench = workbench;
    }
    public void open(Player player) {
    gui = createGui();
    window = Window.single()
        .setTitle(new AdventureComponentWrapper(ChatUtil.color(workbench.getTitle())))
        .setGui(gui)
        .setCloseHandlers(Collections.singletonList(() -> onClose()))
        .build(player);
    window.open();
}
    private void onClose() {
    }
    private Gui createGui() {
        Gui.Builder.Normal guiBuilder = Gui.normal()
            .setStructure(workbench.getStructure().toArray(new String[0]));
        inventory = new VirtualInventory(null, 54);
        inventory.setPostUpdateHandler(event -> checkAndCraft());
        int slotIndex = 0;
        for (String row : workbench.getStructure()) {
            String[] chars = row.split(" ");
            for (String charStr : chars) {
                if (charStr.length() == 1) {
                    char c = charStr.charAt(0);
                    if (c != 'X') {
                        structure.put(c, slotIndex);
                        guiBuilder.addIngredient(c, new xyz.xenondevs.invui.gui.SlotElement.InventorySlotElement(inventory, slotIndex));
                    } else {
                        guiBuilder.addIngredient('X', workbench.getFiller());
                    }
                }
                slotIndex++;
            }
        }
        return guiBuilder.build();
    }
    private void checkAndCraft() {
        for (WorkbenchRecipe recipe : workbench.getRecipes()) {
            boolean matches = true;
            for (Map.Entry<Integer, ItemStack> ingredient : recipe.getIngredients().entrySet()) {
                Integer slotIndex = ingredient.getKey();
                ItemStack required = ingredient.getValue();
                Integer guiSlot = findSlotByNumber(slotIndex);
                if (guiSlot == null) {
                    matches = false;
                    break;
                }
                ItemStack item = inventory.getItem(guiSlot);
                if (item == null || 
                    item.getType() != required.getType() || 
                    item.getAmount() < required.getAmount()) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                craft(recipe);
                return;
            }
        }
    }
    private Integer findSlotByNumber(int number) {
        for (Map.Entry<Character, Integer> entry : structure.entrySet()) {
            char c = entry.getKey();
            if (Character.isDigit(c) && Character.getNumericValue(c) == number) {
                return entry.getValue();
            }
        }
        return null;
    }
    private void craft(WorkbenchRecipe recipe) {
        for (Map.Entry<Integer, ItemStack> ingredient : recipe.getIngredients().entrySet()) {
            Integer slotIndex = findSlotByNumber(ingredient.getKey());
            if (slotIndex != null) {
                ItemStack item = inventory.getItem(slotIndex);
                if (item != null) {
                    item.setAmount(item.getAmount() - ingredient.getValue().getAmount());
                    if (item.getAmount() <= 0) {
                        inventory.setItem(null, slotIndex, null);
                    }
                }
            }
        }
        Integer resultSlot = structure.get('R');
        if (resultSlot != null) {
            ItemStack result = recipe.getResult().clone();
            ItemStack existingResult = inventory.getItem(resultSlot);
            if (existingResult == null || existingResult.getType() == Material.AIR) {
                inventory.setItem(null, resultSlot, result);
            } else if (existingResult.isSimilar(result) && 
                    existingResult.getAmount() + result.getAmount() <= result.getMaxStackSize()) {
                existingResult.setAmount(existingResult.getAmount() + result.getAmount());
            }
        }
    }
}
