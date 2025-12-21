package me.FireKillGrib.iAInteractables.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.FireKillGrib.iAInteractables.Plugin;

public class ChatUtil {
    public static void sendMessage(Player player, String msg) {
        player.sendMessage(color(msg));
    }
    public static void sendMessage(CommandSender sender, String msg) {
        sender.sendMessage(color(msg));
    }
    public static void sendConfigMessage(CommandSender sender, String path) {
        sender.sendMessage(color(Plugin.getInstance().getConfig().getString(path,path)));
    }
    public static void sendConfigMessageList(CommandSender sender, String path) {
        for (String line: Plugin.getInstance().getConfig().getStringList(path)) {
            sender.sendMessage(color(line));
        }
    }
    public static Component color(String value) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(value)
                .decoration(TextDecoration.ITALIC, false);
    }
}
