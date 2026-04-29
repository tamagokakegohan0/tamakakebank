package tamakake;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import tamakake.atm.ATMCommand;
import tamakake.atm.ATMListener;

import tamakake.command.BankCommand;
import tamakake.command.BankOpCommand;
import tamakake.command.PayCommand;
import tamakake.command.BalTopCommand;
import tamakake.debt.LoanCommand;

import tamakake.database.MySQLManager;
import tamakake.economy.VaultEconomy;
import tamakake.listener.PlayerJoinListener;

public class Tamakakebank extends JavaPlugin {

    private static Tamakakebank instance;
    private MySQLManager mysql;

    @Override
    public void onEnable() {

        instance = this;

        // ================= config =================
        saveDefaultConfig();

        // ================= MySQL =================
        mysql = new MySQLManager(this);
        mysql.connect();

        if (mysql.getConnection() == null) {
            getLogger().severe("MySQL接続失敗 → プラグイン停止");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        mysql.createTables();

        // ================= コマンド登録 =================
        registerCommands();

        // ================= Listener登録 =================
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ATMListener(this), this);

        // ================= Vault登録 =================
        VaultEconomy vault = new VaultEconomy(this);

        getServer().getServicesManager().register(
                Economy.class,
                vault,
                this,
                ServicePriority.Normal
        );

        getLogger().info("TamakakeBank 起動完了");
    }

    @Override
    public void onDisable() {
        if (mysql != null && mysql.getConnection() != null) {
            try {
                mysql.getConnection().close();
            } catch (Exception ignored) {}
        }
    }

    // ================= コマンド登録 =================
    private void registerCommands() {

        // ================= ATM =================
        if (getCommand("atm") != null)
            getCommand("atm").setExecutor(new ATMCommand(this));

        // ================= 銀行メイン =================
        if (getCommand("bank") != null)
            getCommand("bank").setExecutor(new BankCommand(this));

        // ================= 残高コマンド統一 =================
        BankCommand bankCommand = new BankCommand(this);

        if (getCommand("bal") != null)
            getCommand("bal").setExecutor(bankCommand);

        if (getCommand("balance") != null)
            getCommand("balance").setExecutor(bankCommand);

        if (getCommand("money") != null)
            getCommand("money").setExecutor(bankCommand);

        // ================= 管理者 =================
        if (getCommand("bankop") != null)
            getCommand("bankop").setExecutor(new BankOpCommand(this));

        // ================= 送金 =================
        if (getCommand("pay") != null)
            getCommand("pay").setExecutor(new PayCommand(this));

        // ================= ランキング =================
        if (getCommand("baltop") != null)
            getCommand("baltop").setExecutor(new BalTopCommand(this));

        // ================= 借金 =================
        if (getCommand("loan") != null)
            getCommand("loan").setExecutor(new LoanCommand(this));
    }

    // ================= getter =================
    public static Tamakakebank getInstance() {
        return instance;
    }

    public MySQLManager getMysql() {
        return mysql;
    }
}