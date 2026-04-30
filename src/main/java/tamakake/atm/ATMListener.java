package tamakake.atm;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import tamakake.Tamakakebank;

public class ATMListener implements Listener {

    private final Tamakakebank plugin;

    public ATMListener(Tamakakebank plugin) {
        this.plugin = plugin;
    }

    // ================= GUIクリック =================
    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (!(e.getWhoClicked() instanceof Player player)) return;

        String title = e.getView().getTitle();

        // ================= 入金GUI =================
        if (title.equals("入金")) {

            // 下のプレイヤーインベントリはOK
            if (e.getClickedInventory() == null) return;

            if (e.getClickedInventory().equals(e.getView().getTopInventory())) {
                e.setCancelled(true);
            }

            return;
        }

        // ================= ATMメイン =================
        if (title.equals("ATM")) {

            e.setCancelled(true); // ★全部操作禁止

            if (e.getCurrentItem() == null) return;

            Material type = e.getCurrentItem().getType();

            // 入金ボタン
            if (type == Material.EMERALD_BLOCK) {
                openDepositGUI(player);
            }

            // 出金ボタン
            if (type == Material.REDSTONE_BLOCK) {
                withdraw(player, 1000); // デフォ1000円
            }
        }
    }

    // ================= GUI閉じた時（入金処理） =================
    @EventHandler
    public void onClose(InventoryCloseEvent e) {

        if (!(e.getPlayer() instanceof Player player)) return;

        String title = e.getView().getTitle();

        if (!title.equals("入金")) return;

        Inventory inv = e.getInventory();

        long total = 0;

        for (ItemStack item : inv.getContents()) {
            total += MoneyItem.getValue(item);
        }

        if (total <= 0) return;

        // ★Vaultに入金
        plugin.getEconomy().depositPlayer(player, total);

        player.sendMessage("§a" + format(total) + " 入金しました");
    }

    // ================= 入金GUI =================
    private void openDepositGUI(Player player) {

        Inventory inv = Bukkit.createInventory(null, 27, "入金");

        // ガラスで枠（任意）
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, glass);
        }

        // 中央だけ空ける（ここに紙幣入れる）
        inv.setItem(13, null);

        player.openInventory(inv);
    }

    // ================= 出金 =================
    private void withdraw(Player player, long amount) {

        double balance = plugin.getEconomy().getBalance(player);

        if (balance < amount) {
            player.sendMessage("§cお金が足りません");
            return;
        }

        plugin.getEconomy().withdrawPlayer(player, amount);

        // 紙幣を渡す
        player.getInventory().addItem(MoneyItem.create(amount));

        player.sendMessage("§a" + format(amount) + " 出金しました");
    }

    // ================= コンマ =================
    private String format(long amount) {
        return String.format("%,d円", amount);
    }
}