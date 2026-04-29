package tamakake.atm;

import org.bukkit.entity.Player;
import tamakake.Tamakakebank;

public class ATMManager {

    private final Tamakakebank plugin;

    public ATMManager(Tamakakebank plugin) {
        this.plugin = plugin;
    }

    // 出金
    public void withdraw(Player player, long amount) {

        long balance = plugin.getMysql().getBalance(player.getUniqueId());

        if (balance < amount) {
            player.sendMessage("§cお金が足りません");
            return;
        }

        plugin.getMysql().removeBalance(player.getUniqueId(), amount);
        player.getInventory().addItem(MoneyItem.create(amount));

        player.sendMessage("§a" + amount + "円 引き出しました");
    }

    // 入金
    public void deposit(Player player) {

        long total = 0;

        for (var item : player.getInventory().getContents()) {
            long value = MoneyItem.getValue(item);
            if (value > 0) {
                total += value;
                item.setAmount(0);
            }
        }

        if (total == 0) {
            player.sendMessage("§c紙幣がありません");
            return;
        }

        plugin.getMysql().addBalance(player.getUniqueId(), total);
        player.sendMessage("§a" + total + "円 入金しました");
    }
}