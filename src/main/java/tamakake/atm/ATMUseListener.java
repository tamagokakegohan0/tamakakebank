package tamakake.atm;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;

public class ATMUseListener implements Listener {

    @EventHandler
    public void onUse(PlayerInteractEvent e) {

        if (!e.getAction().toString().contains("RIGHT_CLICK")) return;

        ItemStack item = e.getItem();
        if (item == null) return;

        long value = MoneyItem.getValue(item);
        if (value <= 0) return;

        Player player = e.getPlayer();

        // ATM開く
        Bukkit.dispatchCommand(player, "atm");
    }
}