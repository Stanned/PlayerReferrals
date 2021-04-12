package com.stanexe.playerreferrals.util;

import com.stanexe.playerreferrals.PlayerReferrals;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseUtil {
    private static final ExecutorService dbThread = Executors.newSingleThreadExecutor();
    static final PlayerReferrals plugin = PlayerReferrals.getInstance();
    static final String dbType = plugin.getConfig().getString("database-type");
    private static Connection conn;

    public static String getDbType() {
        return dbType;
    }

    public static Connection getConn() {
        try {
            if (conn != null && conn.isValid(1)) {
                return conn;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (dbType == null) {
            plugin.getLogger().info("Invalid database type. Expected SQLITE or MYSQL, received nothing.");
            Bukkit.getPluginManager().disablePlugin(PlayerReferrals.getInstance());
        } else {
            if (dbType.equalsIgnoreCase("SQLITE")) {
                try {
                    conn = new SQLite().openConnection();
                    return conn;
                } catch (SQLException e) {
                    plugin.getLogger().warning("Unable to open connection to database. If this is a bug, please report it.");
                }
            } else if (dbType.equalsIgnoreCase("MYSQL")) {
                try {
                    conn = new MySQL().openConnection();
                    plugin.getLogger().info("Connected to MYSQL database!");
                    return conn;
                } catch (SQLException e) {
                    e.printStackTrace();
                    plugin.getLogger().warning("Unable to open connection to database. If this is a bug, please report it.");
                }
            } else {
                plugin.getLogger().info("Invalid database type. Expected SQLITE or MYSQL, received: " + dbType);
                Bukkit.getPluginManager().disablePlugin(PlayerReferrals.getInstance());
            }
        }
        return null;
    }

    public static void initializeTables(Connection conn) {

        String tablePrefix = plugin.getConfig().getString("table-prefix");
        plugin.getLogger().info(tablePrefix);
        String[] sql = {"CREATE TABLE IF NOT EXISTS `" + tablePrefix + "referrals` (`uuid` CHAR(36) PRIMARY KEY NOT NULL, `referrer-uuid` CHAR(36));",
                "CREATE TABLE IF NOT EXISTS `" + tablePrefix + "referral-scores` (`uuid` CHAR(36) PRIMARY KEY NOT NULL, `score` INT DEFAULT 0 NOT NULL);",
                "CREATE TABLE IF NOT EXISTS `" + tablePrefix + "awaiting-reward` (`uuid` CHAR(36) PRIMARY KEY NOT NULL, `reward-score` INT NOT NULL, `referral-uuid` CHAR(36) NOT NULL)",
                "CREATE TABLE IF NOT EXISTS `" + tablePrefix + "ip-addresses` (`uuid` CHAR(36) PRIMARY KEY NOT NULL, `ip` TEXT)"};
        int i;
        for (i = 0; i < sql.length; i++) {
            try {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(sql[i]);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    public static ExecutorService getDbThread() {
        return dbThread;
    }

}
