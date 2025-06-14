package me.TurtleMod.market;

import me.TurtleMod.MoneyManager;
import me.TurtleMod.TurtlePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class MarketManager {
    private final TurtlePlugin plugin;
    private final MoneyManager moneyManager;

    private final Map<Material, List<BuyOffer>> buyOffers = new HashMap<>();
    private final Map<Material, List<SellOffer>> sellOffers = new HashMap<>();
    private final Map<UUID, List<MarketOffer>> playerOffers = new HashMap<>();

    private final File file;
    private final YamlConfiguration config;

    public MarketManager(TurtlePlugin plugin, MoneyManager moneyManager) {
        this.plugin = plugin;
        this.moneyManager = moneyManager;
        this.file = new File(plugin.getDataFolder(), "market.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        loadOffers();
    }

    public List<BuyOffer> getBuyOffers(Material mat) { return buyOffers.getOrDefault(mat, new ArrayList<>()); }
    public List<SellOffer> getSellOffers(Material mat) { return sellOffers.getOrDefault(mat, new ArrayList<>()); }
    public List<MarketOffer> getPlayerOffers(UUID uuid) { return playerOffers.getOrDefault(uuid, new ArrayList<>()); }

    public void addBuyOffer(BuyOffer offer) {
        buyOffers.computeIfAbsent(offer.material, k -> new LinkedList<>()).add(offer);
        playerOffers.computeIfAbsent(offer.player, k -> new LinkedList<>()).add(offer);
        buyOffers.get(offer.material).sort((a, b) -> Integer.compare(b.pricePerUnit, a.pricePerUnit));
        saveOffers();
    }

    public void addSellOffer(SellOffer offer) {
        sellOffers.computeIfAbsent(offer.material, k -> new LinkedList<>()).add(offer);
        playerOffers.computeIfAbsent(offer.player, k -> new LinkedList<>()).add(offer);
        sellOffers.get(offer.material).sort(Comparator.comparingInt(o -> o.pricePerUnit));
        saveOffers();
    }

    public void removeOffer(MarketOffer offer) {
        if (offer.isBuy()) buyOffers.getOrDefault(offer.material, new LinkedList<>()).remove(offer);
        else sellOffers.getOrDefault(offer.material, new LinkedList<>()).remove(offer);
        playerOffers.getOrDefault(offer.player, new LinkedList<>()).remove(offer);
        saveOffers();
    }

    public void saveOffers() {
        config.set("offers", null);
        int i = 0;
        for (List<BuyOffer> list : buyOffers.values()) {
            for (BuyOffer o : list) {
                config.set("offers.buy." + i + ".player", o.player.toString());
                config.set("offers.buy." + i + ".material", o.material.name());
                config.set("offers.buy." + i + ".amount", o.amount);
                config.set("offers.buy." + i + ".price", o.pricePerUnit);
                i++;
            }
        }
        i = 0;
        for (List<SellOffer> list : sellOffers.values()) {
            for (SellOffer o : list) {
                config.set("offers.sell." + i + ".player", o.player.toString());
                config.set("offers.sell." + i + ".material", o.material.name());
                config.set("offers.sell." + i + ".amount", o.amount);
                config.set("offers.sell." + i + ".price", o.pricePerUnit);
                i++;
            }
        }
        try { config.save(file); } catch (IOException ignored) {}
    }

    public void loadOffers() {
        buyOffers.clear();
        sellOffers.clear();
        playerOffers.clear();
        if (config.contains("offers.buy")) {
            for (String idx : config.getConfigurationSection("offers.buy").getKeys(false)) {
                UUID uuid = UUID.fromString(config.getString("offers.buy." + idx + ".player"));
                Material mat = Material.valueOf(config.getString("offers.buy." + idx + ".material"));
                int amt = config.getInt("offers.buy." + idx + ".amount");
                int price = config.getInt("offers.buy." + idx + ".price");
                BuyOffer offer = new BuyOffer(uuid, mat, amt, price);
                buyOffers.computeIfAbsent(mat, k -> new LinkedList<>()).add(offer);
                playerOffers.computeIfAbsent(uuid, k -> new LinkedList<>()).add(offer);
            }
        }
        if (config.contains("offers.sell")) {
            for (String idx : config.getConfigurationSection("offers.sell").getKeys(false)) {
                UUID uuid = UUID.fromString(config.getString("offers.sell." + idx + ".player"));
                Material mat = Material.valueOf(config.getString("offers.sell." + idx + ".material"));
                int amt = config.getInt("offers.sell." + idx + ".amount");
                int price = config.getInt("offers.sell." + idx + ".price");
                SellOffer offer = new SellOffer(uuid, mat, amt, price);
                sellOffers.computeIfAbsent(mat, k -> new LinkedList<>()).add(offer);
                playerOffers.computeIfAbsent(uuid, k -> new LinkedList<>()).add(offer);
            }
        }
    }
}