package me.TurtleMod.market;

import org.bukkit.Material;
import java.util.UUID;

public class SellOffer extends MarketOffer {
    public SellOffer(UUID player, Material material, int amount, int pricePerUnit) {
        super(player, material, amount, pricePerUnit);
    }
    @Override
    public boolean isBuy() { return false; }
}