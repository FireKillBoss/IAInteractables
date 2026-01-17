package me.FireKillGrib.iAInteractables.managers;

import dev.lone.itemsadder.api.CustomStack;
import me.FireKillGrib.iAInteractables.Plugin;
import me.FireKillGrib.iAInteractables.integration.RecipeContainer;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.inventory.*;
import java.util.*;

public class IntegrationManager {
    private final Map<String, List<RecipeContainer>> externalRecipes = new HashMap<>();
    public void loadRecipes() {
        externalRecipes.clear();
        Set<String> processedSignatures = new HashSet<>();
        List<Recipe> allRecipes = new ArrayList<>();
        Iterator<Recipe> it = Bukkit.recipeIterator();
        while (it.hasNext()) allRecipes.add(it.next());
        allRecipes.sort(Comparator.comparing(this::getPriority).reversed());
        for (Recipe recipe : allRecipes) {
            String signature = calculateSignature(recipe);
            if (processedSignatures.contains(signature)) {
                continue;
            }
            String namespace = getNamespace(recipe);
            boolean added = false;
            boolean isIaItem = false;
            if (Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
                if (CustomStack.byItemStack(recipe.getResult()) != null) {
                    isIaItem = true;
                }
            }
            if (namespace.equals("craftorithm") || namespace.equals("customcrafting")) {
                addRecipe(namespace, recipe);
                added = true;
            }
            else if (isIaItem) {
                addRecipe("itemsadder", recipe);
                added = true;
            }
            if (added) {
                processedSignatures.add(signature);
            }
        }
        Plugin.getInstance().getLogger().info("Loaded external recipes: " + externalRecipes.keySet());
    }
    private int getPriority(Recipe r) {
        String ns = getNamespace(r);
        if (ns.equals("craftorithm")) return 100;
        if (ns.equals("customcrafting")) return 50;
        if (ns.equals("itemsadder") || ns.equals("ia")) return 20;
        return 0;
    }
    private String getNamespace(Recipe recipe) {
        if (recipe instanceof Keyed) {
            return ((Keyed) recipe).getKey().getNamespace().toLowerCase();
        }
        return "minecraft";
    }
    private void addRecipe(String namespace, Recipe recipe) {
        externalRecipes.computeIfAbsent(namespace, k -> new ArrayList<>())
                .add(new RecipeContainer(recipe));
    }
    private String calculateSignature(Recipe recipe) {
        StringBuilder sb = new StringBuilder();
        sb.append(itemToString(recipe.getResult())).append("=");
        List<String> ingredients = new ArrayList<>();
        if (recipe instanceof ShapedRecipe) {
            for (RecipeChoice choice : ((ShapedRecipe) recipe).getChoiceMap().values()) {
                ingredients.add(choiceToString(choice));
            }
        } else if (recipe instanceof ShapelessRecipe) {
            for (RecipeChoice choice : ((ShapelessRecipe) recipe).getChoiceList()) {
                ingredients.add(choiceToString(choice));
            }
        } else if (recipe instanceof CookingRecipe) {
            ingredients.add(choiceToString(((CookingRecipe<?>) recipe).getInputChoice()));
        } else if (recipe instanceof SmithingRecipe) {
            SmithingRecipe sr = (SmithingRecipe) recipe;
            ingredients.add(choiceToString(sr.getBase()));
            ingredients.add(choiceToString(sr.getAddition()));
            if (recipe instanceof SmithingTransformRecipe) {
                ingredients.add(choiceToString(((SmithingTransformRecipe) recipe).getTemplate()));
            }
        }
        Collections.sort(ingredients);
        for (String ing : ingredients) sb.append(ing).append(",");
        return sb.toString();
    }
    
    @SuppressWarnings("deprecation")
    private String choiceToString(RecipeChoice choice) {
        if (choice == null) return "AIR";
        ItemStack bestMatch = null;
        if (choice instanceof RecipeChoice.ExactChoice) {
            List<ItemStack> list = ((RecipeChoice.ExactChoice) choice).getChoices();
            for (ItemStack stack : list) {
                if (stack.hasItemMeta() && stack.getItemMeta().hasCustomModelData()) {
                    bestMatch = stack;
                    break;
                }
            }
            if (bestMatch == null && !list.isEmpty()) bestMatch = list.get(0);
        }
        
        if (bestMatch == null) {
            try { bestMatch = choice.getItemStack(); } catch (Exception e) {}
        }
        
        return itemToString(bestMatch);
    }
    private String itemToString(ItemStack item) {
        if (item == null) return "AIR";
        if (Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
            CustomStack cs = CustomStack.byItemStack(item);
            if (cs != null) return "IA:" + cs.getNamespacedID();
        }
        String s = item.getType().toString();
        if (item.hasItemMeta() && item.getItemMeta().hasCustomModelData()) {
            s += "#" + item.getItemMeta().getCustomModelData();
        }
        return s;
    }
    public Map<String, List<RecipeContainer>> getExternalRecipes() {
        return externalRecipes;
    }
    public String getDisplayName(String namespace) {
        switch (namespace) {
            case "itemsadder": return "&eItemsAdder";
            case "customcrafting": return "&dCustomCrafting";
            case "craftorithm": return "&bCraftorithm";
            default: return "&7" + namespace;
        }
    }
}