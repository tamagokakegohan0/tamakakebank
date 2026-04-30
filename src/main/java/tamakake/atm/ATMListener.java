package tamakake.atm;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import tamakake.Tamakakebank;

public class ATMListener implements Listener {

    private final Tamakakebank plugin;

    public ATMListener(Tamakakebank plugin) {
        this.plugin = plugin;
    }

    // ===== 紙幣右クリック =====
    @EventHandler
    public void onUse(PlayerInteractEvent e) {

        if (e.getItem() == null) return;
        if (!e.getAction().toString().contains("RIGHT_CLICK")) return;

        long value = MoneyItem.getValue(e.getItem());

        if (value > 0) {
            e.setCancelled(true);
            e.getPlayer().performCommand("atm");
        }
    }

    // ===== GUIクリック =====
    @EventHandler
    public void onClick(InventoryClickEvent e) {

        Player player = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();

        // ================= ATM =================
        if (title.equals("ATM")) {

            e.setCancelled(true);
            if (e.getCurrentItem() == null) return;

            if (e.getCurrentItem().getType() == Material.CHEST) {
                openDepositGUI(player);
                return;
            }

            if (e.getCurrentItem().getType() == Material.FURNACE) {
                openWithdrawGUI(player);
                return;
            }
        }

        // ================= 出金 =================
        if (title.equals("出金")) {

            e.setCancelled(true);
            if (e.getCurrentItem() == null) return;

            long value = MoneyItem.getValue(e.getCurrentItem());
            if (value <= 0) return;

            double bal = plugin.getEconomy().getBalance(player);

            if (bal < value) {
                player.sendMessage("§c残高不足");
                return;
            }

            plugin.getEconomy().withdrawPlayer(player, value);
            player.getInventory().addItem(MoneyItem.create(value));

            player.sendMessage("§a" + String.format("%,d円 出金しました", value));
        }

        // ================= 入金（クリックでは何もしない） =================
        if (title.equals("入金")) {
            // ここは削除ではなく無効（閉じる処理に移動）
        }
    }

    // ===== 入金確定（★ここが本体）=====
    @EventHandler
    public void onClose(InventoryCloseEvent e) {

        if (!e.getView().getTitle().equals("入金")) return;

        Player player = (Player) e.getPlayer();

        int total = 0;

        for (ItemStack item : e.getInventory().getContents()) {

            if (item == null) continue;

            long value = MoneyItem.getValue(item);

            if (value > 0) {
                total += value;
            }
        }

        if (total > 0) {
            plugin.getMysql().addBalance(player.getUniqueId(), total);
            player.sendMessage("§a" + String.format("%,d円 入金しました", total));
        }

        e.getInventory().clear();
    }

    // ===== 入金GUI =====
    private void openDepositGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "入金");
        player.openInventory(inv);
    }

    // ===== 出金GUI =====
    private void openWithdrawGUI(Player player) {

        Inventory inv = Bukkit.createInventory(null, 27, "出金");

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);

        for (int i = 0; i < 27; i++) {
            inv.setItem(i, glass);
        }

        inv.setItem(10, MoneyItem.create(10));
        inv.setItem(11, MoneyItem.create(100));
        inv.setItem(12, MoneyItem.create(1000));

        inv.setItem(14, MoneyItem.create(10000));
        inv.setItem(15, MoneyItem.create(100000));
        inv.setItem(16, MoneyItem.create(1000000));

        player.openInventory(inv);
    }
}