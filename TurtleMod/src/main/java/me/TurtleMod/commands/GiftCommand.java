package me.TurtleMod.commands;

import me.TurtleMod.MoneyManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiftCommand implements CommandExecutor {
    private final MoneyManager moneyManager;

    public GiftCommand(MoneyManager moneyManager) {
        this.moneyManager = moneyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage("§cUsage: /gift <player> <amount>");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage("§cThat player is not online!");
            return true;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[1].replace(",", ""));
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid amount.");
            return true;
        }

        moneyManager.addMoney(target.getUniqueId(), amount);
        player.sendMessage("§aYou gifted §e" + target.getName() + " " + moneyManager.formatMoney(amount) + "§a turtle coins.");
        target.sendMessage("§aYou received §e" + moneyManager.formatMoney(amount) + "§a turtle coins as a gift from §e" + player.getName() + "§a.");
        return true;
    }
}