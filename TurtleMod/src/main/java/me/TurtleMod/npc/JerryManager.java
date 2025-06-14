package me.TurtleMod.npc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import me.TurtleMod.TurtlePlugin;

import java.util.Objects;
import java.util.UUID;

public class JerryManager implements Listener {
    private final TurtlePlugin plugin;
    private UUID jerryUUID = null;

    public JerryManager(TurtlePlugin plugin) {
        this.plugin = plugin;
    }

    public void spawnJerry() {
        World world = Bukkit.getWorlds().get(0);
        Location loc = new Location(world, 0.5, 10, 0.5); // Centered on block
        // Remove existing Jerry(s)
        world.getEntitiesByClass(Villager.class).stream()
                .filter(v -> "§aJerry".equals(v.getCustomName()))
                .forEach(Villager::remove);
        // Spawn
        Villager villager = (Villager) world.spawnEntity(loc, EntityType.VILLAGER);
        villager.setCustomName("§aJerry");
        villager.setCustomNameVisible(true);
        villager.setAI(false);
        villager.setCollidable(false);
        villager.setInvulnerable(true);
        villager.setSilent(true);
        villager.setGravity(false);
        villager.setPersistent(true);
        villager.setProfession(Villager.Profession.NITWIT);
        villager.setRemoveWhenFarAway(false);
        jerryUUID = villager.getUniqueId();

        // Make Jerry look at the nearest player every tick (no distance limit)
        new BukkitRunnable() {
            @Override
            public void run() {
                Villager v = getJerry();
                if (v == null || v.isDead()) {
                    this.cancel();
                    return;
                }
                Player nearest = null;
                double bestDist = Double.MAX_VALUE;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.getWorld().equals(world)) continue;
                    double dist = p.getLocation().distanceSquared(v.getLocation());
                    if (dist < bestDist) {
                        bestDist = dist;
                        nearest = p;
                    }
                }
                if (nearest != null) {
                    Location villagerLoc = v.getLocation();
                    Location playerLoc = nearest.getLocation().clone().add(0, nearest.getEyeHeight(), 0);

                    double dx = playerLoc.getX() - villagerLoc.getX();
                    double dy = playerLoc.getY() - (villagerLoc.getY() + 1.5);
                    double dz = playerLoc.getZ() - villagerLoc.getZ();
                    double distanceXZ = Math.sqrt(dx * dx + dz * dz);

                    float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                    float pitch = (float) Math.toDegrees(-Math.atan2(dy, distanceXZ));

                    // Teleport Jerry to the same spot but update yaw/pitch to look at player
                    v.teleport(new Location(world, villagerLoc.getX(), villagerLoc.getY(), villagerLoc.getZ(), yaw, pitch));
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private Villager getJerry() {
        if (jerryUUID == null) return null;
        World world = Bukkit.getWorlds().get(0);
        return (Villager) world.getEntity(jerryUUID);
    }

    @EventHandler
    public void onJerryDamaged(EntityDamageEvent event) {
        if (event.getEntity() instanceof Villager && Objects.equals(event.getEntity().getCustomName(), "§aJerry")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onJerryInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Villager && Objects.equals(event.getRightClicked().getCustomName(), "§aJerry")) {
            openJerryGui(event.getPlayer());
            event.setCancelled(true);
        }
    }

    private void openJerryGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, "Jerry");
        ItemStack help = new ItemStack(Material.PAPER);
        ItemMeta meta = help.getItemMeta();
        meta.setDisplayName("§aHelp");
        help.setItemMeta(meta);
        gui.setItem(4, help);
        player.openInventory(gui);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getServer().getPluginManager().registerEvents(new org.bukkit.event.Listener() {
                @EventHandler
                public void onClick(org.bukkit.event.inventory.InventoryClickEvent e) {
                    if (e.getWhoClicked().equals(player) && e.getView().getTitle().equals("Jerry") && e.getSlot() == 4) {
                        player.closeInventory();
                        player.sendMessage("§bWelcome to Turtleland!\n§bFollow the tutorial pathway to get started on your adventure!");
                        e.setCancelled(true);
                    }
                    // Unregister after use
                    if (e.getWhoClicked().equals(player) && e.getView().getTitle().equals("Jerry")) {
                        org.bukkit.event.HandlerList.unregisterAll(this);
                    }
                }
            }, plugin);
        }, 1L);
    }
}