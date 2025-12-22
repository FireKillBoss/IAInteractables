package me.FireKillGrib.iAInteractables.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.FireKillGrib.iAInteractables.Plugin;
import me.FireKillGrib.iAInteractables.data.Furnace;
import me.FireKillGrib.iAInteractables.data.Workbench;
import me.FireKillGrib.iAInteractables.managers.RecipeManager;
import me.FireKillGrib.iAInteractables.menu.WorkbenchGUI;
import me.FireKillGrib.iAInteractables.utils.ChatUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("interactables|iai")
public class MainCommand extends BaseCommand {
    private final RecipeManager recipeManager = Plugin.getInstance().getRecipeManager();

    @Default
    public void onDefault(CommandSender sender){
        ChatUtil.sendConfigMessageList(sender,"usages");
    }

    @Subcommand("workbench")
    @CommandCompletion("@workbenches @nothing")
    @CommandPermission("iainteractables.workbench.open")
    public void onWorkbench(Player player, String[] args){
        if (args.length < 1){
            ChatUtil.sendConfigMessageList(player,"usages");
            return;
        }
        Workbench workbench = recipeManager.getWorkbench(args[0]);
        if (workbench == null){
            ChatUtil.sendConfigMessage(player,"workbench-not-found");
            return;
        }
        new WorkbenchGUI(workbench).open(player);
    }

    @Subcommand("furnace")
    @CommandCompletion("@furnaces @nothing")
    @CommandPermission("iainteractables.furnace.open")
    public void onFurnace(Player player, String[] args){
        if (args.length < 1){
            ChatUtil.sendConfigMessageList(player,"usages");
            return;
        }
        Furnace furnace = recipeManager.getFurnace(args[0]);
        if (furnace == null){
            ChatUtil.sendConfigMessage(player,"furnace-not-found");
            return;
        }
    }

    @Subcommand("reload")
    @CommandPermission("iainteractables.reload")
    public void onReload(CommandSender sender){
        Plugin.getInstance().reload();
        ChatUtil.sendConfigMessage(sender,"reload");
    }
}
