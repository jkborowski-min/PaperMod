package me.TurtleMod.commands;

import me.TurtleMod.MoneyManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {
    private final MoneyManager moneyManager;

    public BalanceCommand(MoneyManager moneyManager) {
        this.moneyManager = moneyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        int bal = moneyManager.getMoney(player.getUniqueId());
        player.sendMessage("§aYou have §e" + moneyManager.formatMoney(bal) + "§a turtle coins.");
        return true;
    }
}