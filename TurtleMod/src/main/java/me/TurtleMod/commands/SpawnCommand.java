package me.TurtleMod.commands;

import me.TurtleMod.BackManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {
    private final BackManager backManager;

    public SpawnCommand(BackManager backManager) {
        this.backManager = backManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use /spawn!");
            return true;
        }
        World world = Bukkit.getWorlds().get(0); // Overworld
        if (world == null) {
            player.sendMessage("World not found!");
            return true;
        }
        backManager.recordBackLocation(player); // Record last location
        player.teleport(new Location(world, 0, 10, 0));
        player.sendMessage("§aTeleported to spawn!");
        return true;
    }
}