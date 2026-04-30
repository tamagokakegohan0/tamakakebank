package tamakake.atm;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import tamakake.Tamakakebank;

public class MoneyItem {

    public static ItemStack create(long value) {

        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        // ★ コンマ付き＋円だけ
        meta.setDisplayName("§e" + format(value));

        meta.getPersistentDataContainer().set(
                new NamespacedKey(Tamakakebank.getInstance(), "money"),
                PersistentDataType.LONG,
                value
        );

        item.setItemMeta(meta);
        return item;
    }

    public static long getValue(ItemStack item) {

        if (item == null || !item.hasItemMeta()) return 0;

        ItemMeta meta = item.getItemMeta();

        Long value = meta.getPersistentDataContainer().get(
                new NamespacedKey(Tamakakebank.getInstance(), "money"),
                PersistentDataType.LONG
        );

        return value == null ? 0 : value;
    }

    // ★ コンマ処理
    private static String format(long amount) {
        return String.format("%,d円", amount);
    }
}