package me.FireKillGrib.iAInteractables.managers;

import me.FireKillGrib.iAInteractables.data.Furnace;
import org.bukkit.Location;
import java.util.HashMap;
import java.util.Map;

public class FurnaceManager {
    private final Map<String, FurnaceController> activeControllers = new HashMap<>();
    public FurnaceController getOrCreate(Furnace furnace, Location location) {
        String key = locationToKey(location);
        return activeControllers.computeIfAbsent(key, k -> new FurnaceController(furnace, location));
    }
    public void remove(Location location) {
        String key = locationToKey(location);
        FurnaceController controller = activeControllers.remove(key);
        if (controller != null) {
            controller.shutdown();
        }
    }
    public void shutdown() {
        activeControllers.values().forEach(FurnaceController::shutdown);
        activeControllers.clear();
    }
    private String locationToKey(Location loc) {
        return loc.getWorld().getName() + "_" + 
            loc.getBlockX() + "_" + 
            loc.getBlockY() + "_" + 
            loc.getBlockZ();
    }
}
