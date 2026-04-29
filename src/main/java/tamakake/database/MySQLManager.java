package tamakake.database;

import tamakake.Tamakakebank;

import java.sql.*;
import java.util.UUID;

public class MySQLManager {

    private final Tamakakebank plugin;
    private Connection connection;

    public MySQLManager(Tamakakebank plugin) {
        this.plugin = plugin;
    }

    // ================= 接続 =================
    public void connect() {

        try {
            String host = plugin.getConfig().getString("database.host");
            int port = plugin.getConfig().getInt("database.port");
            String db = plugin.getConfig().getString("database.database");
            String user = plugin.getConfig().getString("database.user");
            String pass = plugin.getConfig().getString("database.password");
            boolean useSSL = plugin.getConfig().getBoolean("database.useSSL");

            String url = "jdbc:mysql://" + host + ":" + port + "/" + db +
                    "?useSSL=" + useSSL +
                    "&allowPublicKeyRetrieval=true&serverTimezone=Asia/Tokyo";

            connection = DriverManager.getConnection(url, user, pass);

            plugin.getLogger().info("MySQL接続成功");

        } catch (Exception e) {
            plugin.getLogger().severe("MySQL接続失敗");
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    // ================= テーブル =================
    public void createTables() {

        try {
            String sql = """
                CREATE TABLE IF NOT EXISTS players (
                    uuid VARCHAR(36) PRIMARY KEY,
                    name VARCHAR(16),
                    balance BIGINT DEFAULT 0,
                    debt BIGINT DEFAULT 0,
                    loan_level INT DEFAULT 0,
                    loan_success INT DEFAULT 0,
                    last_interest BIGINT DEFAULT 0
                )
            """;

            connection.createStatement().execute(sql);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= プレイヤー登録 =================
    public void registerPlayer(UUID uuid, String name) {

        try {
            String sql = """
                INSERT INTO players (uuid, name, balance, debt, loan_level, loan_success, last_interest)
                VALUES (?, ?, 0, 0, 0, 0, 0)
                ON DUPLICATE KEY UPDATE name = VALUES(name)
            """;

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, uuid.toString());
            ps.setString(2, name);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= balance =================
    public long getBalance(UUID uuid) {
        return getLong(uuid, "balance");
    }

    public void addBalance(UUID uuid, long amount) {
        update("UPDATE players SET balance = balance + ? WHERE uuid=?", amount, uuid);
    }

    public void removeBalance(UUID uuid, long amount) {
        update("UPDATE players SET balance = balance - ? WHERE uuid=?", amount, uuid);
    }

    public void setBalance(UUID uuid, long amount) {
        update("UPDATE players SET balance=? WHERE uuid=?", amount, uuid);
    }

    // ================= debt =================
    public long getDebt(UUID uuid) {
        return getLong(uuid, "debt");
    }

    public void addDebt(UUID uuid, long amount) {
        update("UPDATE players SET debt = debt + ? WHERE uuid=?", amount, uuid);
    }

    public void removeDebt(UUID uuid, long amount) {
        update("UPDATE players SET debt = debt - ? WHERE uuid=?", amount, uuid);
    }

    // ================= loan system =================
    public int getLoanLevel(UUID uuid) {
        return (int) getLong(uuid, "loan_level");
    }

    public void setLoanLevel(UUID uuid, int level) {
        update("UPDATE players SET loan_level=? WHERE uuid=?", level, uuid);
    }

    public int getLoanSuccess(UUID uuid) {
        return (int) getLong(uuid, "loan_success");
    }

    public void setLoanSuccess(UUID uuid, int success) {
        update("UPDATE players SET loan_success=? WHERE uuid=?", success, uuid);
    }

    public void addLoanSuccess(UUID uuid) {
        update("UPDATE players SET loan_success = loan_success + 1 WHERE uuid=?", uuid);
    }

    public long getLastInterest(UUID uuid) {
        return getLong(uuid, "last_interest");
    }

    public void setLastInterest(UUID uuid, long time) {
        update("UPDATE players SET last_interest=? WHERE uuid=?", time, uuid);
    }

    // ================= utility =================
    private long getLong(UUID uuid, String column) {

        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT " + column + " FROM players WHERE uuid=?"
            );

            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getLong(column);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    private void update(String sql, Object value, UUID uuid) {

        try {
            PreparedStatement ps = connection.prepareStatement(sql);

            if (value instanceof Long l) ps.setLong(1, l);
            else if (value instanceof Integer i) ps.setInt(1, i);

            ps.setString(2, uuid.toString());
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void update(String sql, UUID uuid) {

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, uuid.toString());
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= close =================
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}