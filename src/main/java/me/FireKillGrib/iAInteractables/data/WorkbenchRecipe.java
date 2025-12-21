package me.FireKillGrib.iAInteractables.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.inventory.Inventory;
import java.util.Map;

@AllArgsConstructor
@Getter
public class WorkbenchRecipe {
    private final ItemStack result;
    private final Map<Integer, ItemStack> ingredients;
    public boolean check(Inventory inventory) {
        ItemStack[] items = inventory.getItems();
        for (Map.Entry<Integer, ItemStack> entry : ingredients.entrySet()) {
            int slot = entry.getKey();
            ItemStack required = entry.getValue();
            if (slot >= items.length) return false;
            ItemStack current = items[slot];
            if (current == null || !current.isSimilar(required) || current.getAmount() < required.getAmount()) {
                return false;
            }
        }
        return true;
    }
}
