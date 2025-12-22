package me.FireKillGrib.iAInteractables.listeners;

import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.Events.FurnitureBreakEvent;
import dev.lone.itemsadder.api.Events.FurnitureInteractEvent;
import me.FireKillGrib.iAInteractables.Plugin;
import me.FireKillGrib.iAInteractables.data.Furnace;
import me.FireKillGrib.iAInteractables.data.FurnaceInstance;
import me.FireKillGrib.iAInteractables.data.Workbench;
import me.FireKillGrib.iAInteractables.menu.FurnaceGUI;
import me.FireKillGrib.iAInteractables.menu.WorkbenchGUI;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.io.File;

public class FurnitureListener implements Listener {

    @EventHandler
    public void onFurnitureInteract(FurnitureInteractEvent event) {
        Player player = event.getPlayer();
        String furnitureId = event.getNamespacedID();
        String name = furnitureId.contains(":") 
            ? furnitureId.split(":")[1] 
            : furnitureId;
        Furnace furnace = Plugin.getInstance().getRecipeManager().getFurnace(name);
        if (furnace != null) {
            event.setCancelled(true);
            new FurnaceGUI(furnace, event.getBukkitEntity().getLocation()).open(player);
            return;
        }
        Workbench workbench = Plugin.getInstance().getRecipeManager().getWorkbench(name);
        if (workbench != null) {
            event.setCancelled(true);
            new WorkbenchGUI(workbench).open(player);
        }
    }
    @EventHandler
    public void onFurnitureBreak(FurnitureBreakEvent event) {
        Entity entity = event.getBukkitEntity();
        CustomFurniture furniture = CustomFurniture.byAlreadySpawned(entity);
        if (furniture == null) return;
        String furnitureId = furniture.getNamespacedID();
        Location loc = entity.getLocation();
        String furnaceType = getTypeByFurniture(furnitureId, "furnaces");
        if (furnaceType != null) {
            FurnaceInstance instance = Plugin.getInstance().getInstanceManager()
                    .getFurnaceInstance(loc, furnaceType);
            for (org.bukkit.inventory.ItemStack item : instance.getSlots().values()) {
                if (item != null && !item.getType().isAir()) {
                    loc.getWorld().dropItemNaturally(loc, item);
                }
            }
            if (instance.getResult() != null) {
                loc.getWorld().dropItemNaturally(loc, instance.getResult());
            }
            Plugin.getInstance().getInstanceManager().removeInstance(loc);
        }
    }
    private String getTypeByFurniture(String furnitureId, String folder) {
        File dir = new File(Plugin.getInstance().getDataFolder(), folder);
        if (!dir.exists()) return null;
        File[] files = dir.listFiles((d, name) -> name.endsWith(".yml"));
        if (files == null) return null;
        for (File file : files) {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            String configFurnitureId = cfg.getString("itemsadder-furniture");
            if (furnitureId.equals(configFurnitureId)) {
                return file.getName().replace(".yml", "");
            }
        }
        return null;
    }
}
