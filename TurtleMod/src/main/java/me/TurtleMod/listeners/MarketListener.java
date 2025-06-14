package me.TurtleMod.listeners;

import me.TurtleMod.TurtlePlugin;
import me.TurtleMod.market.*;
import me.TurtleMod.commands.MarketCommand;
import me.TurtleMod.commands.MarketCommand.TabType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerSignPromptEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class MarketListener implements Listener {
    private final TurtlePlugin plugin;
    private final MarketManager market;
    private final String MAIN = "Market [";
    private final String DETAIL = "Market: ";
    private final String YOUR_OFFERS = "Your Offers";
    // For sign prompt state
    private final Map<UUID, SignPromptContext> signPromptState = new HashMap<>(); // per player

    public MarketListener(TurtlePlugin plugin, MarketManager market) {
        this.plugin = plugin;
        this.market = market;
    }

    // Helper for tab index
    private TabType tabFromSlot(int slot) {
        TabType[] types = TabType.values();
        return slot >= 45 && slot < 50 ? types[slot - 45] : null;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        InventoryView view = e.getView();
        String title = view.getTitle();

        // MAIN MARKET (with tabs)
        if (title.startsWith(MAIN)) {
            e.setCancelled(true);
            int slot = e.getRawSlot();
            if (slot < 0 || slot >= view.getTopInventory().getSize()) return;
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) return;

            // Tabs (slot 45-49)
            if (slot >= 45 && slot <= 49) {
                TabType tab = tabFromSlot(slot);
                if (tab != null) {
                    MarketCommand.openMarketMenu(player, tab, 0);
                    return;
                }
            }
            // Pagination
            if (slot == 51 && clicked.getType() == Material.ARROW) {
                // Previous page
                String[] split = title.split(" ");
                TabType tab = TabType.valueOf(title.split("\\[")[1].split("\\]")[0]);
                int page = Integer.parseInt(split[split.length - 1]) - 1;
                MarketCommand.openMarketMenu(player, tab, page - 1);
                return;
            }
            if (slot == 52 && clicked.getType() == Material.ARROW) {
                // Next page
                String[] split = title.split(" ");
                TabType tab = TabType.valueOf(title.split("\\[")[1].split("\\]")[0]);
                int page = Integer.parseInt(split[split.length - 1]) - 1;
                MarketCommand.openMarketMenu(player, tab, page + 1);
                return;
            }
            // "Your Offers" book
            if (slot == 53 && clicked.getType() == Material.BOOK) {
                openYourOffersMenu(player);
                return;
            }
            // Open item detail
            if (clicked.getType() != Material.BOOK && clicked.getType() != Material.ARROW) {
                openItemDetailMenu(player, clicked.getType());
            }
            return;
        }

        // ITEM DETAIL MENU
        if (title.startsWith(DETAIL)) {
            e.setCancelled(true);
            int slot = e.getRawSlot();
            if (slot < 0 || slot >= view.getTopInventory().getSize()) return;
            Material mat = Material.valueOf(title.replace(DETAIL, ""));
            if (slot == 22) return; // The item itself
            // Left
            if (slot == 20) { // Buy Instantly
                requestAmount(player, mat, true, false);
                return;
            } else if (slot == 21) { // Sell Instantly
                requestAmount(player, mat, false, false);
                return;
            }
            // Right
            else if (slot == 23) { // Create Buy Offer
                requestAmount(player, mat, true, true);
                return;
            } else if (slot == 24) { // Create Sell Offer
                requestAmount(player, mat, false, true);
                return;
            }
            // Back
            else if (slot == 40) {
                MarketCommand.openMarketMenu(player, TabType.BLOCKS, 0);
                return;
            }
        }

        // YOUR OFFERS
        if (title.equals(YOUR_OFFERS)) {
            e.setCancelled(true);
            int slot = e.getRawSlot();
            if (slot < 0 || slot >= view.getTopInventory().getSize()) return;
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;
            ItemMeta meta = clicked.getItemMeta();
            if (meta.hasDisplayName() && meta.getDisplayName().equals("§cBack")) {
                MarketCommand.openMarketMenu(player, TabType.BLOCKS, 0);
                return;
            }
        }
    }

    // --- Sign prompt logic below ---

    // Helper context for sign input state
    private static class SignPromptContext {
        Material mat;
        boolean isBuy;
        boolean isOffer;
        int amount; // set after first input
        SignPromptContext(Material mat, boolean isBuy, boolean isOffer) {
            this.mat = mat; this.isBuy = isBuy; this.isOffer = isOffer;
        }
    }

    // Initiate amount input via sign
    private void requestAmount(Player player, Material mat, boolean buy, boolean isOffer) {
        signPromptState.put(player.getUniqueId(), new SignPromptContext(mat, buy, isOffer));
        player.closeInventory();
        player.sendSignPrompt(new String[]{"Enter amount", "", "", ""});
    }

    @EventHandler
    public void onSignPrompt(PlayerSignPromptEvent event) {
        Player player = event.getPlayer();
        SignPromptContext ctx = signPromptState.get(player.getUniqueId());
        if (ctx == null) return;
        String amountLine = event.getLines()[1].replaceAll("[^0-9]", "");
        if (amountLine.isEmpty() || Integer.parseInt(amountLine) <= 0) {
            player.sendMessage("§cInvalid amount.");
            signPromptState.remove(player.getUniqueId());
            return;
        }
        ctx.amount = Integer.parseInt(amountLine);
        if (ctx.isOffer) {
            // Ask for price via a second sign input
            player.sendSignPrompt(new String[]{"Price each?", "", "", ""});
        } else {
            // Handle instant buy/sell (use dummy price)
            handleMarketAction(player, ctx.mat, ctx.amount, -1, ctx.isBuy, false);
            signPromptState.remove(player.getUniqueId());
        }
    }

    // Second prompt: price input for offers
    @EventHandler
    public void onSignPromptPrice(PlayerSignPromptEvent event) {
        Player player = event.getPlayer();
        SignPromptContext ctx = signPromptState.get(player.getUniqueId());
        if (ctx == null || ctx.amount <= 0) return;
        if (!event.getLines()[0].equalsIgnoreCase("Price each?")) return;
        String priceLine = event.getLines()[1].replaceAll("[^0-9]", "");
        if (priceLine.isEmpty() || Integer.parseInt(priceLine) <= 0) {
            player.sendMessage("§cInvalid price.");
            signPromptState.remove(player.getUniqueId());
            return;
        }
        int price = Integer.parseInt(priceLine);
        handleMarketAction(player, ctx.mat, ctx.amount, price, ctx.isBuy, true);
        signPromptState.remove(player.getUniqueId());
    }

    // --- Market logic: fulfillment and offers ---

    private void handleMarketAction(Player player, Material mat, int amount, int price, boolean isBuy, boolean isOffer) {
        // Implement core market logic here: fulfillment, balance/item change, offer creation.
        // This is a stub; you’d add the logic to check inventory, balance, offers, fulfill, etc.
        player.sendMessage("§a[MARKET] " + (isOffer ? (isBuy ? "Created buy offer" : "Created sell offer") : (isBuy ? "Buy instantly" : "Sell instantly")) +
                " for " + amount + "x " + mat.name().toLowerCase() + (isOffer ? (" at " + price + " coins each.") : "."));
    }

    // --- GUI helpers below (unchanged from above) ---

    private void openItemDetailMenu(Player player, Material mat) {
        Inventory inv = Bukkit.createInventory(null, 45, DETAIL + mat.name());
        ItemStack center = new ItemStack(mat);
        ItemMeta cMeta = center.getItemMeta();
        cMeta.setDisplayName("§e" + mat.name().replace('_', ' ').toLowerCase());
        center.setItemMeta(cMeta);
        inv.setItem(22, center);

        // Left: Buy Instantly (20), Sell Instantly (21)
        ItemStack buy = new ItemStack(Material.GOLDEN_HORSE_ARMOR);
        ItemMeta buyMeta = buy.getItemMeta();
        buyMeta.setDisplayName("§aBuy Instantly");
        buy.setItemMeta(buyMeta);
        inv.setItem(20, buy);

        ItemStack sell = new ItemStack(Material.HOPPER);
        ItemMeta sellMeta = sell.getItemMeta();
        sellMeta.setDisplayName("§aSell Instantly");
        sell.setItemMeta(sellMeta);
        inv.setItem(21, sell);

        // Right: Create Buy Offer (23), Create Sell Offer (24)
        ItemStack cbuy = new ItemStack(Material.PAPER);
        ItemMeta cbuyMeta = cbuy.getItemMeta();
        cbuyMeta.setDisplayName("§eCreate Buy Offer");
        cbuy.setItemMeta(cbuyMeta);
        inv.setItem(23, cbuy);

        ItemStack csell = new ItemStack(Material.PAPER);
        ItemMeta csellMeta = csell.getItemMeta();
        csellMeta.setDisplayName("§eCreate Sell Offer");
        csell.setItemMeta(csellMeta);
        inv.setItem(24, csell);

        // Back
        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§cBack");
        back.setItemMeta(backMeta);
        inv.setItem(40, back);

        player.openInventory(inv);
    }

    private void openYourOffersMenu(Player player) {
        List<MarketOffer> offers = market.getPlayerOffers(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(null, 54, YOUR_OFFERS);
        int i = 0;
        for (MarketOffer offer : offers) {
            if (i >= 45) break;
            ItemStack is = new ItemStack(offer.material);
            ItemMeta meta = is.getItemMeta();
            meta.setDisplayName(offer.isBuy() ? "§eBuy Offer" : "§bSell Offer");
            List<String> lore = new ArrayList<>();
            lore.add("[" + (offer.isBuy() ? "BUY" : "SELL") + "] " + offer.material.name());
            lore.add("Amount: " + offer.amount);
            lore.add("Price per: " + offer.pricePerUnit);
            meta.setLore(lore);
            is.setItemMeta(meta);
            inv.setItem(i++, is);
        }
        // Back
        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§cBack");
        back.setItemMeta(backMeta);
        inv.setItem(49, back);
        player.openInventory(inv);
    }
}