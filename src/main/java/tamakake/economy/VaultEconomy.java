package tamakake.economy;

import net.milkbowl.vault.economy.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import tamakake.Tamakakebank;

import java.util.*;

public class VaultEconomy implements Economy {

    private final Tamakakebank plugin;

    public VaultEconomy(Tamakakebank plugin) {
        this.plugin = plugin;
    }

    // ===== 基本 =====
    public boolean isEnabled() { return true; }
    public String getName() { return "TamakakeBank"; }
    public boolean hasBankSupport() { return false; }
    public int fractionalDigits() { return 0; }
    public String currencyNamePlural() { return "円"; }
    public String currencyNameSingular() { return "円"; }
    public String format(double amount) { return String.format("%,d円", (long) amount); }

    private UUID uuid(String name) {
        return Bukkit.getOfflinePlayer(name).getUniqueId();
    }

    // ===== DBラッパー（ここが超重要）=====
    private long get(UUID uuid) {
        plugin.getMysql().registerPlayer(uuid, "player");
        return plugin.getMysql().getBalance(uuid);
    }

    private void add(UUID uuid, long amount) {
        plugin.getMysql().registerPlayer(uuid, "player");
        plugin.getMysql().addBalance(uuid, amount);
    }

    private void remove(UUID uuid, long amount) {
        plugin.getMysql().registerPlayer(uuid, "player");
        plugin.getMysql().removeBalance(uuid, amount);
    }

    // ===== アカウント =====
    public boolean hasAccount(OfflinePlayer p) { return true; }
    public boolean hasAccount(OfflinePlayer p, String w) { return true; }
    public boolean hasAccount(String p) { return true; }
    public boolean hasAccount(String p, String w) { return true; }

    public boolean createPlayerAccount(OfflinePlayer p) { return true; }
    public boolean createPlayerAccount(OfflinePlayer p, String w) { return true; }
    public boolean createPlayerAccount(String p) { return true; }
    public boolean createPlayerAccount(String p, String w) { return true; }

    // ===== 残高 =====
    public double getBalance(OfflinePlayer p) { return get(p.getUniqueId()); }
    public double getBalance(OfflinePlayer p, String w) { return getBalance(p); }
    public double getBalance(String p) { return get(uuid(p)); }
    public double getBalance(String p, String w) { return getBalance(p); }

    public boolean has(OfflinePlayer p, double a) { return getBalance(p) >= a; }
    public boolean has(OfflinePlayer p, String w, double a) { return has(p, a); }
    public boolean has(String p, double a) { return getBalance(p) >= a; }
    public boolean has(String p, String w, double a) { return has(p, a); }

    // ===== 入金 =====
    public EconomyResponse depositPlayer(OfflinePlayer p, double a) {
        add(p.getUniqueId(), (long) a);
        return new EconomyResponse(a, getBalance(p), EconomyResponse.ResponseType.SUCCESS, null);
    }
    public EconomyResponse depositPlayer(OfflinePlayer p, String w, double a) { return depositPlayer(p, a); }
    public EconomyResponse depositPlayer(String p, double a) {
        add(uuid(p), (long) a);
        return new EconomyResponse(a, getBalance(p), EconomyResponse.ResponseType.SUCCESS, null);
    }
    public EconomyResponse depositPlayer(String p, String w, double a) { return depositPlayer(p, a); }

    // ===== 出金 =====
    public EconomyResponse withdrawPlayer(OfflinePlayer p, double a) {
        double bal = getBalance(p);
        if (bal < a) return new EconomyResponse(0, bal, EconomyResponse.ResponseType.FAILURE, "残高不足");
        remove(p.getUniqueId(), (long) a);
        return new EconomyResponse(a, getBalance(p), EconomyResponse.ResponseType.SUCCESS, null);
    }
    public EconomyResponse withdrawPlayer(OfflinePlayer p, String w, double a) { return withdrawPlayer(p, a); }
    public EconomyResponse withdrawPlayer(String p, double a) {
        double bal = getBalance(p);
        if (bal < a) return new EconomyResponse(0, bal, EconomyResponse.ResponseType.FAILURE, "残高不足");
        remove(uuid(p), (long) a);
        return new EconomyResponse(a, getBalance(p), EconomyResponse.ResponseType.SUCCESS, null);
    }
    public EconomyResponse withdrawPlayer(String p, String w, double a) { return withdrawPlayer(p, a); }

    // ===== 銀行（未対応）=====
    public EconomyResponse createBank(String name, String player) { return fail(); }
    public EconomyResponse createBank(String name, OfflinePlayer player) { return fail(); }

    public EconomyResponse deleteBank(String name) { return fail(); }
    public EconomyResponse bankBalance(String name) { return fail(); }
    public EconomyResponse bankHas(String name, double amount) { return fail(); }
    public EconomyResponse bankWithdraw(String name, double amount) { return fail(); }
    public EconomyResponse bankDeposit(String name, double amount) { return fail(); }

    public EconomyResponse isBankOwner(String name, String player) { return fail(); }
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) { return fail(); }

    public EconomyResponse isBankMember(String name, String player) { return fail(); }
    public EconomyResponse isBankMember(String name, OfflinePlayer player) { return fail(); }

    public List<String> getBanks() { return Collections.emptyList(); }

    private EconomyResponse fail() {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "未対応");
    }
}