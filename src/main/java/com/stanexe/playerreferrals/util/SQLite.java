package com.stanexe.playerreferrals.util;

import com.stanexe.playerreferrals.PlayerReferrals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLite {

    private Connection conn;
    PlayerReferrals plugin = PlayerReferrals.getInstance();
    public Connection openConnection() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            return conn;
        }
        conn = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/" + plugin.getName() + ".db");
        return conn;
    }


}
