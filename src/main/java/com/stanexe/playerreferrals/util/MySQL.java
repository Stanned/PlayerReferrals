package com.stanexe.playerreferrals.util;

import com.stanexe.playerreferrals.PlayerReferrals;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQL {
    final PlayerReferrals plugin = PlayerReferrals.getInstance();
    private final FileConfiguration config = plugin.getConfig();
    private Connection conn;
    private final String host = config.getString("host");
    private final int port = config.getInt("port");
    private final String database = config.getString("database");
    private final String username = config.getString("username");
    private final String password = config.getString("password");

    public Connection openConnection() {
        try {
            if (conn != null && conn.isValid(1)) {
                return conn;
            }
            conn = DriverManager.getConnection("jdbc:mysql://" +
                    this.host + ":" +
                    this.port + "/" +
                    this.database, this.username, this.password
            );
        } catch (SQLException e) {
            plugin.getLogger().warning("Database error: " + e.getErrorCode());
            plugin.getLogger().warning("Please check if you entered the correct database credentials and the database is reachable.");
        }

        return conn;
    }


}
