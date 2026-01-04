package me.FireKillGrib.iAInteractables.menu;

import dev.lone.itemsadder.api.CustomStack;
import me.FireKillGrib.iAInteractables.data.Workbench;
import me.FireKillGrib.iAInteractables.data.WorkbenchRecipe;
import me.FireKillGrib.iAInteractables.utils.ChatUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.SlotElement;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.window.Window;
import java.util.*;

public class WorkbenchGUI {
    private final Workbench workbench;
    private final VirtualInventory gridInventory;
    private Gui gui;
    private WorkbenchRecipe currentRecipe = null;
    private final Map<Character, Integer> charToSlotMap = new HashMap<>();
    private final List<Integer> indexToSlotList = new ArrayList<>();
    private int resultGuiSlotIndex = -1;
    public WorkbenchGUI(Workbench workbench) {
        this.workbench = workbench;
        this.gridInventory = new VirtualInventory(null, 54);
    }
    public void open(Player player) {
        gui = createGui();
        gridInventory.setPostUpdateHandler(event -> updateResult());
        Window.single()
                .setTitle(new AdventureComponentWrapper(ChatUtil.color(workbench.getTitle())))
                .setGui(gui)
                .addCloseHandler(() -> {
                    for (int i = 0; i < indexToSlotList.size(); i++) {
                        ItemStack is = gridInventory.getItem(i);
                        if (is != null && !is.getType().isAir()) {
                            player.getInventory().addItem(is).values()
                                    .forEach(remain -> player.getWorld().dropItem(player.getLocation(), remain));
                        }
                    }
                })
                .open(player);
    }
    private Gui createGui() {
        Gui.Builder.Normal guiBuilder = Gui.normal()
                .setStructure(workbench.getStructure().toArray(new String[0]));
        int internalIndex = 0;
        int guiSlotCounter = 0;
        for (String row : workbench.getStructure()) {
            String cleanRow = row.replace(" ", ""); 
            for (char c : cleanRow.toCharArray()) {
                if (c == 'X') {
                    guiBuilder.addIngredient('X', workbench.getFiller());
                } else if (c == 'R') {
                    guiBuilder.addIngredient('R', new ResultItem());
                    resultGuiSlotIndex = guiSlotCounter;
                } else {
                    guiBuilder.addIngredient(c, new SlotElement.InventorySlotElement(gridInventory, internalIndex));
                    charToSlotMap.put(c, internalIndex);
                    indexToSlotList.add(internalIndex);
                    internalIndex++;
                }
                guiSlotCounter++;
            }
        }
        return guiBuilder.build();
    }
    private void updateResult() {
        currentRecipe = null;
        for (WorkbenchRecipe recipe : workbench.getRecipes()) {
            if (checkRecipe(recipe)) {
                currentRecipe = recipe;
                break;
            }
        }
        if (gui != null && resultGuiSlotIndex != -1) {
            gui.setItem(resultGuiSlotIndex, new ResultItem());
        }
    }
    private boolean checkRecipe(WorkbenchRecipe recipe) {
        for (Map.Entry<Character, ItemStack> entry : recipe.getIngredients().entrySet()) {
            char key = entry.getKey();
            Integer slotIndex = resolveSlotIndex(key);
            if (slotIndex == null) return false;
            ItemStack currentItem = gridInventory.getItem(slotIndex);
            ItemStack required = entry.getValue();
            if (currentItem == null || currentItem.getType() == Material.AIR) return false;
            if (!isSameItem(currentItem, required)) return false;
            if (currentItem.getAmount() < required.getAmount()) return false;
        }
        for (Integer slotIndex : indexToSlotList) {
            if (!isSlotUsedInRecipe(recipe, slotIndex)) {
                ItemStack item = gridInventory.getItem(slotIndex);
                if (item != null && !item.getType().isAir()) {
                    return false;
                }
            }
        }
        return true;
    }
    private boolean isSlotUsedInRecipe(WorkbenchRecipe recipe, int slotIndex) {
        for (char key : recipe.getIngredients().keySet()) {
            Integer mappedSlot = resolveSlotIndex(key);
            if (mappedSlot != null && mappedSlot == slotIndex) {
                return true;
            }
        }
        return false;
    }
    private Integer resolveSlotIndex(char key) {
        if (charToSlotMap.containsKey(key)) {
            return charToSlotMap.get(key);
        }
        if (Character.isDigit(key)) {
            int index = Character.getNumericValue(key);
            if (index >= 0 && index < indexToSlotList.size()) {
                return indexToSlotList.get(index);
            }
        }
        return null;
    }
    private boolean isSameItem(ItemStack item1, ItemStack item2) {
        if (item1 == null || item2 == null) return false;
        CustomStack c1 = CustomStack.byItemStack(item1);
        CustomStack c2 = CustomStack.byItemStack(item2);
        if (c1 != null && c2 != null) {
            return c1.getNamespacedID().equals(c2.getNamespacedID());
        }
        return c1 == null && c2 == null && item1.getType() == item2.getType();
    }
    private class ResultItem extends AbstractItem {
        @Override
        public ItemProvider getItemProvider() {
            if (currentRecipe != null) {
                return new xyz.xenondevs.invui.item.builder.ItemBuilder(currentRecipe.getResult());
            }
            return new xyz.xenondevs.invui.item.builder.ItemBuilder(Material.AIR);
        }

        @Override
        public void handleClick(ClickType clickType, Player player, InventoryClickEvent event) {
            if (currentRecipe == null) return;
            if (event.isLeftClick() || event.isRightClick() || event.isShiftClick()) {
                if (player.getItemOnCursor().getType() != Material.AIR) return;
                player.setItemOnCursor(currentRecipe.getResult().clone());
                if (workbench.getEffects() != null && workbench.getEffects().getOnCraft() != null) {
                    workbench.getEffects().getOnCraft().play(player, player.getLocation());
                }
                consumeIngredients();
                updateResult();
            }
        }
        private void consumeIngredients() {
            for (Map.Entry<Character, ItemStack> entry : currentRecipe.getIngredients().entrySet()) {
                Integer slotIndex = resolveSlotIndex(entry.getKey());
                if (slotIndex != null) {
                    ItemStack current = gridInventory.getItem(slotIndex);
                    if (current != null) {
                        int newAmount = current.getAmount() - entry.getValue().getAmount();
                        if (newAmount <= 0) {
                            gridInventory.setItem(null, slotIndex, null);
                        } else {
                            ItemStack newItem = current.clone();
                            newItem.setAmount(newAmount);
                            gridInventory.setItem(null, slotIndex, newItem);
                        }
                    }
                }
            }
        }
    }
}