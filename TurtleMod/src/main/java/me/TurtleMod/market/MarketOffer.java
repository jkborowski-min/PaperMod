package me.TurtleMod.market;

import org.bukkit.Material;
import java.util.UUID;

public abstract class MarketOffer {
    public final UUID player;
    public final Material material;
    public int amount;
    public int pricePerUnit;

    public MarketOffer(UUID player, Material material, int amount, int pricePerUnit) {
        this.player = player;
        this.material = material;
        this.amount = amount;
        this.pricePerUnit = pricePerUnit;
    }

    public abstract boolean isBuy();
}