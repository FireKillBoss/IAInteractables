package me.FireKillGrib.iAInteractables.menu.recipebook;

import me.FireKillGrib.iAInteractables.Plugin;
import me.FireKillGrib.iAInteractables.data.Furnace;
import me.FireKillGrib.iAInteractables.data.Workbench;
import me.FireKillGrib.iAInteractables.utils.ChatUtil;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;
import java.util.ArrayList;
import java.util.List;

public class StationListGUI {
    public void open(Player player) {
        List<String> hidden = Plugin.getInstance().getConfig().getStringList("recipe-book.hidden-stations");
        List<xyz.xenondevs.invui.item.Item> items = new ArrayList<>();
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        for (Workbench wb : Plugin.getInstance().getRecipeManager().getWorkbenches()) {
            if (hidden.contains(wb.getName())) continue;
            ItemBuilder iconBuilder = new ItemBuilder(Material.CRAFTING_TABLE)
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
            ItemBuilder iconBuilder = new ItemBuilder(Material.FURNACE)
                    .setDisplayName(serializer.serialize(ChatUtil.color("&e" + fn.getTitle())))
                    .addLoreLines(
                        serializer.serialize(ChatUtil.color("&7Click to see recipies")),
                        serializer.serialize(ChatUtil.color("&7Type: furnace"))
                    );
            items.add(new SimpleItem(iconBuilder, click -> {
                new RecipeListGUI(fn).open(player);
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
                        .setDisplayName(serializer.serialize(ChatUtil.color("Back")))))
                .addIngredient('>', new SimpleItem(new ItemBuilder(Material.ARROW)
                        .setDisplayName(serializer.serialize(ChatUtil.color("Forward")))))
                .addIngredient('C', new SimpleItem(new ItemBuilder(Material.BARRIER)
                        .setDisplayName(serializer.serialize(ChatUtil.color("Close"))), 
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