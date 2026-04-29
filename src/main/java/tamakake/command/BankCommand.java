package tamakake.command;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import tamakake.Tamakakebank;

public class BankCommand implements CommandExecutor {

    private final Tamakakebank plugin;

    public BankCommand(Tamakakebank plugin) {
        this.plugin = plugin;
    }

    private Economy eco() {
        return Bukkit.getServicesManager()
                .getRegistration(Economy.class)
                .getProvider();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) return true;

        double bal = eco().getBalance(player);
        long debt = plugin.getMysql().getDebt(player.getUniqueId());

        player.sendMessage("§e====== 残高 ======");
        player.sendMessage("§7player: §f" + player.getName());
        player.sendMessage("§7残高: §f" + (long) bal + "円");
        player.sendMessage("§7借金: §c" + debt + "円");
        player.sendMessage("§e==================");

        return true;
    }
}