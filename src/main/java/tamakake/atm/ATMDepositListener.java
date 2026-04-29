package tamakake.atm;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import tamakake.Tamakakebank;

public class ATMDepositListener implements Listener {

    private final Tamakakebank plugin;

    public ATMDepositListener(Tamakakebank plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {

        if (!e.getView().getTitle().equals("入金")) return;

        Player player = (Player) e.getPlayer();

        long total = 0;

        for (ItemStack item : e.getInventory().getContents()) {
            long value = MoneyItem.getValue(item);
            if (value > 0) {
                total += value;
            }
        }

        if (total > 0) {
            plugin.getMysql().addBalance(player.getUniqueId(), total);
            player.sendMessage("§a" + total + "円 入金しました");
        }
    }
}