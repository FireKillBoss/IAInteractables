package me.FireKillGrib.iAInteractables.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import java.util.Map;

@AllArgsConstructor
@Getter
public class WorkbenchRecipe {
    private final ItemStack result;
    private final Map<Character, ItemStack> ingredients;
}