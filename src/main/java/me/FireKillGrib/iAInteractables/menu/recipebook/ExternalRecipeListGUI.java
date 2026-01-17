package me.FireKillGrib.iAInteractables.menu.recipebook;

import me.FireKillGrib.iAInteractables.integration.RecipeContainer;
import me.FireKillGrib.iAInteractables.utils.ChatUtil;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
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
    private final String namespace;
    private final String displayName;
    private final List<RecipeContainer> recipes;
    public ExternalRecipeListGUI(String namespace, String displayName, List<RecipeContainer> recipes) {
        this.namespace = namespace;
        this.displayName = displayName;
        this.recipes = recipes;
    }
    public void open(Player player) {
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        List<xyz.xenondevs.invui.item.Item> items = new ArrayList<>();
        for (RecipeContainer recipe : recipes) {
            ItemStack result = fixPotionVisual(recipe.getResult().clone());
            if (result.getType() == Material.AIR) result = new ItemStack(Material.BARRIER);
            items.add(new SimpleItem(new ItemBuilder(result), click -> {
                new ExternalRecipeViewGUI(recipe, namespace, displayName, recipes).open(player);
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
        updateNavigationButtons(gui, serializer);
        Window.single()
                .setViewer(player)
                .setTitle(new AdventureComponentWrapper(ChatUtil.color(displayName)))
                .setGui(gui)
                .build()
                .open();
    }
    private ItemStack fixPotionVisual(ItemStack item) {
        if (item.getType() == Material.POTION || item.getType() == Material.SPLASH_POTION || item.getType() == Material.LINGERING_POTION) {
            if (item.getItemMeta() instanceof PotionMeta) {
                PotionMeta meta = (PotionMeta) item.getItemMeta();
                if (meta.getBasePotionType() == null) {
                    if (meta.hasCustomEffects()) {
                        meta.setBasePotionType(PotionType.AWKWARD);
                    } else {
                        meta.setBasePotionType(PotionType.WATER);
                    }
                    item.setItemMeta(meta);
                }
            }
        }
        return item;
    }
    private void updateNavigationButtons(PagedGui<xyz.xenondevs.invui.item.Item> gui, LegacyComponentSerializer serializer) {
        
        gui.setItem(38, new SimpleItem(new ItemProvider() {
            @Override
            public ItemStack get(String lang) {
                if (gui.hasPreviousPage()) {
                    return new ItemBuilder(Material.ARROW)
                            .setDisplayName(serializer.serialize(ChatUtil.color("&eBack")))
                            .addLoreLines(serializer.serialize(ChatUtil.color("&7Page " + gui.getCurrentPage() + " / " + gui.getPageAmount())))
                            .get(lang);
                } else {
                    return new ItemBuilder(Material.GRAY_DYE)
                            .setDisplayName(serializer.serialize(ChatUtil.color("&7No next pages")))
                            .get(lang);
                }
            }
        }, click -> {
            if (gui.hasPreviousPage()) {
                gui.goBack();
                updateNavigationButtons(gui, serializer);
            }
        }));
        gui.setItem(42, new SimpleItem(new ItemProvider() {
            @Override
            public ItemStack get(String lang) {
                if (gui.hasNextPage()) {
                    return new ItemBuilder(Material.ARROW)
                            .setDisplayName(serializer.serialize(ChatUtil.color("&eForward")))
                            .addLoreLines(serializer.serialize(ChatUtil.color("&7Page " + (gui.getCurrentPage() + 2) + " / " + gui.getPageAmount())))
                            .get(lang);
                } else {
                    return new ItemBuilder(Material.GRAY_DYE)
                            .setDisplayName(serializer.serialize(ChatUtil.color("&7No next pages")))
                            .get(lang);
                }
            }
        }, click -> {
            if (gui.hasNextPage()) {
                gui.goForward();
                updateNavigationButtons(gui, serializer);
            }
        }));
    }
}