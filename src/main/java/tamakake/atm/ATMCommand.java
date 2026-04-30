package tamakake.atm;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tamakake.Tamakakebank;

public class ATMCommand implements CommandExecutor {

    private final Tamakakebank plugin;

    public ATMCommand(Tamakakebank plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) return true;

        Inventory inv = Bukkit.createInventory(null, 27, "ATM");

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gm = glass.getItemMeta();
        gm.setDisplayName(" ");
        glass.setItemMeta(gm);

        for (int i = 0; i < 27; i++) {
            inv.setItem(i, glass);
        }

        // 入金（チェスト）
        ItemStack deposit = new ItemStack(Material.CHEST);
        ItemMeta dm = deposit.getItemMeta();
        dm.setDisplayName("§a入金");
        deposit.setItemMeta(dm);

        // 出金（かまど）
        ItemStack withdraw = new ItemStack(Material.FURNACE);
        ItemMeta wm = withdraw.getItemMeta();
        wm.setDisplayName("§e出金");
        withdraw.setItemMeta(wm);

        // 配置
        inv.setItem(10, deposit);
        inv.setItem(11, deposit);
        inv.setItem(12, deposit);

        inv.setItem(14, withdraw);
        inv.setItem(15, withdraw);
        inv.setItem(16, withdraw);

        player.openInventory(inv);
        return true;
    }
}