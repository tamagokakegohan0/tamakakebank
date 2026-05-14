package tamakake.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import tamakake.Tamakakebank;

import java.util.*;

public class BalTopCommand implements CommandExecutor {

    private final Tamakakebank plugin;

    public BalTopCommand(Tamakakebank plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Set<UUID> users = new HashSet<>(plugin.getMysql().getAllUsers());

        // ★ 同じ名前を統合
        Map<String, Long> balances = new HashMap<>();

        for (UUID uuid : users) {

            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

            String name = player.getName();

            if (name == null || name.isEmpty()) continue;

            long balance = plugin.getMysql().getBalance(uuid);

            // ★ 同名なら高い方を採用
            if (balances.containsKey(name)) {

                if (balance > balances.get(name)) {
                    balances.put(name, balance);
                }

            } else {
                balances.put(name, balance);
            }
        }

        List<Map.Entry<String, Long>> sorted =
                new ArrayList<>(balances.entrySet());

        sorted.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

        sender.sendMessage("§e===== Baltop =====");

        int rank = 1;

        for (Map.Entry<String, Long> entry : sorted) {

            sender.sendMessage(
                    "§6#" + rank +
                            " §f" + entry.getKey() +
                            " §7- §a" +
                            String.format("%,d円", entry.getValue())
            );

            rank++;

            if (rank > 10) break;
        }

        sender.sendMessage("§e==================");

        return true;
    }
}