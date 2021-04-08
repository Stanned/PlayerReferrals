package com.stanexe.playerreferrals.util;

import com.stanexe.playerreferrals.PlayerReferrals;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseUtil {
    static PlayerReferrals plugin = PlayerReferrals.getInstance();
    static String dbType = plugin.getConfig().getString("database-type");

    public static Connection getConn() {
        if (dbType.equalsIgnoreCase("SQLITE")) {
            try {
                return new SQLite().openConnection();
            } catch (SQLException throwables) {
                plugin.getLogger().warning("Unable to open connection to database. If this is a bug, please report it.");
            }
        } else if (dbType.equalsIgnoreCase("MYSQL")) {
            return null;
//          try {
//              return new MySQL().openConnection();
//          } catch (SQLException throwables) {
//              getLogger().warning("Unable to open connection to database. If this is a bug, please report it.");
//          }
        } else {
            plugin.getLogger().info("Invalid database type. Expected SQLITE or MYSQL, received: " + dbType);
            Bukkit.getPluginManager().disablePlugin(PlayerReferrals.getInstance());
        }
        return null;
    }


    static ExecutorService dbThread = Executors.newSingleThreadExecutor();

    public static ExecutorService getDbThread() {
        return dbThread;
    }

    public static void initializeTables(Connection conn) {
        getDbThread().execute(new Runnable() {
            @Override
            public void run() {
                String[] sql = {"CREATE TABLE IF NOT EXISTS `referrals` (`uuid` CHAR(36) PRIMARY KEY NOT NULL, `referrer-uuid` CHAR(36));",
                        "CREATE TABLE IF NOT EXISTS `referral-scores` (`uuid` CHAR(36) PRIMARY KEY NOT NULL, `score` INT DEFAULT 0 NOT NULL);",
                        "CREATE TABLE IF NOT EXISTS `awaiting-reward` (`uuid` CHAR(36) PRIMARY KEY NOT NULL, `reward-types` TEXT)"};
                int i;
                for (i = 0; i < sql.length; i++) {
                    try {
                        Statement stmt = conn.createStatement();
                        stmt.execute(sql[i]);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


    }


    public static void returnConnection(Connection conn) {
        if (dbType.equalsIgnoreCase("SQLITE")) {
            try {
                conn.close();
            } catch (SQLException throwables) {
                return;
            }
        } else if (dbType.equalsIgnoreCase("MYSQL")) {
            // Put connection back in pool (?)
        }
    }
}
