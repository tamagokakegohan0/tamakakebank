package tamakake.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import net.milkbowl.vault.economy.Economy;
import tamakake.Tamakakebank;

import java.util.UUID;

public class BankOpCommand implements CommandExecutor {

    private final Tamakakebank plugin;

    public BankOpCommand(Tamakakebank plugin) {
        this.plugin = plugin;
    }

    // Vault取得（方法A）
    private Economy eco() {
        return Bukkit.getServicesManager()
                .getRegistration(Economy.class)
                .getProvider();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("tamakakebank.admin")) {
            sender.sendMessage("§c権限がありません");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§c使い方: /bankop setbank <player> <金額>");
            return true;
        }

        if (!args[0].equalsIgnoreCase("setbank")) {
            sender.sendMessage("§c使い方: /bankop setbank <player> <金額>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        UUID uuid = target.getUniqueId();

        long amount;

        try {
            amount = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c数字を入力してください");
            return true;
        }

        if (amount < 0) {
            sender.sendMessage("§cマイナスは使えません");
            return true;
        }

        // =========================
        // 💥 Vaultを直接操作（これが正解）
        // =========================

        double current = eco().getBalance(target);

        if (current > amount) {
            eco().withdrawPlayer(target, current - amount);
        } else {
            eco().depositPlayer(target, amount - current);
        }

        sender.sendMessage("§a" + target.getName() + " の所持金を §e" + amount + "円 §aに設定しました");

        return true;
    }
}