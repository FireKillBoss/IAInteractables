package me.FireKillGrib.iAInteractables;

import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import me.FireKillGrib.iAInteractables.commands.MainCommand;
import me.FireKillGrib.iAInteractables.listeners.FurnitureListener;
import me.FireKillGrib.iAInteractables.managers.*;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class Plugin extends JavaPlugin {
    @Getter private static Plugin instance;
    @Getter private RecipeManager recipeManager;
    @Getter private ConfigManager configManager;
    @Getter private InstanceManager instanceManager;
    @Getter private FurnaceDataManager furnaceDataManager;
    @Getter private FurnaceManager furnaceManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        createDefaultConfigs();
        configManager = new ConfigManager();
        recipeManager = new RecipeManager();
        instanceManager = new InstanceManager();
        furnaceDataManager = new FurnaceDataManager(getDataFolder());
        furnaceManager = new FurnaceManager();
        PaperCommandManager manager = new PaperCommandManager(this);
        manager.registerCommand(new MainCommand());
        getServer().getPluginManager().registerEvents(new FurnitureListener(), this);
        reload(); 
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            furnaceManager.saveAll();
        }, 6000L, 6000L);
    }

    @Override
    public void onDisable() {
        if (furnaceManager != null) {
            furnaceManager.shutdown();
        }
    }
    public void reload() {
        if (configManager != null) configManager.reload();
        if (recipeManager != null) {
            recipeManager.clearAll();
            recipeManager.loadFurnaces();
            recipeManager.loadWorkbenches();
        }
    }
    private void createDefaultConfigs() {
        File furnacesFolder = new File(getDataFolder(), "furnaces");
        File workbenchesFolder = new File(getDataFolder(), "workbenches");
        if (!furnacesFolder.exists()) {
            furnacesFolder.mkdirs();
            saveResource("furnaces/default.yml", false);
        }
        if (!workbenchesFolder.exists()) {
            workbenchesFolder.mkdirs();
            saveResource("workbenches/default.yml", false);
        }
    }
}