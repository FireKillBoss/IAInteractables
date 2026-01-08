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
        Set<String> explicitNamespaces = new HashSet<>();
        if (Bukkit.getPluginManager().isPluginEnabled("CustomCrafting")) explicitNamespaces.add("customcrafting");
        if (Bukkit.getPluginManager().isPluginEnabled("Craftorithm")) explicitNamespaces.add("craftorithm");
        Iterator<Recipe> it = Bukkit.recipeIterator();
        while (it.hasNext()) {
            Recipe recipe = it.next();
            String signature = calculateSignature(recipe);
            if (processedSignatures.contains(signature)) {
                continue;
            }
            String namespace = "minecraft";
            if (recipe instanceof Keyed) {
                namespace = ((Keyed) recipe).getKey().getNamespace().toLowerCase();
            }
            boolean added = false;
            if (explicitNamespaces.contains(namespace)) {
                addRecipe(namespace, recipe);
                added = true;
            }
            if (!added && Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
                if (CustomStack.byItemStack(recipe.getResult()) != null) {
                    addRecipe("itemsadder", recipe);
                    added = true;
                }
            }
            if (added) {
                processedSignatures.add(signature);
            }
        }
        Plugin.getInstance().getLogger().info("Loaded external recipes: " + externalRecipes.keySet());
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
            ShapedRecipe sr = (ShapedRecipe) recipe;
            for (RecipeChoice choice : sr.getChoiceMap().values()) {
                ingredients.add(choiceToString(choice));
            }
        } else if (recipe instanceof ShapelessRecipe) {
            ShapelessRecipe sl = (ShapelessRecipe) recipe;
            for (RecipeChoice choice : sl.getChoiceList()) {
                ingredients.add(choiceToString(choice));
            }
        } else if (recipe instanceof CookingRecipe) {
            ingredients.add(choiceToString(((CookingRecipe<?>) recipe).getInputChoice()));
        }
        Collections.sort(ingredients);
        for (String ing : ingredients) sb.append(ing).append(",");
        
        return sb.toString();
    }

    @SuppressWarnings("deprecation")
    private String choiceToString(RecipeChoice choice) {
        if (choice == null) return "null";
        ItemStack item;
        if (choice instanceof RecipeChoice.ExactChoice) {
            List<ItemStack> list = ((RecipeChoice.ExactChoice) choice).getChoices();
            item = list.isEmpty() ? new ItemStack(org.bukkit.Material.AIR) : list.get(0);
        } else {
            item = choice.getItemStack();
        }
        return itemToString(item);
    }
    private String itemToString(ItemStack item) {
        if (item == null) return "null";
        if (Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
            CustomStack cs = CustomStack.byItemStack(item);
            if (cs != null) return "IA:" + cs.getNamespacedID();
        }
        String s = item.getType().toString() + ":" + item.getAmount();
        if (item.hasItemMeta() && item.getItemMeta().hasCustomModelData()) {
            s += ":CMD" + item.getItemMeta().getCustomModelData();
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