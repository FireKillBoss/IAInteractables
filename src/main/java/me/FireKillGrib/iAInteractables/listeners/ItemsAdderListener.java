package me.FireKillGrib.iAInteractables.listeners;

import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import me.FireKillGrib.iAInteractables.Plugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ItemsAdderListener implements Listener {

    @EventHandler
    public void onItemsAdderLoad(ItemsAdderLoadDataEvent event) {
        Plugin.getInstance().getLogger().info("ItemsAdder loaded! Reloading external recipes...");
        Plugin.getInstance().getIntegrationManager().loadRecipes();
    }
}