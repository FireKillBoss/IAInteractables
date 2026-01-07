package me.FireKillGrib.iAInteractables.listeners;

import dev.lone.itemsadder.api.CustomStack;
import me.FireKillGrib.iAInteractables.Plugin;
import me.FireKillGrib.iAInteractables.menu.recipebook.StationListGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class RecipeBookListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!Plugin.getInstance().getConfig().getBoolean("recipe-book.enabled")) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) return;
        String configItem = Plugin.getInstance().getConfig().getString("recipe-book.item");
        if (configItem == null) return;
        boolean isMatch = false;
        if (configItem.toLowerCase().startsWith("ia-") || configItem.contains(":")) {
            String targetID = configItem.replace("ia-", "");
            CustomStack stack = CustomStack.byItemStack(item);
            if (stack != null && stack.getNamespacedID().equals(targetID)) {
                isMatch = true;
            }
        } else {
            try {
                Material mat = Material.valueOf(configItem.toUpperCase());
                if (item.getType() == mat) {
                    isMatch = true;
                }
            } catch (IllegalArgumentException e) {
            }
        }
        if (isMatch) {
            Player player = event.getPlayer();
            event.setCancelled(true);
            new StationListGUI().open(player);
        }
    }
}