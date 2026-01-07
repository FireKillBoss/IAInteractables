package me.FireKillGrib.iAInteractables.menu.recipebook;

import me.FireKillGrib.iAInteractables.data.Furnace;
import me.FireKillGrib.iAInteractables.data.FurnaceRecipe;
import me.FireKillGrib.iAInteractables.data.Workbench;
import me.FireKillGrib.iAInteractables.data.WorkbenchRecipe;
import me.FireKillGrib.iAInteractables.utils.ChatUtil;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;
import java.util.List;
import java.util.Set;

public class RecipeViewGUI {
    private final Workbench workbench;
    private final WorkbenchRecipe wbRecipe;
    private final Furnace furnace;
    private final FurnaceRecipe fnRecipe;
    public RecipeViewGUI(Workbench wb, WorkbenchRecipe recipe) {
        this.workbench = wb;
        this.wbRecipe = recipe;
        this.furnace = null;
        this.fnRecipe = null;
    }
    public RecipeViewGUI(Furnace fn, FurnaceRecipe recipe) {
        this.furnace = fn;
        this.fnRecipe = recipe;
        this.workbench = null;
        this.wbRecipe = null;
    }
    public void open(Player player) {
        Gui.Builder.Normal guiBuilder = Gui.normal();
        List<String> structure;
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        SimpleItem backButton = new SimpleItem(
                new ItemBuilder(Material.BARRIER)
                    .setDisplayName(serializer.serialize(ChatUtil.color("&cBack to list"))),
                click -> {
                    if (workbench != null) new RecipeListGUI(workbench).open(player);
                    else if (furnace != null) new RecipeListGUI(furnace).open(player);
                }
        );
        if (workbench != null) {
            structure = workbench.getStructure();
            guiBuilder.setStructure(structure.toArray(new String[0]));
            for (String row : structure) {
                String cleanRow = row.replace(" ", "");
                for (char c : cleanRow.toCharArray()) {
                    if (c == 'X') {
                        guiBuilder.addIngredient('X', workbench.getFiller());
                    } else if (c == 'R') {
                        guiBuilder.addIngredient('R', new SimpleItem(new ItemBuilder(wbRecipe.getResult())));
                    } else if (c == 'Z') {
                        guiBuilder.addIngredient('Z', backButton);
                    } else {
                        if (wbRecipe.getIngredients().containsKey(c)) {
                            guiBuilder.addIngredient(c, new SimpleItem(new ItemBuilder(wbRecipe.getIngredients().get(c))));
                        } else {
                            guiBuilder.addIngredient(c, new SimpleItem(new ItemBuilder(Material.AIR)));
                        }
                    }
                }
            }
        } else if (furnace != null) {
            structure = furnace.getStructure();
            guiBuilder.setStructure(structure.toArray(new String[0]));
            for (String row : structure) {
                String cleanRow = row.replace(" ", "");
                for (char c : cleanRow.toCharArray()) {
                    if (c == 'X') {
                        guiBuilder.addIngredient('X', furnace.getFiller());
                    } else if (c == 'P') {
                        guiBuilder.addIngredient('P', new SimpleItem(new ItemBuilder(furnace.getProgressBar().getItemForProgress(0))));
                    } else if (c == 'R') {
                        guiBuilder.addIngredient('R', new SimpleItem(new ItemBuilder(fnRecipe.getResult())));
                    } else if (c == 'Z') {
                        guiBuilder.addIngredient('Z', backButton);
                    } else {
                        if (fnRecipe.getRaws().containsKey(c)) {
                            Set<ItemStack> raws = fnRecipe.getRaws().get(c);
                            if (!raws.isEmpty()) {
                                guiBuilder.addIngredient(c, new SimpleItem(new ItemBuilder(raws.iterator().next())));
                            }
                        } 
                        else if (fnRecipe.getFuels().containsKey(c)) {
                            Set<ItemStack> fuels = fnRecipe.getFuels().get(c);
                            if (!fuels.isEmpty()) {
                                guiBuilder.addIngredient(c, new SimpleItem(new ItemBuilder(fuels.iterator().next())));
                            }
                        } else {
                            guiBuilder.addIngredient(c, new SimpleItem(new ItemBuilder(Material.AIR)));
                        }
                    }
                }
            }
        }
        Window window = Window.single()
                .setTitle(new AdventureComponentWrapper(ChatUtil.color("&8View recipe")))
                .setGui(guiBuilder.build())
                .build(player);
        window.open();
    }
}