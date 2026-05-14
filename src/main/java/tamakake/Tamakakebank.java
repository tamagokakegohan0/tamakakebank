package tamakake;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import tamakake.atm.ATMCommand;
import tamakake.atm.ATMListener;

import tamakake.command.BankCommand;
import tamakake.command.BankOpCommand;
import tamakake.command.PayCommand;
import tamakake.command.BalTopCommand;

import tamakake.database.MySQLManager;
import tamakake.debt.LoanCommand;
import tamakake.debt.LoanInterestTask;
import tamakake.economy.VaultEconomy;

public class Tamakakebank extends JavaPlugin implements Listener {

    private static Tamakakebank instance;

    private MySQLManager mysql;
    private Economy economy;

    public static Tamakakebank getInstance() {
        return instance;
    }

    public MySQLManager getMysql() {
        return mysql;
    }

    public Economy getEconomy() {
        return economy;
    }

    @Override
    public void onEnable() {

        instance = this;

        saveDefaultConfig();

        // ================= MySQL =================
        mysql = new MySQLManager(this);
        mysql.connect();
        mysql.createTables();

        // ================= Vaultチェック =================
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Vaultが見つかりません！");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // ================= Economy登録 =================
        VaultEconomy vaultEconomy = new VaultEconomy(this);

        getServer().getServicesManager().register(
                Economy.class,
                vaultEconomy,
                this,
                ServicePriority.Highest
        );

        RegisteredServiceProvider<Economy> rsp =
                getServer().getServicesManager().getRegistration(Economy.class);

        economy = (rsp != null) ? rsp.getProvider() : null;

        if (economy == null) {
            getLogger().severe("Economy登録失敗");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Vault Economy 登録成功");

        // ================= コマンド =================
        register("atm", new ATMCommand(this));
        register("loan", new LoanCommand(this));

        register("bank", new BankCommand(this));
        register("bal", new BankCommand(this));
        register("balance", new BankCommand(this));
        register("money", new BankCommand(this));

        register("bankop", new BankOpCommand(this));
        register("pay", new PayCommand(this));
        register("baltop", new BalTopCommand(this));

        // ================= リスナー =================
        Bukkit.getPluginManager().registerEvents(new ATMListener(this), this);
        Bukkit.getPluginManager().registerEvents(this, this);

        // ================= 利息 =================
        new LoanInterestTask(this)
                .runTaskTimer(this, 20L, 20L * 60 * 5);

        getLogger().info("TamakakeBank 起動完了");
    }

    // ================= コマンド登録 =================
    private void register(String name, Object executor) {

        if (getCommand(name) == null) {
            getLogger().warning("plugin.ymlに未定義: " + name);
            return;
        }

        getCommand(name).setExecutor(
                (org.bukkit.command.CommandExecutor) executor
        );
    }

    // ================= 初回登録 =================
    @org.bukkit.event.EventHandler
    public void onJoin(PlayerJoinEvent e) {

        mysql.registerPlayer(
                e.getPlayer().getUniqueId(),
                e.getPlayer().getName()
        );
    }

    @Override
    public void onDisable() {
        getLogger().info("TamakakeBank 停止");
    }
}