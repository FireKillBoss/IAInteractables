package me.FireKillGrib.iAInteractables.integration;

import lombok.Getter;
import org.bukkit.inventory.*;
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
        if (choice instanceof RecipeChoice.ExactChoice) {
            List<ItemStack> choices = ((RecipeChoice.ExactChoice) choice).getChoices();
            if (!choices.isEmpty()) {
                return choices.get(0).clone();
            }
        }
        return choice.getItemStack().clone();
    }
    private void parseShaped(ShapedRecipe recipe) {
        Map<Character, RecipeChoice> choiceMap = recipe.getChoiceMap();
        String[] shape = recipe.getShape();
        int row = 0;
        for (String line : shape) {
            int col = 0;
            for (char c : line.toCharArray()) {
                if (choiceMap.containsKey(c)) {
                    RecipeChoice choice = choiceMap.get(c);
                    ItemStack item = resolveChoice(choice);
                    if (item != null) {
                        shapedIngredients.put(row * 3 + col, item);
                    }
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