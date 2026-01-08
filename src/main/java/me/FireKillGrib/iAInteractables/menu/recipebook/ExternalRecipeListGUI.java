package me.FireKillGrib.iAInteractables.menu.recipebook;

import me.FireKillGrib.iAInteractables.integration.RecipeContainer;
import me.FireKillGrib.iAInteractables.utils.ChatUtil;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.List;

public class ExternalRecipeListGUI {
    private final String displayName;
    private final List<RecipeContainer> recipes;
    public ExternalRecipeListGUI(String namespace, String displayName, List<RecipeContainer> recipes) {
        this.displayName = displayName;
        this.recipes = recipes;
    }
    public void open(Player player) {
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        List<xyz.xenondevs.invui.item.Item> items = new ArrayList<>();
        for (RecipeContainer recipe : recipes) {
            ItemStack result = recipe.getResult().clone();
            if (result.getType() == Material.AIR) result = new ItemStack(Material.BARRIER);
            items.add(new SimpleItem(new ItemBuilder(result), click -> {
                new ExternalRecipeViewGUI(recipe, displayName).open(player);
            }));
        }
        PagedGui.Builder<xyz.xenondevs.invui.item.Item> builder = PagedGui.items()
                .setStructure(
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "# # < # B # > # #"
                )
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('B', new SimpleItem(
                        new ItemBuilder(Material.BARRIER)
                            .setDisplayName(serializer.serialize(ChatUtil.color("&cBack to categories"))),
                        click -> new StationListGUI().open(player)
                ))
                .setContent(items);
        PagedGui<xyz.xenondevs.invui.item.Item> gui = builder.build();
        gui.setItem(38, new SimpleItem(new ItemProvider() {
            @Override
            public ItemStack get(String lang) {
                ItemBuilder ib;
                if (gui.hasPreviousPage()) {
                    ib = new ItemBuilder(Material.ARROW)
                        .setDisplayName(serializer.serialize(ChatUtil.color("&eBack")));
                } else {
                    ib = new ItemBuilder(Material.ARROW)
                        .setDisplayName(serializer.serialize(ChatUtil.color("&eBack")));
                }
                return ib.get(lang);
            }
        }, click -> {
            if (gui.hasPreviousPage()) gui.goBack();
        }));
        gui.setItem(42, new SimpleItem(new ItemProvider() {
            @Override
            public ItemStack get(String lang) {
                ItemBuilder ib;
                if (gui.hasNextPage()) {
                    ib = new ItemBuilder(Material.ARROW)
                        .setDisplayName(serializer.serialize(ChatUtil.color("&eForward")));
                } else {
                    ib = new ItemBuilder(Material.ARROW)
                        .setDisplayName(serializer.serialize(ChatUtil.color("&eForward")));
                }
                return ib.get(lang);
            }
        }, click -> {
            if (gui.hasNextPage()) gui.goForward();
        }));
        Window.single()
                .setViewer(player)
                .setTitle(new AdventureComponentWrapper(ChatUtil.color(displayName)))
                .setGui(gui)
                .build()
                .open();
    }
}