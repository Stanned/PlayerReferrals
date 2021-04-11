package com.stanexe.playerreferrals.util;

import com.stanexe.playerreferrals.PlayerReferrals;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.stanexe.playerreferrals.util.StringTools.colors;

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getPlayerScore() {
        Connection conn;
        try {
            conn = DatabaseUtil.getConn();
            PreparedStatement stmt;
            if (conn != null) {
                stmt = conn.prepareStatement("SELECT * FROM `referral-scores` WHERE `uuid` = ?;");
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
            } else {
                Bukkit.getLogger().warning("An error has occurred in the database. Please report this to the plugin author if this keeps happening.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void setPlayerScore(int newScore) {
        Connection conn;
        try {
            conn = DatabaseUtil.getConn();
            if (conn != null) {
                PreparedStatement stmt = conn.prepareStatement("INSERT OR REPLACE INTO `referral-scores` (`uuid`, `score`) VALUES (?, ?)");
                stmt.setString(1, String.valueOf(uuid));
                stmt.setInt(2, newScore);
                stmt.executeUpdate();
                Bukkit.getLogger().info(String.valueOf(conn.isValid(1)));
            } else {
                Bukkit.getLogger().warning("An error has occurred in the database. Please report this to the plugin author if this keeps happening.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
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

    public String getStoredIP() {
        Connection conn;
        try {
            conn = DatabaseUtil.getConn();
            if (conn != null) {
                PreparedStatement stmt = conn.prepareStatement("SELECT `ip` FROM `ip-addresses` WHERE `uuid` = ?;");
                stmt.setString(1, String.valueOf(uuid));
                ResultSet resultSet = stmt.executeQuery();
                if (resultSet.next()) {
                    String ip = resultSet.getString("ip");
                    stmt.close();
                    return ip;
                }
                stmt.close();
            } else {
                Bukkit.getLogger().warning("An error has occurred in the database. Please report this to the plugin author if this keeps happening.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void storeIP() {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null) {
            return;
        }
        String ip = Objects.requireNonNull(p.getAddress()).getHostString();
        Bukkit.getLogger().info(ip);
        Connection conn;
        try {
            conn = DatabaseUtil.getConn();
            if (conn != null) {
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO `ip-addresses` (`uuid`, `ip`) VALUES (?, ?) ON CONFLICT(`uuid`) DO UPDATE SET `ip`=?;");
                stmt.setString(1, String.valueOf(uuid));
                stmt.setString(2, ip);
                stmt.setString(3, ip);
                stmt.execute();
                stmt.close();
            } else {
                Bukkit.getLogger().warning("An error has occurred in the database. Please report this to the plugin author if this keeps happening.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public UUID getReferrer() {
        Connection conn;
        try {
            conn = DatabaseUtil.getConn();
            if (conn != null) {
                PreparedStatement stmt = conn.prepareStatement("SELECT `referrer-uuid` FROM `referrals` WHERE `uuid` = ?");
                stmt.setString(1, String.valueOf(uuid));
                ResultSet resultSet = stmt.executeQuery();
                if (!resultSet.next()) {
                    return null;
                } else {
                    UUID referrerUuid = UUID.fromString(resultSet.getString("referrer-uuid"));
                    stmt.close();
                    return referrerUuid;
                }
            } else {
                Bukkit.getLogger().warning("An error has occurred in the database. Please report this to the plugin author if this keeps happening.");
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setReferrer(UUID referrerUUID) {
        Connection conn;
        try {
            conn = DatabaseUtil.getConn();
            if (conn != null) {
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO `referrals` (`uuid`, `referrer-uuid`) VALUES (?, ?) ON CONFLICT(`uuid`) DO UPDATE SET `referrer-uuid`=?;");
                stmt.setString(1, String.valueOf(uuid));
                stmt.setString(2, String.valueOf(referrerUUID));
                stmt.setString(3, String.valueOf(referrerUUID));
                stmt.execute();
                stmt.close();
            } else {
                Bukkit.getLogger().warning("An error has occurred in the database. Please report this to the plugin author if this keeps happening.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void giveReferredRewards(UUID referralUUID) {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null) {
            return;
        }
        String msg = plugin.getConfig().getString("referred-rewards.message");
        List<String> commands = (List<String>) plugin.getConfig().getList("referred-rewards.commands");
        if (msg != null) {
            msg = msg.replace("%username%", p.getName());
            msg = msg.replace("%newScore%", String.valueOf(this.getPlayerScore()));
            msg = msg.replace("%referralUsername%", String.valueOf(Bukkit.getOfflinePlayer(referralUUID).getName()));
            p.sendMessage(colors(msg));
        }
        if (commands != null) {
            for (String cmd : commands) {
                cmd = cmd.replace("%username%", p.getName());
                cmd = cmd.replace("%referralUsername%", String.valueOf(Bukkit.getOfflinePlayer(referralUUID).getName()));
                String finalCmd = cmd;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
                    }
                }.runTask(plugin);

            }
        }

    }

    public void giveReferralRewards(UUID referralUUID, int score) {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null) {
            return;
        }
        String msg = plugin.getConfig().getString("referral-rewards.message");
        List<String> commands = (List<String>) plugin.getConfig().getList("referral-rewards.commands");
        if (msg != null) {
            msg = msg.replace("%username%", p.getName());
            msg = msg.replace("%referredUsername%", String.valueOf(Bukkit.getOfflinePlayer(referralUUID).getName()));
            p.sendMessage(colors(msg));
        }
        if (commands != null) {
            for (String cmd : commands) {
                cmd = cmd.replace("%username%", p.getName());
                cmd = cmd.replace("%referredUsername%", String.valueOf(Bukkit.getOfflinePlayer(referralUUID).getName()));
                String finalCmd = cmd;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
                    }
                }.runTask(plugin);
            }
        }
        if (Milestones.isMilestone(score)) {
            this.giveMilestoneRewards(score);
        }
    }

    private void giveMilestoneRewards(int score) {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null) {
            return;
        }
        List<String> commands = plugin.getConfig().getStringList("milestones." + score + ".commands");
        String msg = plugin.getConfig().getString("milestones." + score + ".message");
        if (msg != null) {
            msg = msg.replace("%username%", p.getName());
            msg = msg.replace("%score%", String.valueOf(score));
            p.sendMessage(colors(msg));
        }
        if (commands.size() != 0) {
            for (String cmd : commands) {
                cmd = cmd.replace("%username%", p.getName());
                cmd = cmd.replace("%score%", String.valueOf(score));
                String finalCmd = cmd;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
                    }
                }.runTask(plugin);
            }

        }
    }

    public void resetReferrer() {
        Connection conn;
        try {
            conn = DatabaseUtil.getConn();
            PreparedStatement stmt;
            if (conn != null) {
                stmt = conn.prepareStatement("DELETE FROM `referrals` WHERE `uuid` = ?;");
                if (stmt != null) {
                    stmt.setString(1, String.valueOf(uuid));
                    stmt.execute();
                    stmt.close();
                }
            } else {
                Bukkit.getLogger().warning("An error has occurred in the database. Please report this to the plugin author if this keeps happening.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void setOfflineRewards(UUID referralUUID, int referralScore) {
        Connection conn;
        try {
            conn = DatabaseUtil.getConn();
            if (conn != null) {
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO `awaiting-reward` VALUES (?,?,?);");
                stmt.setString(1, String.valueOf(uuid));
                stmt.setInt(2, referralScore);
                stmt.setString(3, String.valueOf(referralUUID));
                stmt.execute();
                stmt.close();

            } else {
                Bukkit.getLogger().warning("An error has occurred in the database. Please report this to the plugin author if this keeps happening.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void claimPendingRewards() {
        Connection conn;
        try {
            conn = DatabaseUtil.getConn();

            PreparedStatement stmt;
            if (conn != null) {
                stmt = conn.prepareStatement("SELECT * FROM `awaiting-reward` WHERE `uuid` = ?;");
                stmt.setString(1, String.valueOf(uuid));
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    Bukkit.getLogger().info("Giving pending reward...");
                    this.giveReferralRewards(
                            UUID.fromString(resultSet.getString("referral-uuid")),
                            resultSet.getInt("reward-score")
                    );
                }
                stmt.close();
                Bukkit.getLogger().info("Done giving rewards.");
                PreparedStatement stmt2 = conn.prepareStatement("DELETE FROM `awaiting-reward` WHERE `uuid`=?;");
                stmt2.execute();
                stmt2.close();
            } else {
                Bukkit.getLogger().warning("An error has occurred in the database. Please report this to the plugin author if this keeps happening.");
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
