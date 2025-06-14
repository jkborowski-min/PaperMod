package me.TurtleMod.commands;

import me.TurtleMod.BackManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BackCommand implements CommandExecutor {
    private final BackManager backManager;

    public BackCommand(BackManager backManager) {
        this.backManager = backManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use /back!");
            return true;
        }
        Location loc = backManager.getBackLocation(player);
        if (loc == null) {
            player.sendMessage("§cNo back location found!");
            return true;
        }
        // Record current location as new "back" before teleport, for chaining
        backManager.recordBackLocation(player);
        player.teleport(loc);
        player.sendMessage("§aTeleported to your previous location!");
        return true;
    }
}