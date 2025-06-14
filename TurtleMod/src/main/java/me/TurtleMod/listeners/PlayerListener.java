package me.TurtleMod.listeners;

import me.TurtleMod.BackManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class PlayerListener implements Listener {
    private final BackManager backManager;
    private final me.TurtleMod.TurtlePlugin plugin;

    public PlayerListener(me.TurtleMod.TurtlePlugin plugin, BackManager backManager) {
        this.plugin = plugin;
        this.backManager = backManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage("§a" + event.getPlayer().getName() + " has arrived to join all the turtles");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage("§e" + event.getPlayer().getName() + " has gone for a break from the turtles");
    }

    // Format all chat messages
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        String name = event.getPlayer().getName();
        String message = event.getMessage();
        event.getPlayer().getServer().broadcastMessage("§7[§9" + name + "§7] §f" + message);
    }

    // Prevent /spawnpoint as player or console
    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        if (event.getCommand().toLowerCase().startsWith("spawnpoint")) {
            event.setCancelled(true);
            event.getSender().sendMessage("§c/spawnpoint is disabled on this server.");
        }
    }
}