package me.TurtleMod;

import me.TurtleMod.commands.*;
import me.TurtleMod.listeners.*;
import me.TurtleMod.market.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class TurtlePlugin extends JavaPlugin {
    private static TurtlePlugin instance;
    private BackManager backManager;
    private MoneyManager moneyManager;
    private MarketManager marketManager;

    @Override
    public void onEnable() {
        instance = this;
        this.backManager = new BackManager(this);
        this.moneyManager = new MoneyManager(this);
        this.marketManager = new MarketManager(this, this.moneyManager);

        getServer().getPluginManager().registerEvents(new MarketListener(this, marketManager), this);

        getCommand("market").setExecutor(new MarketCommand(marketManager));
    }

    public static TurtlePlugin getInstance() { return instance; }
    public MoneyManager getMoneyManager() { return moneyManager; }
    public BackManager getBackManager() { return backManager; }
    public MarketManager getMarketManager() { return marketManager; }
}