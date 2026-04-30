package tamakake.atm;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tamakake.Tamakakebank;

public class ATMListener implements Listener {

    private final Tamakakebank plugin;

    public ATMListener(Tamakakebank plugin) {
        this.plugin = plugin;
    }

    // ================= クリック処理 =================
    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (!(e.getWhoClicked() instanceof Player player)) return;

        String title = e.getView().getTitle();

        if (!isATM(title)) return;

        ItemStack item = e.getCurrentItem();

        // ================= ATMメニュー =================
        if (title.equals("ATM")) {

            e.setCancelled(true);

            if (item == null) return;

            Material type = item.getType();

            if (type == Material.CHEST) {
                openDepositMenu(player);
            }

            if (type == Material.FURNACE) {
                openWithdrawMenu(player);
            }

            return;
        }

        // ================= 出金 =================
        if (title.equals("出金")) {

            e.setCancelled(true);

            if (item == null) return;

            long amount = MoneyItem.getValue(item);
            if (amount <= 0) return;

            long balance = plugin.getMysql().getBalance(player.getUniqueId());

            if (balance < amount) {
                player.sendMessage("§cお金が足りません");
                return;
            }

            plugin.getMysql().removeBalance(player.getUniqueId(), amount);
            player.getInventory().addItem(MoneyItem.create(amount));

            player.sendMessage("§a" + amount + "円 出金しました");

            return;
        }

        // ================= 入金 =================
        if (title.equals("入金")) {

            // ★重要：ここはキャンセルしない（入金できるようにする）
            e.setCancelled(false);
        }
    }

    // ================= ドラッグ防止 =================
    @EventHandler
    public void onDrag(InventoryDragEvent e) {

        if (!isATM(e.getView().getTitle())) return;

        e.setCancelled(true);
    }

    // ================= 入金確定 =================
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

    // ================= GUI =================
    private void openDepositMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "入金");
        player.openInventory(inv);
    }

    private void openWithdrawMenu(Player player) {

        Inventory inv = Bukkit.createInventory(null, 27, "出金");

        int[] amounts = {10, 100, 1000, 10000, 100000, 1000000};
        int[] slots = {10, 11, 12, 14, 15, 16};

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);

        for (int i = 0; i < 27; i++) {
            inv.setItem(i, glass);
        }

        for (int i = 0; i < amounts.length; i++) {
            inv.setItem(slots[i], MoneyItem.create(amounts[i]));
        }

        player.openInventory(inv);
    }

    // ================= 判定 =================
    private boolean isATM(String title) {
        return title.equals("ATM")
                || title.equals("出金")
                || title.equals("入金");
    }
}