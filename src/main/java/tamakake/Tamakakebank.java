package tamakake;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import tamakake.atm.ATMCommand;
import tamakake.atm.ATMListener;
import tamakake.atm.ATMItemListener;

import tamakake.command.*;
import tamakake.database.MySQLManager;
import tamakake.economy.VaultEconomy;
import tamakake.listener.PlayerJoinListener;
import tamakake.debt.LoanCommand;
import tamakake.debt.LoanInterestTask;

public class Tamakakebank extends JavaPlugin {

    private static Tamakakebank instance;
    private MySQLManager mysql;
    private Economy economy;

    @Override
    public void onEnable() {

        instance = this;

        saveDefaultConfig();

        // ================= MySQL =================
        mysql = new MySQLManager(this);
        mysql.connect();

        if (mysql.getConnection() == null) {
            getLogger().severe("MySQL接続失敗");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        mysql.createTables();

        // ================= Vault =================
        VaultEconomy vault = new VaultEconomy(this);

        getServer().getServicesManager().register(
                Economy.class,
                vault,
                this,
                ServicePriority.Normal
        );

        this.economy = vault;

        // ================= Commands =================
        registerCommands();

        // ================= Listeners =================
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ATMListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ATMItemListener(this), this);

        // ================= Loan =================
        new LoanInterestTask(this)
                .runTaskTimer(this, 20L * 60L * 60L, 20L * 60L * 60L);

        getLogger().info("TamakakeBank 起動完了");
    }

    private void registerCommands() {

        BankCommand bank = new BankCommand(this);

        getCommand("bank").setExecutor(bank);
        getCommand("bal").setExecutor(bank);
        getCommand("balance").setExecutor(bank);
        getCommand("money").setExecutor(bank);

        getCommand("atm").setExecutor(new ATMCommand(this));
        getCommand("pay").setExecutor(new PayCommand(this));
        getCommand("bankop").setExecutor(new BankOpCommand(this));
        getCommand("loan").setExecutor(new LoanCommand(this));
        getCommand("baltop").setExecutor(new BalTopCommand(this));
    }

    @Override
    public void onDisable() {
        if (mysql != null && mysql.getConnection() != null) {
            try {
                mysql.getConnection().close();
            } catch (Exception ignored) {}
        }
    }

    public static Tamakakebank getInstance() {
        return instance;
    }

    public MySQLManager getMysql() {
        return mysql;
    }

    public Economy getEconomy() {
        return economy;
    }
}