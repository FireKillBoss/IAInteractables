package me.FireKillGrib.iAInteractables.menu.recipebook;

import dev.lone.itemsadder.api.CustomStack;
import me.FireKillGrib.iAInteractables.Plugin;
import me.FireKillGrib.iAInteractables.data.Furnace;
import me.FireKillGrib.iAInteractables.data.SmithingTable;
import me.FireKillGrib.iAInteractables.data.Workbench;
import me.FireKillGrib.iAInteractables.utils.ChatUtil;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
import java.util.Map;
import me.FireKillGrib.iAInteractables.integration.RecipeContainer;

public class StationListGUI {
    public void open(Player player) {
        List<String> hidden = Plugin.getInstance().getConfig().getStringList("recipe-book.hidden-stations");
        List<xyz.xenondevs.invui.item.Item> items = new ArrayList<>();
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        for (Workbench wb : Plugin.getInstance().getRecipeManager().getWorkbenches()) {
            if (hidden.contains(wb.getName())) continue;
            ItemStack iconStack;
            if (wb.getNamespacedID() != null && CustomStack.getInstance(wb.getNamespacedID()) != null) {
                iconStack = CustomStack.getInstance(wb.getNamespacedID()).getItemStack();
            } else {
                iconStack = new ItemStack(Material.CRAFTING_TABLE);
            }
            ItemBuilder iconBuilder = new ItemBuilder(iconStack)
                    .setDisplayName(serializer.serialize(ChatUtil.color("&e" + wb.getTitle())))
                    .addLoreLines(
                        serializer.serialize(ChatUtil.color("&7Click to see recipies")),
                        serializer.serialize(ChatUtil.color("&7Type: workbench"))
                    );
            items.add(new SimpleItem(iconBuilder, click -> {
                new RecipeListGUI(wb).open(player);
            }));
        }
        for (Furnace fn : Plugin.getInstance().getRecipeManager().getFurnaces()) {
            if (hidden.contains(fn.getName())) continue;
            ItemStack iconStack;
            if (fn.getNamespacedID() != null && CustomStack.getInstance(fn.getNamespacedID()) != null) {
                iconStack = CustomStack.getInstance(fn.getNamespacedID()).getItemStack();
            } else {
                iconStack = new ItemStack(Material.FURNACE);
            }
            ItemBuilder iconBuilder = new ItemBuilder(iconStack)
                    .setDisplayName(serializer.serialize(ChatUtil.color("&e" + fn.getTitle())))
                    .addLoreLines(
                        serializer.serialize(ChatUtil.color("&7Click to see recipies")),
                        serializer.serialize(ChatUtil.color("&7Type: furnace"))
                    );
            items.add(new SimpleItem(iconBuilder, click -> {
                new RecipeListGUI(fn).open(player);
            }));
        }
        for (SmithingTable st : Plugin.getInstance().getRecipeManager().getSmithingTables()) {
            if (hidden.contains(st.getName())) continue;
            ItemStack iconStack;
            if (st.getNamespacedID() != null && CustomStack.getInstance(st.getNamespacedID()) != null) {
                iconStack = CustomStack.getInstance(st.getNamespacedID()).getItemStack();
            } else {
                iconStack = new ItemStack(Material.SMITHING_TABLE);
            }
            ItemBuilder iconBuilder = new ItemBuilder(iconStack)
                    .setDisplayName(serializer.serialize(ChatUtil.color("&e" + st.getTitle())))
                    .addLoreLines(
                            serializer.serialize(ChatUtil.color("&7Click to see recipies")),
                            serializer.serialize(ChatUtil.color("&7Type: smithing table"))
                    );
            items.add(new SimpleItem(iconBuilder, click -> {
                new RecipeListGUI(st).open(player);
            }));
        }
        Map<String, List<RecipeContainer>> externalRecipes = Plugin.getInstance().getIntegrationManager().getExternalRecipes();
        for (Map.Entry<String, List<RecipeContainer>> entry : externalRecipes.entrySet()) {
            String namespace = entry.getKey();
            List<RecipeContainer> recipes = entry.getValue();
            if (recipes.isEmpty()) continue;
            String displayName = Plugin.getInstance().getIntegrationManager().getDisplayName(namespace);
            ItemBuilder iconBuilder = new ItemBuilder(Material.ENDER_CHEST)
                    .setDisplayName(serializer.serialize(ChatUtil.color(displayName)))
                    .addLoreLines(
                        serializer.serialize(ChatUtil.color("&7Click to see recipies")),
                        serializer.serialize(ChatUtil.color("&7Total recipies: &e" + recipes.size())),
                        serializer.serialize(ChatUtil.color("&7Plugin: &f" + namespace))
                    );
            items.add(new SimpleItem(iconBuilder, click -> {
                new ExternalRecipeListGUI(namespace, displayName, recipes).open(player);
            }));
        }
        Gui gui = PagedGui.items()
                .setStructure(
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "# # < # C # > # #"
                )
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('<', new SimpleItem(new ItemBuilder(Material.ARROW)
                        .setDisplayName(serializer.serialize(ChatUtil.color("&eBack")))))
                .addIngredient('>', new SimpleItem(new ItemBuilder(Material.ARROW)
                        .setDisplayName(serializer.serialize(ChatUtil.color("&eForward")))))
                .addIngredient('C', new SimpleItem(new ItemBuilder(Material.BARRIER)
                        .setDisplayName(serializer.serialize(ChatUtil.color("&cClose"))), 
                        click -> click.getPlayer().closeInventory()))
                .setContent(items)
                .build();
        Window.single()
                .setViewer(player)
                .setTitle(new AdventureComponentWrapper(ChatUtil.color(Plugin.getInstance().getConfig().getString("recipe-book.gui-title"))))
                .setGui(gui)
                .build()
                .open();
    }
}