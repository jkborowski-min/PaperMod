package me.TurtleMod;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

public class MoneyManager {
    private final TurtlePlugin plugin;

    public MoneyManager(TurtlePlugin plugin) {
        this.plugin = plugin;
    }

    public int getMoney(UUID uuid) {
        return plugin.getConfig().getInt("money." + uuid, 0);
    }

    public void setMoney(UUID uuid, int amount) {
        plugin.getConfig().set("money." + uuid, amount);
        plugin.saveConfig();
    }

    public void addMoney(UUID uuid, int amount) {
        setMoney(uuid, getMoney(uuid) + amount);
    }

    public void removeMoney(UUID uuid, int amount) {
        setMoney(uuid, getMoney(uuid) - amount);
    }

    public String formatMoney(int amount) {
        return NumberFormat.getInstance(Locale.US).format(amount);
    }
}