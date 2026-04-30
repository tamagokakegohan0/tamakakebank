package tamakake.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tamakake.Tamakakebank;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BankCommand implements CommandExecutor {

    private final Tamakakebank plugin;

    public BankCommand(Tamakakebank plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("プレイヤーのみ実行できます");
            return true;
        }

        // ================= 残高取得 =================
        long balance = plugin.getMysql().getBalance(player.getUniqueId());
        long debt = plugin.getMysql().getDebt(player.getUniqueId());
        Date due = plugin.getMysql().getLoanDue(player.getUniqueId());

        // ================= 表示 =================
        player.sendMessage("§e======残高======");
        player.sendMessage("§7player: §f" + player.getName());
        player.sendMessage("§7残高: §a" + format(balance));

        // 借金がある場合のみ表示
        if (debt > 0) {
            player.sendMessage("§c借金: §c" + format(debt));

            if (due != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                player.sendMessage("§7期限: §f" + sdf.format(due));
            }
        }

        player.sendMessage("§e==============");

        return true;
    }

    // ================= カンマ付きフォーマット =================
    private String format(long amount) {
        return String.format("%,d円", amount);
    }
}