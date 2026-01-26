package me.FireKillGrib.iAInteractables.managers;

import dev.lone.itemsadder.api.CustomStack;
import me.FireKillGrib.iAInteractables.Plugin;
import me.FireKillGrib.iAInteractables.integration.RecipeContainer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.inventory.*;
import java.util.*;

public class IntegrationManager {
    private final Map<String, List<RecipeContainer>> externalRecipes = new HashMap<>();
    public void loadRecipes() {
        externalRecipes.clear();
        Map<String, List<RecipeInfo>> signatureMap = new HashMap<>();
        Iterator<Recipe> it = Bukkit.recipeIterator();
        while (it.hasNext()) {
            Recipe recipe = it.next();
            if (recipe instanceof Keyed) {
                if (((Keyed) recipe).getKey().getNamespace().startsWith("zzzfake_")) continue;
            }
            String signature = calculateSignature(recipe);
            String namespace = resolveNamespace(recipe);
            if (!isPluginRecipe(namespace, recipe)) continue;
            signatureMap.computeIfAbsent(signature, k -> new ArrayList<>())
                    .add(new RecipeInfo(namespace, recipe));
        }
        for (List<RecipeInfo> duplicates : signatureMap.values()) {
            RecipeInfo best = selectBestRecipe(duplicates);
            if (best != null) {
                addRecipe(best.namespace, best.recipe);
            }
        }
        for (List<RecipeContainer> list : externalRecipes.values()) {
            list.sort((r1, r2) -> {
                String name1 = getItemName(r1.getResult());
                String name2 = getItemName(r2.getResult());
                return name1.compareToIgnoreCase(name2);
            });
        }
        Plugin.getInstance().getLogger().info("Loaded external recipes: " + externalRecipes.keySet());
    }
    private String getItemName(ItemStack item) {
        if (item == null) return "";
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName());
        }
        return item.getType().name();
    }
    private String resolveNamespace(Recipe recipe) {
        if (recipe instanceof Keyed) {
            String ns = ((Keyed) recipe).getKey().getNamespace().toLowerCase();
            if (ns.equals("ia")) return "itemsadder";
            if (ns.equals("cc")) return "customcrafting";
            return ns;
        }
        return "unknown";
    }
    private boolean isPluginRecipe(String namespace, Recipe recipe) {
        if (namespace.equals("itemsadder") || 
            namespace.equals("customcrafting") || 
            namespace.equals("craftorithm")) {
            return true;
        }
        if (Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
            if (CustomStack.byItemStack(recipe.getResult()) != null) {
                return true;
            }
        }
        return false;
    }
    private RecipeInfo selectBestRecipe(List<RecipeInfo> candidates) {
        if (candidates.isEmpty()) return null;
        if (candidates.size() == 1) return candidates.get(0);
        for (RecipeInfo info : candidates) {
            if (info.namespace.equals("itemsadder")) return info;
        }
        for (RecipeInfo info : candidates) {
            if (info.namespace.equals("customcrafting")) return info;
        }
        return candidates.get(0);
    }
    private void addRecipe(String namespace, Recipe recipe) {
        if (Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
            if (CustomStack.byItemStack(recipe.getResult()) != null) {
                namespace = "itemsadder";
            }
        }
        externalRecipes.computeIfAbsent(namespace, k -> new ArrayList<>())
                .add(new RecipeContainer(recipe));
    }
    private static class RecipeInfo {
        String namespace;
        Recipe recipe;
        RecipeInfo(String n, Recipe r) { namespace = n; recipe = r; }
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
            try {
                if (recipe instanceof SmithingTransformRecipe) {
                    ingredients.add(choiceToString(((SmithingTransformRecipe) recipe).getTemplate()));
                } else if (recipe instanceof SmithingTrimRecipe) {
                    ingredients.add(choiceToString(((SmithingTrimRecipe) recipe).getTemplate()));
                }
            } catch (Throwable ignored) {}
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

    @SuppressWarnings("deprecation")
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