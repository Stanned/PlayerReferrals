package com.stanexe.playerreferrals.util;

import com.stanexe.playerreferrals.PlayerReferrals;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class RefUser {
    private final UUID uuid;

    private final PlayerReferrals plugin = PlayerReferrals.getInstance();

    public RefUser(UUID providedUuid) {
        uuid = providedUuid;
    }

    public void adjustPlayerScore(int value) {
        Connection conn;
        try {
            conn = new SQLite().openConnection();
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO `referral-scores` (`uuid`, `score`) VALUES(?, ?) ON CONFLICT(`uuid`) DO UPDATE SET `score`= score + ?;");
            stmt.setString(1, String.valueOf(uuid));
            stmt.setInt(2, value);
            stmt.setInt(3, value);
            stmt.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public int getPlayerScore() {
        Connection conn;
        try {
            conn = DatabaseUtil.getConn();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `referral-scores` WHERE `uuid` = ?;");
            stmt.setString(1, String.valueOf(uuid));
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                int score = resultSet.getInt("score");
                stmt.close();
                return score;
            } else {
                stmt.close();
                return 0;
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return -1;
    }

    public void setPlayerScore(int newScore) {
        Connection conn;
        try {
            conn = DatabaseUtil.getConn();
            PreparedStatement stmt = conn.prepareStatement("INSERT OR REPLACE INTO `referral-scores` (`uuid`, `score`) VALUES (?, ?)");
            stmt.setString(1, String.valueOf(uuid));
            stmt.setInt(2, newScore);
            stmt.executeUpdate();
            Bukkit.getLogger().info(String.valueOf(conn.isValid(1)));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public boolean isInTime() {
        long ticksPlayed = Bukkit.getOfflinePlayer(uuid).getStatistic(Statistic.PLAY_ONE_MINUTE);
        long minutesAllowed = plugin.getConfig().getLong("minutes-allowed");
        long minutesPlayed = ticksPlayed / 20 / 60;
        return minutesPlayed <= minutesAllowed;
    }

    public long getMinutesRemaining() {
        long ticksPlayed = Bukkit.getOfflinePlayer(uuid).getStatistic(Statistic.PLAY_ONE_MINUTE);
        long minutesAllowed = plugin.getConfig().getLong("minutes-allowed");
        long minutesPlayed = ticksPlayed / 20 / 60;
        return minutesAllowed - minutesPlayed;
    }

    public UUID getReferrer() {
        Connection conn;
        try {
            conn = DatabaseUtil.getConn();
            PreparedStatement stmt = conn.prepareStatement("SELECT `referrer-uuid` FROM `referrals` WHERE `uuid` = ?");
            stmt.setString(1, String.valueOf(uuid));
            ResultSet resultSet = stmt.executeQuery();
            stmt.close();
            if (!resultSet.next()) {
                return null;
            } else {
                UUID referrerUuid = UUID.fromString(resultSet.getString("referrer-uuid"));
                return referrerUuid;
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public void setReferrer(UUID referrerUUID) {
        Connection conn;
        try {
            conn = DatabaseUtil.getConn();
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO `referrals` (`uuid`, `referrer-uuid`) VALUES (?, ?) ON CONFLICT(`uuid`) DO UPDATE SET `referrer-uuid`=?;");
            stmt.setString(1, String.valueOf(uuid));
            stmt.setString(2, String.valueOf(referrerUUID));
            stmt.setString(3, String.valueOf(referrerUUID));
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void giveRewards() {
        //TODO: Give actual rewards
        Bukkit.getPlayer(uuid).sendMessage("You have received rewards.");
    }

    public void resetReferrer() {
    }
}
