package me.FireKillGrib.iAInteractables.menu.recipebook;

import me.FireKillGrib.iAInteractables.data.Furnace;
import me.FireKillGrib.iAInteractables.data.FurnaceRecipe;
import me.FireKillGrib.iAInteractables.data.Workbench;
import me.FireKillGrib.iAInteractables.data.WorkbenchRecipe;
import me.FireKillGrib.iAInteractables.utils.ChatUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;
import java.util.ArrayList;
import java.util.List;

public class RecipeListGUI {
    private final Workbench workbench;
    private final Furnace furnace;
    private final String title;
    public RecipeListGUI(Workbench workbench) {
        this.workbench = workbench;
        this.furnace = null;
        this.title = workbench.getTitle();
    }
    public RecipeListGUI(Furnace furnace) {
        this.furnace = furnace;
        this.workbench = null;
        this.title = furnace.getTitle();
    }
    public void open(Player player) {
        List<xyz.xenondevs.invui.item.Item> items = new ArrayList<>();
        if (workbench != null) {
            for (WorkbenchRecipe recipe : workbench.getRecipes()) {
                ItemStack result = recipe.getResult().clone();
                items.add(new SimpleItem(new ItemBuilder(result), click -> {
                    new RecipeViewGUI(workbench, recipe).open(player);
                }));
            }
        } else if (furnace != null) {
            for (FurnaceRecipe recipe : furnace.getRecipes()) {
                ItemStack result = recipe.getResult().clone();
                items.add(new SimpleItem(new ItemBuilder(result), click -> {
                    new RecipeViewGUI(furnace, recipe).open(player);
                }));
            }
        }
        Gui gui = PagedGui.items()
                .setStructure(
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "# # < # B # > # #"
                )
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('<', new SimpleItem(new ItemBuilder(Material.ARROW).setDisplayName("Back")))
                .addIngredient('>', new SimpleItem(new ItemBuilder(Material.ARROW).setDisplayName("Forward")))
                .addIngredient('B', new SimpleItem(new ItemBuilder(Material.BARRIER).setDisplayName("Back to menu"), click -> {
                    new StationListGUI().open(player);
                }))
                .setContent(items)
                .build();
        Window.single()
                .setViewer(player)
                .setTitle(new AdventureComponentWrapper(ChatUtil.color(title + " &8- Recipies")))
                .setGui(gui)
                .build()
                .open();
    }
}