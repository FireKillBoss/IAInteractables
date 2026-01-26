package me.FireKillGrib.iAInteractables.menu;

import dev.lone.itemsadder.api.CustomStack;
import me.FireKillGrib.iAInteractables.data.SmithingRecipe;
import me.FireKillGrib.iAInteractables.data.SmithingTable;
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
import java.util.HashMap;
import java.util.Map;

public class SmithingGUI {
    private final SmithingTable table;
    private final VirtualInventory inventory;
    private Gui gui;
    private SmithingRecipe currentRecipe = null;
    private final Map<Character, Integer> charToSlotMap = new HashMap<>();
    private int resultGuiSlotIndex = -1;
    public SmithingGUI(SmithingTable table) {
        this.table = table;
        this.inventory = new VirtualInventory(null, 3);
        charToSlotMap.put('T', 0);
        charToSlotMap.put('B', 1);
        charToSlotMap.put('A', 2);
    }
    public void open(Player player) {
        gui = createGui();
        inventory.setPostUpdateHandler(event -> updateResult());
        Window.single()
                .setTitle(new AdventureComponentWrapper(ChatUtil.color(table.getTitle())))
                .setGui(gui)
                .addCloseHandler(() -> {
                    for (int i = 0; i < 3; i++) {
                        ItemStack is = inventory.getItem(i);
                        if (is != null && !is.getType().isAir()) {
                            player.getInventory().addItem(is).values()
                                    .forEach(remain -> player.getWorld().dropItem(player.getLocation(), remain));
                        }
                    }
                })
                .build(player)
                .open();
    }
    private Gui createGui() {
        Gui.Builder.Normal guiBuilder = Gui.normal()
                .setStructure(table.getStructure().toArray(new String[0]));
        int guiSlotCounter = 0;
        for (String row : table.getStructure()) {
            String cleanRow = row.replace(" ", "");
            for (char c : cleanRow.toCharArray()) {
                if (c == 'X') {
                    guiBuilder.addIngredient('X', table.getFiller());
                } else if (c == 'R') {
                    guiBuilder.addIngredient('R', new ResultItem());
                    resultGuiSlotIndex = guiSlotCounter;
                } else if (charToSlotMap.containsKey(c)) {
                    guiBuilder.addIngredient(c, new SlotElement.InventorySlotElement(inventory, charToSlotMap.get(c)));
                }
                guiSlotCounter++;
            }
        }
        return guiBuilder.build();
    }
    private void updateResult() {
        currentRecipe = null;
        for (SmithingRecipe recipe : table.getRecipes()) {
            if (checkRecipe(recipe)) {
                currentRecipe = recipe;
                break;
            }
        }
        if (gui != null && resultGuiSlotIndex != -1) {
            gui.setItem(resultGuiSlotIndex, new ResultItem());
        }
    }
    private boolean checkRecipe(SmithingRecipe recipe) {
        if (!checkSlot(0, recipe.getTemplate())) return false;
        if (!checkSlot(1, recipe.getBase())) return false;
        if (!checkSlot(2, recipe.getAddition())) return false;
        return true;
    }
    @SuppressWarnings("null")
    private boolean checkSlot(int slotIndex, ItemStack required) {
        ItemStack current = inventory.getItem(slotIndex);
        if (required == null && current != null && !current.getType().isAir()) return false;
        if (required != null && (current == null || current.getType().isAir())) return false;
        if (required == null) return true;
        return isSameItem(current, required) && current.getAmount() >= required.getAmount();
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
                if (table.getEffects() != null && table.getEffects().getOnCraft() != null) {
                    table.getEffects().getOnCraft().play(player, player.getLocation());
                }
                consumeItem(0, currentRecipe.getTemplate());
                consumeItem(1, currentRecipe.getBase());
                consumeItem(2, currentRecipe.getAddition());
                updateResult();
            }
        }
        private void consumeItem(int slot, ItemStack required) {
            if (required == null) return;
            ItemStack current = inventory.getItem(slot);
            if (current != null) {
                int newAmount = current.getAmount() - required.getAmount();
                if (newAmount <= 0) {
                    inventory.setItem(null, slot, null);
                } else {
                    ItemStack newItem = current.clone();
                    newItem.setAmount(newAmount);
                    inventory.setItem(null, slot, newItem);
                }
            }
        }
    }
}