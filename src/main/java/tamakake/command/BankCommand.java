package tamakake.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tamakake.Tamakakebank;

public class BankCommand implements CommandExecutor {

    private final Tamakakebank plugin;

    public BankCommand(Tamakakebank plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("プレイヤー専用コマンドです");
            return true;
        }

        var mysql = plugin.getMysql();

        long balance = mysql.getBalance(player.getUniqueId());
        long debt = mysql.getDebt(player.getUniqueId());
        String due = mysql.getLoanDue(player.getUniqueId());

        if (due == null) due = "なし";

        long payAmount = debt > 0 ? Math.max(1, debt / 10) : 0;

        // ================= 表示 =================
        player.sendMessage("§6====== 残高 ======");
        player.sendMessage("§7player: " + player.getName());
        player.sendMessage("§7残高: §a" + balance + "円");

        player.sendMessage("§6====== 借金 ======");

        if (debt <= 0) {
            player.sendMessage("§7借金: なし");
        } else {
            player.sendMessage("§7借金: §c" + debt + "円");
            player.sendMessage("§7期限: " + due);
            player.sendMessage("§7支払い目安: " + payAmount + "円");
        }

        player.sendMessage("§6================");

        return true;
    }
}