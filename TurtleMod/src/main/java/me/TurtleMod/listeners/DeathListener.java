package me.TurtleMod.listeners;

import me.TurtleMod.BackManager;
import me.TurtleMod.TurtlePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class DeathListener implements Listener {
    private final TurtlePlugin plugin;
    private final BackManager backManager;

    public DeathListener(TurtlePlugin plugin, BackManager backManager) {
        this.plugin = plugin;
        this.backManager = backManager;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        double finalHealth = player.getHealth() - event.getFinalDamage();
        if (finalHealth <= 0) {
            backManager.recordBackLocation(player, player.getLocation());

            event.setCancelled(true);
            World world = Bukkit.getWorlds().get(0); // Overworld
            player.setHealth(Math.max(1.0, player.getMaxHealth()));
            player.teleport(new Location(world, 0, 10, 0));

            Bukkit.broadcastMessage("§c" + player.getName() + " did not have the talent of a turtle");
        }
    }
}