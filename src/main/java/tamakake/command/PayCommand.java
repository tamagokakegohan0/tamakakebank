package tamakake.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tamakake.Tamakakebank;

import java.util.UUID;

public class PayCommand implements CommandExecutor {

    private final Tamakakebank plugin;

    public PayCommand(Tamakakebank plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("プレイヤー専用コマンドです");
            return true;
        }

        if (args.length != 2) {
            player.sendMessage("§c使い方: /pay <player> <金額>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        if (target.getUniqueId() == null) {
            player.sendMessage("§cプレイヤーが見つかりません");
            return true;
        }

        long amount;

        try {
            amount = Long.parseLong(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("§c金額が正しくありません");
            return true;
        }

        if (amount <= 0) {
            player.sendMessage("§c金額は1以上にしてください");
            return true;
        }

        UUID senderUUID = player.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        long balance = plugin.getMysql().getBalance(senderUUID);

        if (balance < amount) {
            player.sendMessage("§cお金が足りません");
            return true;
        }

        // ================= 送金処理 =================
        plugin.getMysql().removeBalance(senderUUID, amount);
        plugin.getMysql().addBalance(targetUUID, amount);

        player.sendMessage("§a" + target.getName() + " に " + amount + "円 送金しました");

        Player online = Bukkit.getPlayer(targetUUID);
        if (online != null) {
            online.sendMessage("§a" + player.getName() + " から " + amount + "円 受け取りました");
        }

        return true;
    }
}