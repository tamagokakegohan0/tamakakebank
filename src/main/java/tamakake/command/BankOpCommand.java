package tamakake.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import tamakake.Tamakakebank;

import java.util.UUID;

public class BankOpCommand implements CommandExecutor {

    private final Tamakakebank plugin;

    public BankOpCommand(Tamakakebank plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // ================= 権限チェック（最重要） =================
        if (!sender.hasPermission("tamakakebank.admin")) {
            sender.sendMessage("§c権限がありません");
            return true;
        }

        // ================= 引数チェック =================
        if (args.length < 3) {
            sender.sendMessage("§c使い方: /bankop setbank <player> <金額>");
            return true;
        }

        if (!args[0].equalsIgnoreCase("setbank")) {
            sender.sendMessage("§c使い方: /bankop setbank <player> <金額>");
            return true;
        }

        // ================= プレイヤー取得 =================
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        UUID uuid = target.getUniqueId();

        // ================= 金額チェック =================
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

        // ================= MySQL更新 =================
        plugin.getMysql().setBalance(uuid, amount);

        // ================= メッセージ =================
        String name = target.getName() != null ? target.getName() : "Unknown";

        sender.sendMessage("§a" + name + " の残高を §e" + amount + "円 §aに設定しました");

        return true;
    }
}