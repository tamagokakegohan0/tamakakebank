package tamakake.debt;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import tamakake.Tamakakebank;

import java.time.LocalDate;

public class LoanCommand implements CommandExecutor {

    private final Tamakakebank plugin;

    public LoanCommand(Tamakakebank plugin) {
        this.plugin = plugin;
    }

    // ================= Vault取得（おすすめ方式） =================
    private Economy eco() {
        return Bukkit.getServicesManager()
                .getRegistration(Economy.class)
                .getProvider();
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

        String sub = args[0].toLowerCase();

        // ================= 借入 =================
        if (sub.equals("borrow")) {

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

            long max = getMaxLoan(player);
            long debt = plugin.getMysql().getDebt(player.getUniqueId());

            if (debt + amount > max) {
                player.sendMessage("§c借入上限: " + max + "円");
                return true;
            }

            // ✔ 借金増加
            plugin.getMysql().addDebt(player.getUniqueId(), amount);

            // ✔ Vaultに付与
            eco().depositPlayer(player, amount);

            player.sendMessage("§a" + amount + "円借入しました");
            return true;
        }

        // ================= 返済 =================
        if (sub.equals("pay")) {

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

            long debt = plugin.getMysql().getDebt(player.getUniqueId());

            if (debt <= 0) {
                player.sendMessage("§a借金はありません");
                return true;
            }

            if (amount > debt) amount = debt;

            double bal = eco().getBalance(player);

            if (bal < amount) {
                player.sendMessage("§cお金が足りません");
                return true;
            }

            // ✔ Vaultから減らす
            eco().withdrawPlayer(player, amount);

            // ✔ 借金だけ減らす
            plugin.getMysql().removeDebt(player.getUniqueId(), amount);

            player.sendMessage("§a" + amount + "円返済しました");

            return true;
        }

        // ================= 確認 =================
        if (sub.equals("check")) {

            long debt = plugin.getMysql().getDebt(player.getUniqueId());
            int level = plugin.getMysql().getLoanLevel(player.getUniqueId());
            int success = plugin.getMysql().getLoanSuccess(player.getUniqueId());

            LocalDate deadline = LocalDate.now().plusDays(5);

            player.sendMessage("§e====== 借金情報 ======");
            player.sendMessage("§c借金: §f" + debt + "円");
            player.sendMessage("§7ランク: " + level);
            player.sendMessage("§7成功回数: " + success + "/5");
            player.sendMessage("§e期限: " + deadline);
            player.sendMessage("§e==================");

            return true;
        }

        sendHelp(player);
        return true;
    }

    // ================= ヘルプ =================
    private void sendHelp(Player p) {
        p.sendMessage("§e==== Loan ====");
        p.sendMessage("§7/loan borrow <金額>");
        p.sendMessage("§7/loan pay <金額>");
        p.sendMessage("§7/loan check");
    }

    // ================= 借入上限 =================
    private long getMaxLoan(Player p) {

        int level = plugin.getMysql().getLoanLevel(p.getUniqueId());

        return switch (level) {
            case 0 -> 10_000;
            case 1 -> 100_000;
            case 2 -> 500_000;
            default -> 1_000_000;
        };
    }
}