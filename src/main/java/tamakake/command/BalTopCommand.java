package tamakake.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import tamakake.Tamakakebank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BalTopCommand implements CommandExecutor {

    private final Tamakakebank plugin;

    public BalTopCommand(Tamakakebank plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        sender.sendMessage("§7==========");
        sender.sendMessage("§e   baltop");
        sender.sendMessage("§7==========");

        try (Connection conn = plugin.getMysql().getConnection()) {

            String sql = "SELECT name, balance FROM players ORDER BY balance DESC LIMIT 10";

            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            int rank = 1;

            while (rs.next()) {

                String name = rs.getString("name");
                long balance = rs.getLong("balance");

                sender.sendMessage("§e" + rank + "位: §f" + name + " §7- §a" + balance);

                rank++;
            }

        } catch (Exception e) {
            sender.sendMessage("§cランキング取得失敗");
            e.printStackTrace();
        }

        sender.sendMessage("§7==========");

        return true;
    }
}