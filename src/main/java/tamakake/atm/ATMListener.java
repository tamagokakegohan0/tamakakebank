package tamakake.atm;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import tamakake.Tamakakebank;

public class ATMListener implements Listener {

    private final Tamakakebank plugin;

    public ATMListener(Tamakakebank plugin) {
        this.plugin = plugin;
    }

    // ================= Vault =================
    private Economy eco() {
        return Bukkit.getServicesManager()
                .getRegistration(Economy.class)
                .getProvider();
    }

    // ================= 紙幣右クリックでATM =================
    @EventHandler
    public void onUse(PlayerInteractEvent e) {

        if (e.getItem() == null) return;

        if (MoneyItem.getValue(e.getItem()) > 0) {
            e.setCancelled(true);
            e.getPlayer().performCommand("atm");
        }
    }

    // ================= GUIクリック =================
    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (!(e.getWhoClicked() instanceof Player player)) return;

        String title = e.getView().getTitle();

        // ================= ATMメニュー =================
        if (title.equals("ATM")) {
            e.setCancelled(true);

            if (e.getCurrentItem() == null) return;

            if (e.getCurrentItem().getType() == Material.CHEST) {
                openDeposit(player);
            }

            if (e.getCurrentItem().getType() == Material.FURNACE) {
                openWithdraw(player);
            }
        }

        // ================= 出金 =================
        if (title.equals("出金")) {
            e.setCancelled(true);

            if (e.getCurrentItem() == null) return;

            long amount = MoneyItem.getValue(e.getCurrentItem());
            if (amount <= 0) return;

            double bal = eco().getBalance(player);

            if (bal < amount) {
                player.sendMessage("§c残高不足");
                return;
            }

            eco().withdrawPlayer(player, amount);
            player.getInventory().addItem(MoneyItem.create(amount));

            player.sendMessage("§a" + amount + "円出金しました");
        }

        // ================= 入金 =================
        if (title.equals("入金")) {
            e.setCancelled(false);
        }
    }

    // ================= 入金確定 =================
    @EventHandler
    public void onClose(InventoryCloseEvent e) {

        if (!e.getView().getTitle().equals("入金")) return;

        Player p = (Player) e.getPlayer();

        long total = 0;

        for (ItemStack item : e.getInventory().getContents()) {
            total += MoneyItem.getValue(item);
        }

        if (total > 0) {
            eco().depositPlayer(p, total);
            p.sendMessage("§a" + total + "円入金しました");
        }
    }

    // ================= GUI =================
    private void openDeposit(Player p) {
        p.openInventory(Bukkit.createInventory(null, 54, "入金"));
    }

    private void openWithdraw(Player p) {

        Inventory inv = Bukkit.createInventory(null, 27, "出金");

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gm = glass.getItemMeta();
        gm.setDisplayName(" ");
        glass.setItemMeta(gm);

        // 外枠＋中央ガラス（完全対称）
        int[] glassSlots = {
                0,1,2,3,4,5,6,7,8,
                9,11,13,15,17,
                18,19,20,21,22,23,24,25,26
        };

        for (int i : glassSlots) {
            inv.setItem(i, glass);
        }

        long[] money = {10, 100, 1000, 10000, 100000, 1000000};
        int[] slots = {10, 11, 12, 14, 15, 16};

        for (int i = 0; i < slots.length; i++) {
            inv.setItem(slots[i], MoneyItem.create(money[i]));
        }

        p.openInventory(inv);
    }
}