package tamakake.economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import tamakake.Tamakakebank;

import java.util.List;

public class VaultEconomy implements Economy {

    private final Tamakakebank plugin;

    public VaultEconomy(Tamakakebank plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    @Override
    public String getName() {
        return "TamakakeBank";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 0;
    }

    @Override
    public String format(double amount) {
        return (long) amount + "円";
    }

    @Override
    public String currencyNamePlural() {
        return "円";
    }

    @Override
    public String currencyNameSingular() {
        return "円";
    }

    // =========================
    // アカウント
    // =========================
    @Override
    public boolean hasAccount(String playerName) { return true; }

    @Override
    public boolean hasAccount(OfflinePlayer player) { return true; }

    @Override
    public boolean hasAccount(String playerName, String worldName) { return true; }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) { return true; }

    @Override
    public boolean createPlayerAccount(String playerName) { return true; }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) { return true; }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) { return true; }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) { return true; }

    // =========================
    // 残高
    // =========================
    @Override
    public double getBalance(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return plugin.getMysql().getBalance(player.getUniqueId());
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return plugin.getMysql().getBalance(player.getUniqueId());
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(String playerName, double amount) {
        return getBalance(playerName) >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public boolean has(String playerName, String world, double amount) {
        return has(playerName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String world, double amount) {
        return has(player, amount);
    }

    // =========================
    // 入金
    // =========================
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        plugin.getMysql().addBalance(player.getUniqueId(), (long) amount);
        return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return depositPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String world, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String world, double amount) {
        return depositPlayer(playerName, amount);
    }

    // =========================
    // 出金
    // =========================
    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {

        if (!has(player, amount)) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "お金不足");
        }

        plugin.getMysql().removeBalance(player.getUniqueId(), (long) amount);
        return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return withdrawPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String world, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String world, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    // =========================
    // 銀行（未使用）
    // =========================
    @Override public EconomyResponse createBank(String name, String player) { return notImpl(); }
    @Override public EconomyResponse createBank(String name, OfflinePlayer player) { return notImpl(); }
    @Override public EconomyResponse deleteBank(String name) { return notImpl(); }
    @Override public EconomyResponse bankBalance(String name) { return notImpl(); }
    @Override public EconomyResponse bankHas(String name, double amount) { return notImpl(); }
    @Override public EconomyResponse bankWithdraw(String name, double amount) { return notImpl(); }
    @Override public EconomyResponse bankDeposit(String name, double amount) { return notImpl(); }
    @Override public EconomyResponse isBankOwner(String name, String playerName) { return notImpl(); }
    @Override public EconomyResponse isBankOwner(String name, OfflinePlayer player) { return notImpl(); }
    @Override public EconomyResponse isBankMember(String name, String playerName) { return notImpl(); }
    @Override public EconomyResponse isBankMember(String name, OfflinePlayer player) { return notImpl(); }
    @Override public List<String> getBanks() { return List.of(); }

    private EconomyResponse notImpl() {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "未対応");
    }
}