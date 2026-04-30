package tamakake.database;

import tamakake.Tamakakebank;

import java.sql.*;
import java.util.*;

public class MySQLManager {

    private final Tamakakebank plugin;
    private Connection connection;

    public MySQLManager(Tamakakebank plugin) {
        this.plugin = plugin;
    }

    // ================= CONNECT =================
    public void connect() {
        try {
            String host = plugin.getConfig().getString("database.host");
            int port = plugin.getConfig().getInt("database.port");
            String db = plugin.getConfig().getString("database.database");
            String user = plugin.getConfig().getString("database.user");
            String pass = plugin.getConfig().getString("database.password");

            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false",
                    user,
                    pass
            );

            plugin.getLogger().info("MySQL接続成功");

        } catch (Exception e) {
            plugin.getLogger().severe("MySQL接続失敗");
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    // ================= TABLE =================
    public void createTables() {
        try (Statement st = connection.createStatement()) {

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS bank_data (
                    uuid VARCHAR(36) PRIMARY KEY,
                    balance BIGINT DEFAULT 0,
                    debt BIGINT DEFAULT 0,
                    loan_level INT DEFAULT 0,
                    loan_success INT DEFAULT 0,
                    loan_due DATE DEFAULT NULL
                )
            """);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= REGISTER =================
    public void registerPlayer(UUID uuid, String name) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT IGNORE INTO bank_data (uuid, balance, debt) VALUES (?,0,0)")) {

            ps.setString(1, uuid.toString());
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= BALANCE =================
    public long getBalance(UUID uuid) {
        return getLong(uuid, "balance");
    }

    public void addBalance(UUID uuid, long amount) {
        setLong(uuid, "balance", getBalance(uuid) + amount);
    }

    public void removeBalance(UUID uuid, long amount) {
        setLong(uuid, "balance", Math.max(0, getBalance(uuid) - amount));
    }

    public void setBalance(UUID uuid, long amount) {
        setLong(uuid, "balance", amount);
    }

    // ================= DEBT =================
    public long getDebt(UUID uuid) {
        return getLong(uuid, "debt");
    }

    public void addDebt(UUID uuid, long amount) {
        setLong(uuid, "debt", getDebt(uuid) + amount);
    }

    public void removeDebt(UUID uuid, long amount) {
        setLong(uuid, "debt", Math.max(0, getDebt(uuid) - amount));
    }

    // ================= LOAN =================
    public int getLoanLevel(UUID uuid) {
        return (int) getLong(uuid, "loan_level");
    }

    public void setLoanLevel(UUID uuid, int value) {
        setLong(uuid, "loan_level", value);
    }

    public int getLoanSuccess(UUID uuid) {
        return (int) getLong(uuid, "loan_success");
    }

    public void setLoanSuccess(UUID uuid, int value) {
        setLong(uuid, "loan_success", value);
    }

    // ================= LOAN DUE =================
    public String getLoanDue(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT loan_due FROM bank_data WHERE uuid=?")) {

            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                java.sql.Date date = rs.getDate("loan_due");
                return date != null ? date.toString() : null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setLoanDue(UUID uuid, String date) {
        try (PreparedStatement ps = connection.prepareStatement("""
            UPDATE bank_data SET loan_due=? WHERE uuid=?
        """)) {

            ps.setString(1, date);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= CORE =================
    private long getLong(UUID uuid, String column) {

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT " + column + " FROM bank_data WHERE uuid=?")) {

            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getLong(column);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void setLong(UUID uuid, String column, long value) {

        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE bank_data SET " + column + "=? WHERE uuid=?")) {

            ps.setLong(1, value);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= USERS =================
    public Set<UUID> getAllUsers() {

        Set<UUID> set = new HashSet<>();

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT uuid FROM bank_data")) {

            while (rs.next()) {
                set.add(UUID.fromString(rs.getString("uuid")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return set;
    }
}