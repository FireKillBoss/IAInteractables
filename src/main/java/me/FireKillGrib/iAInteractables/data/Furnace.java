package me.FireKillGrib.iAInteractables.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.xenondevs.invui.item.ItemProvider;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Getter
public class Furnace {
    private final String name;
    private final String title;
    private final List<String> structure;
    private final ItemProvider filler;
    private final Set<FurnaceRecipe> recipes;
    private final FurnaceEffects effects;
}
