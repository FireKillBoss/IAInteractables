package me.FireKillGrib.iAInteractables;

import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import me.FireKillGrib.iAInteractables.commands.MainCommand;
import me.FireKillGrib.iAInteractables.listeners.FurnitureListener;
import me.FireKillGrib.iAInteractables.managers.ConfigManager;
import me.FireKillGrib.iAInteractables.managers.FurnaceDataManager;
import me.FireKillGrib.iAInteractables.managers.FurnaceManager;
import me.FireKillGrib.iAInteractables.managers.InstanceManager;
import me.FireKillGrib.iAInteractables.managers.RecipeManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class Plugin extends JavaPlugin {
    @Getter
    private static Plugin instance;
    @Getter
    private RecipeManager recipeManager;
    @Getter
    private ConfigManager configManager;
    @Getter
    private InstanceManager instanceManager;
    private FurnaceDataManager furnaceDataManager;
    private FurnaceManager furnaceManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        createDefaultConfigs();
        configManager = new ConfigManager();
        recipeManager = new RecipeManager();
        instanceManager = new InstanceManager();
        PaperCommandManager manager = new PaperCommandManager(this);
        manager.registerCommand(new MainCommand());
        getServer().getPluginManager().registerEvents(new FurnitureListener(), this);
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            instanceManager.save();
        }, 6000L, 6000L);
        furnaceDataManager = new FurnaceDataManager(getDataFolder());
        furnaceManager = new FurnaceManager();
    }

    @Override
    public void onDisable() {
        furnaceManager.shutdown();
    }
    public void reload() {
        configManager.reload();
        recipeManager.loadWorkbenches();
        recipeManager.loadFurnaces();
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
    public FurnaceDataManager getFurnaceDataManager() {
        return furnaceDataManager;
    }
    public FurnaceManager getFurnaceManager() {
        return furnaceManager;
    }
}
