package me.TurtleMod.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SudoCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /" + label + (label.equalsIgnoreCase("sudo") ? " <player> <message>" : " <message>"));
            return true;
        }
        if (label.equalsIgnoreCase("say")) {
            String message = String.join(" ", args);
            Bukkit.broadcastMessage("§7[§9" + sender.getName() + "§7] §f" + message);
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /sudo <player> <message>");
            return true;
        }
        String targetName = args[0];
        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        Bukkit.broadcastMessage("§7[§9" + target.getName() + "§7] §f" + message);
        return true;
    }
}