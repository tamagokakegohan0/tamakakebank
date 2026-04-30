package tamakake.atm;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import tamakake.Tamakakebank;

public class ATMItemListener implements Listener {

    private final Tamakakebank plugin;

    public ATMItemListener(Tamakakebank plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {

        Player player = e.getPlayer();
        ItemStack item = e.getItem();

        if (item == null) return;

        // ★ここは紙幣判定（MoneyItemに合わせる）
        if (item.getType() == Material.PAPER &&
                item.hasItemMeta() &&
                item.getItemMeta().getDisplayName().contains("円")) {

            e.setCancelled(true);

            player.performCommand("atm");
        }
    }
}