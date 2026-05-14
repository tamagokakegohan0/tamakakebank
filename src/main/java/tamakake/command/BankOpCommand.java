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

        if (args.length < 2) {
            sender.sendMessage("§c/bankop setbank <player> <金額>");
            sender.sendMessage("§c/bankop give <player> <金額>");
            sender.sendMessage("§c/bankop checkbank <player>");
            return true;
        }

        String sub = args[0];

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        UUID uuid = target.getUniqueId();

        // ================= checkbank =================
        if (sub.equalsIgnoreCase("checkbank")) {

            long balance = plugin.getMysql().getBalance(uuid);
            long debt = plugin.getMysql().getDebt(uuid);

            sender.sendMessage("§e======銀行情報======");
            sender.sendMessage("§7player: §f" + target.getName());
            sender.sendMessage("§7残高: §a" + String.format("%,d円", balance));
            sender.sendMessage("§7借金: §c" + String.format("%,d円", debt));
            sender.sendMessage("§e===================");

            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§c/bankop setbank <player> <金額>");
            sender.sendMessage("§c/bankop give <player> <金額>");
            return true;
        }

        long amount;

        try {
            amount = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c数字を入力してください");
            return true;
        }

        if (amount < 0) {
            sender.sendMessage("§cマイナスは不可");
            return true;
        }

        // ================= setbank =================
        if (sub.equalsIgnoreCase("setbank")) {

            double current = eco().getBalance(target);

            if (current > amount) {
                eco().withdrawPlayer(target, current - amount);
            } else {
                eco().depositPlayer(target, amount - current);
            }

            sender.sendMessage("§a" + target.getName() + " の残高を §e"
                    + String.format("%,d円", amount) + " §aに設定しました");

            return true;
        }

        // ================= give =================
        if (sub.equalsIgnoreCase("give")) {

            eco().depositPlayer(target, amount);

            sender.sendMessage("§a" + target.getName() + " に §e"
                    + String.format("%,d円", amount) + " §a付与しました");

            if (target.isOnline()) {
                target.getPlayer().sendMessage("§a銀行口座に §e"
                        + String.format("%,d円", amount) + " §a振り込まれました");
            }

            return true;
        }

        sender.sendMessage("§c/bankop setbank <player> <金額>");
        sender.sendMessage("§c/bankop give <player> <金額>");
        sender.sendMessage("§c/bankop checkbank <player>");

        return true;
    }
}