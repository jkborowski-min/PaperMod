package me.TurtleMod.commands;

import me.TurtleMod.market.MarketManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class MarketCommand implements CommandExecutor {
    // --- Tabs definition ---
    public enum TabType {
        BLOCKS, ORES, LOGS, FARM, MISC
    }
    // Map tab to items
    public static final Map<TabType, Set<Material>> TAB_ITEMS = new EnumMap<>(TabType.class);
    static {
        TAB_ITEMS.put(TabType.BLOCKS, EnumSet.of(Material.DIRT, Material.GRASS_BLOCK, Material.SAND, Material.GRAVEL, Material.STONE, Material.DEEPSLATE));
        TAB_ITEMS.put(TabType.ORES, EnumSet.of(Material.IRON_ORE, Material.COAL_ORE, Material.DIAMOND, Material.COPPER_ORE, Material.DEEPSLATE_IRON_ORE));
        TAB_ITEMS.put(TabType.LOGS, EnumSet.of(Material.OAK_LOG, Material.BIRCH_LOG, Material.SPRUCE_LOG));
        TAB_ITEMS.put(TabType.FARM, EnumSet.of(Material.WHEAT, Material.CARROT, Material.POTATO, Material.BAMBOO, Material.SUGAR_CANE));
        TAB_ITEMS.put(TabType.MISC, EnumSet.of(Material.SLIME_BALL, Material.GUNPOWDER, Material.BONE, Material.ROTTEN_FLESH));
    }

    private final MarketManager manager;

    public MarketCommand(MarketManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only!");
            return true;
        }
        openMarketMenu(player, TabType.BLOCKS, 0);
        return true;
    }

    public static void openMarketMenu(Player player, TabType tab, int page) {
        int itemsPerPage = 45;
        List<Material> items = new ArrayList<>(TAB_ITEMS.get(tab));
        items.sort(Comparator.comparing(Enum::name));
        int maxPage = (items.size() - 1) / itemsPerPage;
        int from = page * itemsPerPage;
        int to = Math.min(from + itemsPerPage, items.size());
        List<Material> pageItems = items.subList(from, to);

        Inventory inv = Bukkit.createInventory(null, 54, "Market [" + tab.name() + "] - Page " + (page+1));

        // Items
        for (int i = 0; i < pageItems.size(); i++) {
            Material mat = pageItems.get(i);
            ItemStack is = new ItemStack(mat);
            ItemMeta meta = is.getItemMeta();
            meta.setDisplayName("§e" + mat.name().replace('_', ' ').toLowerCase());
            is.setItemMeta(meta);
            inv.setItem(i, is);
        }

        // Tabs (bottom row 0–4)
        int idx = 0;
        for (TabType t : TabType.values()) {
            ItemStack tabIcon = new ItemStack(idx == 0 ? Material.DIRT : idx == 1 ? Material.IRON_ORE : idx == 2 ? Material.OAK_LOG : idx == 3 ? Material.WHEAT : Material.SLIME_BALL);
            ItemMeta tm = tabIcon.getItemMeta();
            tm.setDisplayName(tab == t ? "§a" + t.name() : "§7" + t.name());
            tabIcon.setItemMeta(tm);
            inv.setItem(45 + idx, tabIcon);
            idx++;
        }

        // Book for "Your Offers"
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta bookMeta = book.getItemMeta();
        bookMeta.setDisplayName("§bYour Offers");
        book.setItemMeta(bookMeta);
        inv.setItem(53, book);

        // Pagination
        if (page > 0) {
            ItemStack back = new ItemStack(Material.ARROW);
            ItemMeta bMeta = back.getItemMeta();
            bMeta.setDisplayName("§aPrevious Page");
            back.setItemMeta(bMeta);
            inv.setItem(51, back);
        }
        if (page < maxPage) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nMeta = next.getItemMeta();
            nMeta.setDisplayName("§aNext Page");
            next.setItemMeta(nMeta);
            inv.setItem(52, next);
        }

        player.openInventory(inv);
    }
}