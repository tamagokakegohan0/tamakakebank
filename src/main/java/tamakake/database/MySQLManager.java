package tamakake.database;

import org.bukkit.Bukkit;
import tamakake.Tamakakebank;

import java.sql.*;
import java.util.*;

public class MySQLManager {

    private final Tamakakebank plugin;
    private Connection connection;

    public MySQLManager(Tamakakebank plugin) {
        this.plugin = plugin;
    }

    // ================= 接続 =================
    public void connect() {
        try {
            String host = plugin.getConfig().getString("mysql.host");
            int port = plugin.getConfig().getInt("mysql.port");
            String db = plugin.getConfig().getString("mysql.database");
            String user = plugin.getConfig().getString("mysql.user");
            String pass = plugin.getConfig().getString("mysql.password");

            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&autoReconnect=true",
                    user,
                    pass
            );

            plugin.getLogger().info("MySQL接続成功");

        } catch (Exception e) {
            plugin.getLogger().warning("MySQL接続失敗");
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    // ================= テーブル =================
    public void createTables() {
        try (Statement st = connection.createStatement()) {

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS bank_data (
                    uuid VARCHAR(36) PRIMARY KEY,
                    name VARCHAR(16),
                    balance BIGINT DEFAULT 0,
                    debt BIGINT DEFAULT 0,
                    loan_level INT DEFAULT 0,
                    loan_due DATE
                )
            """);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= プレイヤー登録 =================
    public void registerPlayer(UUID uuid, String name) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT IGNORE INTO bank_data (uuid, name) VALUES (?, ?)"
        )) {
            ps.setString(1, uuid.toString());
            ps.setString(2, name);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // ★ 名前更新
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE bank_data SET name=? WHERE uuid=?"
        )) {
            ps.setString(1, name);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= 残高 =================
    public long getBalance(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT balance FROM bank_data WHERE uuid=?"
        )) {
            ps.setString(1, uuid.toString());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getLong("balance");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void setBalance(UUID uuid, long amount) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE bank_data SET balance=? WHERE uuid=?"
        )) {

            ps.setLong(1, amount);
            ps.setString(2, uuid.toString());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addBalance(UUID uuid, long amount) {
        setBalance(uuid, getBalance(uuid) + amount);
    }

    public void removeBalance(UUID uuid, long amount) {
        setBalance(uuid, Math.max(0, getBalance(uuid) - amount));
    }

    // ================= 借金 =================
    public long getDebt(UUID uuid) {

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT debt FROM bank_data WHERE uuid=?"
        )) {

            ps.setString(1, uuid.toString());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getLong("debt");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void setDebt(UUID uuid, long amount) {

        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE bank_data SET debt=? WHERE uuid=?"
        )) {

            ps.setLong(1, amount);
            ps.setString(2, uuid.toString());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addDebt(UUID uuid, long amount) {

        long newDebt = getDebt(uuid) + amount;

        setDebt(uuid, newDebt);

        // ★ 5日期限
        if (getLoanDue(uuid) == null) {
            setLoanDue(uuid,
                    new java.util.Date(System.currentTimeMillis() + 5L * 86400000));
        }
    }

    public void removeDebt(UUID uuid, long amount) {

        long newDebt = Math.max(0, getDebt(uuid) - amount);

        setDebt(uuid, newDebt);

        if (newDebt == 0) {
            clearLoanDue(uuid);
        }
    }

    // ================= レベル =================
    public int getLoanLevel(UUID uuid) {

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT loan_level FROM bank_data WHERE uuid=?"
        )) {

            ps.setString(1, uuid.toString());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("loan_level");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void setLoanLevel(UUID uuid, int level) {

        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE bank_data SET loan_level=? WHERE uuid=?"
        )) {

            ps.setInt(1, level);
            ps.setString(2, uuid.toString());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= 期限 =================
    public java.util.Date getLoanDue(UUID uuid) {

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT loan_due FROM bank_data WHERE uuid=?"
        )) {

            ps.setString(1, uuid.toString());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDate("loan_due");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setLoanDue(UUID uuid, java.util.Date date) {

        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE bank_data SET loan_due=? WHERE uuid=?"
        )) {

            ps.setDate(1, new java.sql.Date(date.getTime()));
            ps.setString(2, uuid.toString());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearLoanDue(UUID uuid) {

        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE bank_data SET loan_due=NULL WHERE uuid=?"
        )) {

            ps.setString(1, uuid.toString());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= 全ユーザー =================
    public List<UUID> getAllUsers() {

        List<UUID> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT DISTINCT uuid FROM bank_data WHERE uuid IS NOT NULL"
        )) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                try {

                    UUID uuid = UUID.fromString(rs.getString("uuid"));

                    // ★ 名前取得できる人だけ
                    if (Bukkit.getOfflinePlayer(uuid).getName() != null) {
                        list.add(uuid);
                    }

                } catch (Exception ignored) {
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}