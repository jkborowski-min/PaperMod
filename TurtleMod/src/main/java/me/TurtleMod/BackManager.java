package me.TurtleMod;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BackManager {
    private final Map<UUID, Location> lastLocation = new HashMap<>();
    private final TurtlePlugin plugin;

    public BackManager(TurtlePlugin plugin) {
        this.plugin = plugin;
    }

    public void recordBackLocation(Player player) {
        lastLocation.put(player.getUniqueId(), player.getLocation());
    }

    public void recordBackLocation(Player player, Location location) {
        lastLocation.put(player.getUniqueId(), location);
    }

    public Location getBackLocation(Player player) {
        return lastLocation.get(player.getUniqueId());
    }
}