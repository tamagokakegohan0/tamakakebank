package tamakake.debt;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tamakake.Tamakakebank;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LoanCommand implements CommandExecutor {

    private final Tamakakebank plugin;

    public LoanCommand(Tamakakebank plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("プレイヤー専用コマンドです");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {

            // ================= 借りる =================
            case "borrow" -> {

                if (args.length != 2) {
                    player.sendMessage("§c/loan borrow <金額>");
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
                    player.sendMessage("§c1円以上にしてください");
                    return true;
                }

                long max = getMaxLoan(player);
                long debt = plugin.getMysql().getDebt(player.getUniqueId());

                if (debt + amount > max) {
                    player.sendMessage("§c借入上限: " + format(max));
                    return true;
                }

                // ★借金追加
                plugin.getMysql().addDebt(player.getUniqueId(), amount);

                // ★Vaultにお金追加（ここ重要）
                plugin.getEconomy().depositPlayer(player, amount);

                player.sendMessage("§a" + format(amount) + " 借入しました");
            }

            // ================= 返済 =================
            case "pay" -> {

                if (args.length != 2) {
                    player.sendMessage("§c/loan pay <金額>");
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
                    player.sendMessage("§c1円以上にしてください");
                    return true;
                }

                long debt = plugin.getMysql().getDebt(player.getUniqueId());
                double balance = plugin.getEconomy().getBalance(player);

                if (debt <= 0) {
                    player.sendMessage("§a借金はありません");
                    return true;
                }

                if (balance < amount) {
                    player.sendMessage("§cお金が足りません");
                    return true;
                }

                if (amount > debt) {
                    amount = debt;
                }

                // ★Vaultから引く
                plugin.getEconomy().withdrawPlayer(player, amount);

                // ★借金減らす
                plugin.getMysql().removeDebt(player.getUniqueId(), amount);

                player.sendMessage("§a" + format(amount) + " 返済しました");
            }

            // ================= 確認 =================
            case "check" -> {

                long max = getMaxLoan(player);
                long debt = plugin.getMysql().getDebt(player.getUniqueId());
                Date due = plugin.getMysql().getLoanDue(player.getUniqueId());

                player.sendMessage("§e==== 借金情報 ====");

                player.sendMessage("§7現在借金: " + format(debt));
                player.sendMessage("§7最大借入: " + format(max));

                if (due != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                    player.sendMessage("§7期限: " + sdf.format(due));
                }

                player.sendMessage("§e================");
            }

            default -> sendHelp(player);
        }

        return true;
    }

    // ================= ヘルプ =================
    private void sendHelp(Player p) {
        p.sendMessage("§e==== Loanコマンド ====");
        p.sendMessage("§7/loan borrow <金額>");
        p.sendMessage("§7/loan pay <金額>");
        p.sendMessage("§7/loan check");
    }

    // ================= 最大借入 =================
    private long getMaxLoan(Player p) {

        int level = plugin.getMysql().getLoanLevel(p.getUniqueId());

        return switch (level) {
            case 0 -> 10_000;
            case 1 -> 100_000;
            case 2 -> 500_000;
            default -> 1_000_000;
        };
    }

    // ================= カンマ表示 =================
    private String format(long amount) {
        return String.format("%,d円", amount);
    }
}