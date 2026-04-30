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

        // ================= Config =================
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

        // ================= コマンド登録 =================
        registerCommands();

        // ================= Listener登録 =================
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ATMListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ATMItemListener(this), this);

        // ================= 借金利息タスク =================
        // 1時間ごとに実行（調整可能）
        new LoanInterestTask(this)
                .runTaskTimer(this, 20L * 60L * 60L, 20L * 60L * 60L);

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

        BankCommand bank = new BankCommand(this);

        // 残高系
        getCommand("bank").setExecutor(bank);
        getCommand("bal").setExecutor(bank);
        getCommand("balance").setExecutor(bank);
        getCommand("money").setExecutor(bank);

        // ATM
        getCommand("atm").setExecutor(new ATMCommand(this));

        // 支払い
        getCommand("pay").setExecutor(new PayCommand(this));

        // 管理者
        getCommand("bankop").setExecutor(new BankOpCommand(this));

        // 借金
        getCommand("loan").setExecutor(new LoanCommand(this));

        // ランキング（ある場合）
        if (getCommand("baltop") != null) {
            getCommand("baltop").setExecutor(new BalTopCommand(this));
        }
    }

    // ================= Getter =================
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