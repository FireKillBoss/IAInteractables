package me.FireKillGrib.iAInteractables.integration;

import lombok.Getter;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class RecipeContainer {
    public enum RecipeType {
        CRAFTING, FURNACE, SMITHING, OTHER
    }
    private final ItemStack result;
    private final RecipeType type;
    private final Map<Integer, ItemStack> shapedIngredients = new HashMap<>();
    private final List<ItemStack> shapelessIngredients = new ArrayList<>();
    private final boolean isShaped;
    private ItemStack furnaceInput;
    private float experience;
    private int cookingTime;
    private ItemStack smithingBase;
    private ItemStack smithingAddition;
    private ItemStack smithingTemplate;
    public RecipeContainer(Recipe recipe) {
        this.result = recipe.getResult();
        if (recipe instanceof ShapedRecipe) {
            this.type = RecipeType.CRAFTING;
            this.isShaped = true;
            parseShaped((ShapedRecipe) recipe);
        } 
        else if (recipe instanceof ShapelessRecipe) {
            this.type = RecipeType.CRAFTING;
            this.isShaped = false;
            parseShapeless((ShapelessRecipe) recipe);
        } 
        else if (recipe instanceof CookingRecipe) {
            this.type = RecipeType.FURNACE;
            this.isShaped = false;
            parseCooking((CookingRecipe<?>) recipe);
        }
        else if (recipe instanceof SmithingRecipe) {
            this.type = RecipeType.SMITHING;
            this.isShaped = false;
            parseSmithing((SmithingRecipe) recipe);
        } 
        else {
            this.type = RecipeType.OTHER;
            this.isShaped = false;
        }
    }

    @SuppressWarnings("deprecation")
    private ItemStack resolveChoice(RecipeChoice choice) {
        if (choice == null) return null;
        ItemStack bestCandidate = null;
        if (choice instanceof RecipeChoice.ExactChoice) {
            List<ItemStack> choices = ((RecipeChoice.ExactChoice) choice).getChoices();
            for (ItemStack item : choices) {
                if (item.hasItemMeta() && item.getItemMeta().hasCustomModelData()) {
                    bestCandidate = item.clone();
                    break; 
                }
            }
            if (bestCandidate == null && !choices.isEmpty()) {
                bestCandidate = choices.get(0).clone();
            }
        }
        if (bestCandidate == null) {
            try {
                bestCandidate = choice.getItemStack().clone();
            } catch (Exception e) {
                return null;
            }
        }
        return fixPotionVisual(bestCandidate);
    }
    private ItemStack fixPotionVisual(ItemStack item) {
        if (item.getType() == org.bukkit.Material.POTION || 
            item.getType() == org.bukkit.Material.SPLASH_POTION || 
            item.getType() == org.bukkit.Material.LINGERING_POTION) {
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof PotionMeta) {
                PotionMeta pm = (PotionMeta) meta;
                if (pm.getBasePotionType() == null) {
                    if (pm.hasCustomEffects()) {
                        pm.setBasePotionType(PotionType.AWKWARD);
                    } else {
                        pm.setBasePotionType(PotionType.WATER);
                    }
                    item.setItemMeta(pm);
                }
            }
        }
        return item;
    }
    private void parseShaped(ShapedRecipe recipe) {
        Map<Character, RecipeChoice> choiceMap = recipe.getChoiceMap();
        String[] shape = recipe.getShape();
        int row = 0;
        for (String line : shape) {
            int col = 0;
            for (char c : line.toCharArray()) {
                if (choiceMap.containsKey(c)) {
                    ItemStack item = resolveChoice(choiceMap.get(c));
                    if (item != null) shapedIngredients.put(row * 3 + col, item);
                }
                col++;
            }
            row++;
        }
    }
    private void parseShapeless(ShapelessRecipe recipe) {
        for (RecipeChoice choice : recipe.getChoiceList()) {
            ItemStack item = resolveChoice(choice);
            if (item != null) shapelessIngredients.add(item);
        }
    }
    private void parseCooking(CookingRecipe<?> recipe) {
        this.furnaceInput = resolveChoice(recipe.getInputChoice());
        this.experience = recipe.getExperience();
        this.cookingTime = recipe.getCookingTime();
    }
    private void parseSmithing(SmithingRecipe recipe) {
        this.smithingBase = resolveChoice(recipe.getBase());
        this.smithingAddition = resolveChoice(recipe.getAddition());
        if (recipe instanceof SmithingTransformRecipe) {
            this.smithingTemplate = resolveChoice(((SmithingTransformRecipe) recipe).getTemplate());
        } else if (recipe instanceof SmithingTrimRecipe) {
            this.smithingTemplate = resolveChoice(((SmithingTrimRecipe) recipe).getTemplate());
        }
    }
}