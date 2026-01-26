package me.FireKillGrib.iAInteractables.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
@Getter
public class SmithingRecipe {
    private final ItemStack result;
    private final ItemStack template;
    private final ItemStack base;
    private final ItemStack addition;
}