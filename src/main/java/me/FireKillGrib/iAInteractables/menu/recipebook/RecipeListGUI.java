package me.FireKillGrib.iAInteractables.menu.recipebook;

import me.FireKillGrib.iAInteractables.data.*;
import me.FireKillGrib.iAInteractables.utils.ChatUtil;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;
import java.util.ArrayList;
import java.util.List;

public class RecipeListGUI {
    private final Workbench workbench;
    private final Furnace furnace;
    private final SmithingTable smithingTable;
    private final String title;
    public RecipeListGUI(Workbench workbench) {
        this.workbench = workbench;
        this.furnace = null;
        this.smithingTable = null;
        this.title = workbench.getTitle();
    }
    public RecipeListGUI(Furnace furnace) {
        this.furnace = furnace;
        this.workbench = null;
        this.smithingTable = null;
        this.title = furnace.getTitle();
    }
    public RecipeListGUI(SmithingTable smithingTable) {
        this.smithingTable = smithingTable;
        this.workbench = null;
        this.furnace = null;
        this.title = smithingTable.getTitle();
    }
    public void open(Player player) {
        List<xyz.xenondevs.invui.item.Item> items = new ArrayList<>();
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        if (workbench != null) {
            for (WorkbenchRecipe recipe : workbench.getRecipes()) {
                addItem(items, recipe.getResult(), player, () -> new RecipeViewGUI(workbench, recipe).open(player));
            }
        } else if (furnace != null) {
            for (FurnaceRecipe recipe : furnace.getRecipes()) {
                addItem(items, recipe.getResult(), player, () -> new RecipeViewGUI(furnace, recipe).open(player));
            }
        } else if (smithingTable != null) {
            for (SmithingRecipe recipe : smithingTable.getRecipes()) {
                addItem(items, recipe.getResult(), player, () -> new RecipeViewGUI(smithingTable, recipe).open(player));
            }
        }
        items.sort((i1, i2) -> {
            String n1 = getNameFromProvider(i1.getItemProvider());
            String n2 = getNameFromProvider(i2.getItemProvider());
            return n1.compareToIgnoreCase(n2);
        });
        Gui gui = PagedGui.items()
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
                            .setDisplayName(serializer.serialize(ChatUtil.color("&cBack to menu"))), 
                        click -> new StationListGUI().open(player)
                ))
                .setContent(items)
                .build();
        if (gui instanceof PagedGui) {
            PagedGui<?> pagedGui = (PagedGui<?>) gui;
            LinkedPageButton backBtn = new LinkedPageButton(false, pagedGui);
            LinkedPageButton fwdBtn = new LinkedPageButton(true, pagedGui);
            backBtn.setPartner(fwdBtn);
            fwdBtn.setPartner(backBtn);
            gui.setItem(38, backBtn);
            gui.setItem(42, fwdBtn);
        }
        Window.single()
                .setViewer(player)
                .setTitle(new AdventureComponentWrapper(ChatUtil.color(title + " &8- Recipies")))
                .setGui(gui)
                .build()
                .open();
    }
    private void addItem(List<xyz.xenondevs.invui.item.Item> items, ItemStack result, Player player, Runnable action) {
        items.add(new SimpleItem(new ItemBuilder(result.clone()), click -> action.run()));
    }
    private String getNameFromProvider(ItemProvider provider) {
        try {
            ItemStack stack = provider.get(null);
            if (stack.hasItemMeta() && stack.getItemMeta().hasDisplayName()) {
                return PlainTextComponentSerializer.plainText().serialize(stack.getItemMeta().displayName());
            }
            return stack.getType().name();
        } catch (Exception e) { return ""; }
    }
    private static class LinkedPageButton extends AbstractItem {
        private final boolean forward;
        private final PagedGui<?> gui;
        private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        private LinkedPageButton partner;
        public LinkedPageButton(boolean forward, PagedGui<?> gui) {
            this.forward = forward;
            this.gui = gui;
        }
        public void setPartner(LinkedPageButton partner) {
            this.partner = partner;
        }

        @Override
        public ItemProvider getItemProvider() {
            ItemBuilder builder;
            if (forward) {
                if (gui.hasNextPage()) {
                    builder = new ItemBuilder(Material.ARROW)
                            .setDisplayName(serializer.serialize(ChatUtil.color("&eForward")))
                            .addLoreLines(serializer.serialize(ChatUtil.color("&7Page " + (gui.getCurrentPage() + 2) + " / " + gui.getPageAmount())));
                } else {
                    builder = new ItemBuilder(Material.GRAY_DYE)
                            .setDisplayName(serializer.serialize(ChatUtil.color("&7No next pages")));
                }
            } else {
                if (gui.hasPreviousPage()) {
                    builder = new ItemBuilder(Material.ARROW)
                            .setDisplayName(serializer.serialize(ChatUtil.color("&eBack")))
                            .addLoreLines(serializer.serialize(ChatUtil.color("&7Page " + (gui.getCurrentPage()) + " / " + gui.getPageAmount())));
                } else {
                    builder = new ItemBuilder(Material.GRAY_DYE)
                            .setDisplayName(serializer.serialize(ChatUtil.color("&7No previous pages")));
                }
            }
            return builder;
        }

        @Override
        public void handleClick(ClickType clickType, Player player, InventoryClickEvent event) {
            if (forward) {
                if (gui.hasNextPage()) {
                    gui.goForward();
                    updateBoth();
                }
            } else {
                if (gui.hasPreviousPage()) {
                    gui.goBack();
                    updateBoth();
                }
            }
        }
        private void updateBoth() {
            this.notifyWindows();
            if (partner != null) partner.notifyWindows();
        }
    }
}