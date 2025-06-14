package me.TurtleMod.market;

import org.bukkit.Material;
import java.util.UUID;

public class BuyOffer extends MarketOffer {
    public BuyOffer(UUID player, Material material, int amount, int pricePerUnit) {
        super(player, material, amount, pricePerUnit);
    }
    @Override
    public boolean isBuy() { return true; }
}