package me.FireKillGrib.iAInteractables.menu.recipebook;

import me.FireKillGrib.iAInteractables.integration.RecipeContainer;
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

public class ExternalRecipeViewGUI {
    private final RecipeContainer recipe;
    private final String namespace;
    private final String displayName;
    private final List<RecipeContainer> categoryRecipes;
    public ExternalRecipeViewGUI(RecipeContainer recipe, String namespace, String displayName, List<RecipeContainer> categoryRecipes) {
        this.recipe = recipe;
        this.namespace = namespace;
        this.displayName = displayName;
        this.categoryRecipes = categoryRecipes;
    }
    public void open(Player player) {
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        Gui.Builder.Normal guiBuilder = Gui.normal();
        SimpleItem backButton = new SimpleItem(
                new ItemBuilder(Material.BARRIER)
                    .setDisplayName(serializer.serialize(ChatUtil.color("&cBack to list"))),
                click -> new ExternalRecipeListGUI(namespace, displayName, categoryRecipes).open(player)
        );
        SimpleItem resultItem = new SimpleItem(new ItemBuilder(recipe.getResult()));
        SimpleItem filler = new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" "));
        if (recipe.getType() == RecipeContainer.RecipeType.CRAFTING) {
            guiBuilder.setStructure(
                    "A B C X X X X X X",
                    "D E F X X X R X X",
                    "G H I X X X Z X X"
            );
            guiBuilder.addIngredient('X', filler);
            guiBuilder.addIngredient('R', resultItem);
            guiBuilder.addIngredient('Z', backButton);
            if (recipe.isShaped()) {
                fillSlot(guiBuilder, 'A', 0);
                fillSlot(guiBuilder, 'B', 1);
                fillSlot(guiBuilder, 'C', 2);
                fillSlot(guiBuilder, 'D', 3);
                fillSlot(guiBuilder, 'E', 4);
                fillSlot(guiBuilder, 'F', 5);
                fillSlot(guiBuilder, 'G', 6);
                fillSlot(guiBuilder, 'H', 7);
                fillSlot(guiBuilder, 'I', 8);
            } else {
                char[] slots = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};
                for (int i = 0; i < recipe.getShapelessIngredients().size(); i++) {
                    if (i < slots.length) {
                        guiBuilder.addIngredient(slots[i], new SimpleItem(new ItemBuilder(recipe.getShapelessIngredients().get(i))));
                    }
                }
                for (int i = recipe.getShapelessIngredients().size(); i < slots.length; i++) {
                    guiBuilder.addIngredient(slots[i], new SimpleItem(new ItemBuilder(Material.AIR)));
                }
            }
        } else if (recipe.getType() == RecipeContainer.RecipeType.FURNACE) {
            guiBuilder.setStructure(
                    "X X X X X X X X X",
                    "X X I X F X R X X",
                    "X X X X X X Z X X"
            );
            guiBuilder.addIngredient('X', filler);
            guiBuilder.addIngredient('R', resultItem);
            guiBuilder.addIngredient('Z', backButton);
            if (recipe.getFurnaceInput() != null) {
                guiBuilder.addIngredient('I', new SimpleItem(new ItemBuilder(recipe.getFurnaceInput())));
            } else {
                guiBuilder.addIngredient('I', new SimpleItem(new ItemBuilder(Material.AIR)));
            }
            guiBuilder.addIngredient('F', new SimpleItem(
                    new ItemBuilder(Material.BLAZE_POWDER)
                        .setDisplayName(serializer.serialize(ChatUtil.color("&6Information")))
                        .addLoreLines(
                            serializer.serialize(ChatUtil.color("&7Time: &f" + (recipe.getCookingTime()) + "ticks")),
                            serializer.serialize(ChatUtil.color("&7Experience: &f" + recipe.getExperience()))
                        )
            ));
        } else if (recipe.getType() == RecipeContainer.RecipeType.SMITHING) {
            guiBuilder.setStructure(
                    "X X X X X X X X X",
                    "T B A X X X R X X",
                    "X X X X X X Z X X"
            );
            guiBuilder.addIngredient('X', filler);
            guiBuilder.addIngredient('R', resultItem);
            guiBuilder.addIngredient('Z', backButton);
            if (recipe.getSmithingTemplate() != null) 
                guiBuilder.addIngredient('T', new SimpleItem(new ItemBuilder(recipe.getSmithingTemplate())));
            else guiBuilder.addIngredient('T', new SimpleItem(new ItemBuilder(Material.AIR).setDisplayName("&7Template")));
            if (recipe.getSmithingBase() != null) 
                guiBuilder.addIngredient('B', new SimpleItem(new ItemBuilder(recipe.getSmithingBase())));
            else guiBuilder.addIngredient('B', new SimpleItem(new ItemBuilder(Material.AIR).setDisplayName("&7Base")));
            if (recipe.getSmithingAddition() != null) 
                guiBuilder.addIngredient('A', new SimpleItem(new ItemBuilder(recipe.getSmithingAddition())));
            else guiBuilder.addIngredient('A', new SimpleItem(new ItemBuilder(Material.AIR).setDisplayName("&7Material")));
        } else {
            guiBuilder.setStructure("X X X X R X X X B");
            guiBuilder.addIngredient('R', resultItem);
            guiBuilder.addIngredient('Z', backButton);
            guiBuilder.addIngredient('X', filler);
        }
        Window.single()
                .setTitle(new AdventureComponentWrapper(ChatUtil.color("&8View recipe")))
                .setGui(guiBuilder.build())
                .build(player)
                .open();
    }
    private void fillSlot(Gui.Builder.Normal builder, char key, int index) {
        ItemStack item = recipe.getShapedIngredients().get(index);
        if (item != null) {
            builder.addIngredient(key, new SimpleItem(new ItemBuilder(item)));
        } else {
            builder.addIngredient(key, new SimpleItem(new ItemBuilder(Material.AIR)));
        }
    }
}