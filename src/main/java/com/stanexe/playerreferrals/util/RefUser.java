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
import java.util.*;

import static com.stanexe.playerreferrals.util.DatabaseUtil.getDbType;
import static com.stanexe.playerreferrals.util.StringTools.colors;

public class RefUser {
    private final UUID uuid;

    private final PlayerReferrals plugin = PlayerReferrals.getInstance();
    private final String tablePrefix = plugin.getConfig().getString("table-prefix");

    public RefUser(UUID providedUuid) {
        uuid = providedUuid;
    }

    public void adjustPlayerScore(int value) {
        int oldScore = this.getPlayerScore();
        Cache.addToScoresCache(uuid, oldScore + value);

        Connection conn;
        try {
            conn = DatabaseUtil.getConn();
            PreparedStatement stmt;
            if (getDbType().equalsIgnoreCase("SQLITE")) {
                stmt = conn.prepareStatement("INSERT INTO `" + tablePrefix + "referral-scores` (`uuid`, `score`) VALUES(?, ?) ON CONFLICT(`uuid`) DO UPDATE SET `score`= score + ?;");
            } else {
                stmt = conn.prepareStatement("INSERT INTO `" + tablePrefix + "referral-scores` (`uuid`, `score`) VALUES(?, ?) ON DUPLICATE KEY UPDATE `score`= score + ?;");
            }
            stmt.setString(1, String.valueOf(uuid));
            stmt.setInt(2, value);
            stmt.setInt(3, value);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getPlayerScore() {

        HashMap<UUID, Integer> cache = Cache.getScoresCache();
        if (cache.containsKey(uuid)) {
            return cache.get(uuid);
        }

        Connection conn;
        try {
            conn = DatabaseUtil.getConn();
            PreparedStatement stmt;
            if (conn != null) {
                stmt = conn.prepareStatement("SELECT * FROM `" + tablePrefix + "referral-scores` WHERE `uuid` = ?;");
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
                plugin.getLogger().warning("An error has occurred in the database. Please report this to the plugin author if this keeps happening.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void setPlayerScore(int newScore) {
        Cache.addToScoresCache(uuid, newScore);
        Connection conn;
        try {
            conn = DatabaseUtil.getConn();
            if (conn != null) {
                PreparedStatement stmt;
                if (getDbType().equalsIgnoreCase("SQLITE")) {
                    stmt = conn.prepareStatement("INSERT OR REPLACE INTO `" + tablePrefix + "referral-scores` (`uuid`, `score`) VALUES (?, ?)");
                } else {
                    stmt = conn.prepareStatement("REPLACE INTO `" + tablePrefix + "referral-scores` (`uuid`, `score`) VALUES (?, ?)");
                }

                stmt.setString(1, String.valueOf(uuid));
                stmt.setInt(2, newScore);
                stmt.executeUpdate();
            } else {
                plugin.getLogger().warning("An error has occurred in the database. Please report this to the plugin author if this keeps happening.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isInTime() {
        long ticksPlayed = Bukkit.getOfflinePlayer(uuid).getStatistic(Statistic.PLAY_ONE_MINUTE);
        long minutesAllowed = plugin.getConfig().getLong("minutes-allowed");
        if (minutesAllowed == -1) {
            return true;
        }
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

        HashMap<UUID, String> cache = Cache.getIpCache();
        if (cache.containsKey(uuid)) {
            return cache.get(uuid);
        }
        return null;
    }

    public void storeIP() {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null) {
            return;
        }
        String ip = Objects.requireNonNull(p.getAddress()).getHostString();
        Cache.addToIpCache(uuid, ip);
        DatabaseUtil.getDbThread().execute(() -> {
            Connection conn;
            try {
                conn = DatabaseUtil.getConn();
                if (conn != null) {
                    PreparedStatement stmt;
                    if (getDbType().equalsIgnoreCase("SQLITE")) {
                        stmt = conn.prepareStatement("INSERT INTO `" + tablePrefix + "ip-addresses` (`uuid`, `ip`) VALUES (?, ?) ON CONFLICT(`uuid`) DO UPDATE SET `ip`=?;");
                    } else {
                        stmt = conn.prepareStatement("INSERT INTO `" + tablePrefix + "ip-addresses` (`uuid`, `ip`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `ip`=?;");
                    }

                    stmt.setString(1, String.valueOf(uuid));
                    stmt.setString(2, ip);
                    stmt.setString(3, ip);
                    stmt.execute();
                    stmt.close();
                } else {
                    plugin.getLogger().warning("An error has occurred in the database. Please report this to the plugin author if this keeps happening.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public UUID getReferrer() {

        HashMap<UUID, UUID> cache = Cache.getReferralsCache();
        if (cache.containsKey(uuid)) {
            return cache.get(uuid);
        }
        return null;
    }

    public void setReferrer(UUID referrerUUID) {
        Cache.addToReferralsCache(uuid, referrerUUID);
        DatabaseUtil.getDbThread().execute(() -> {
            Connection conn;
            try {
                conn = DatabaseUtil.getConn();
                if (conn != null) {
                    PreparedStatement stmt;
                    if (getDbType().equalsIgnoreCase("SQLITE")) {
                        stmt = conn.prepareStatement("INSERT INTO `" + tablePrefix + "referrals` (`uuid`, `referrer-uuid`) VALUES (?, ?) ON CONFLICT(`uuid`) DO UPDATE SET `referrer-uuid`=?;");
                    } else {
                        stmt = conn.prepareStatement("INSERT INTO `" + tablePrefix + "referrals` (`uuid`, `referrer-uuid`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `referrer-uuid`=?;");
                    }
                    stmt.setString(1, String.valueOf(uuid));
                    stmt.setString(2, String.valueOf(referrerUUID));
                    stmt.setString(3, String.valueOf(referrerUUID));
                    stmt.execute();
                    stmt.close();
                } else {
                    plugin.getLogger().warning("An error has occurred in the database. Please report this to the plugin author if this keeps happening.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void giveReferredRewards(UUID referralUUID) {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null) {
            return;
        }
        String msg = plugin.getConfig().getString("referred-rewards.message");
        List<String> commands = (List<String>) plugin.getConfig().getList("referred-rewards.commands");
        if (msg != null) {
            msg = msg.replace("%username%", p.getName());
            msg = msg.replace("%score%", String.valueOf(this.getPlayerScore()));
            msg = msg.replace("%referralUsername%", String.valueOf(Bukkit.getOfflinePlayer(referralUUID).getName()));
            p.sendMessage(colors(msg));
        }
        if (commands != null) {
            for (String cmd : commands) {
                cmd = cmd.replace("%username%", p.getName());
                cmd = cmd.replace("%score%", String.valueOf(this.getPlayerScore()));
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

    @SuppressWarnings("unchecked")
    public void giveReferralRewards(UUID referralUUID, int score) {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null) {
            return;
        }
        String msg = plugin.getConfig().getString("referral-rewards.message");
        List<String> commands = (List<String>) plugin.getConfig().getList("referral-rewards.commands");
        if (msg != null) {
            msg = msg.replace("%username%", p.getName());
            msg = msg.replace("%score%", String.valueOf(score));
            msg = msg.replace("%referredUsername%", String.valueOf(Bukkit.getOfflinePlayer(referralUUID).getName()));
            p.sendMessage(colors(msg));
        }
        if (commands != null) {
            for (String cmd : commands) {
                cmd = cmd.replace("%username%", p.getName());
                cmd = cmd.replace("%score%", String.valueOf(score));
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
        Cache.removeFromReferralsCache(uuid);
        DatabaseUtil.getDbThread().execute(() -> {
            Connection conn;
            try {
                conn = DatabaseUtil.getConn();
                PreparedStatement stmt;
                if (conn != null) {
                    stmt = conn.prepareStatement("DELETE FROM `" + tablePrefix + "referrals` WHERE `uuid` = ?;");
                    if (stmt != null) {
                        stmt.setString(1, String.valueOf(uuid));
                        stmt.execute();
                        stmt.close();
                    }
                } else {
                    plugin.getLogger().warning("An error has occurred in the database. Please report this to the plugin author if this keeps happening.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

    }

    public void setOfflineRewards(UUID referralUUID, int referralScore) {

        Cache.addToAwaitingRewardCache(uuid, referralUUID, referralScore);
        DatabaseUtil.getDbThread().execute(() -> {
            Connection conn;
            try {
                conn = DatabaseUtil.getConn();
                if (conn != null) {
                    PreparedStatement stmt = conn.prepareStatement("INSERT INTO `" + tablePrefix + "awaiting-reward` VALUES (?,?,?);");
                    stmt.setString(1, String.valueOf(uuid));
                    stmt.setInt(2, referralScore);
                    stmt.setString(3, String.valueOf(referralUUID));
                    stmt.execute();
                    stmt.close();
                } else {
                    plugin.getLogger().warning("An error has occurred in the database. Please report this to the plugin author if this keeps happening.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

    }

    public void claimPendingRewards() {
        ArrayList<Map.Entry<UUID, Integer>> rewards;
        HashMap<UUID, ArrayList<Map.Entry<UUID, Integer>>> cache = Cache.getAwaitingRewardCache();
        if (cache.containsKey(uuid)) {
            rewards = cache.get(uuid);
            for (Map.Entry<UUID, Integer> entry : rewards) {
                this.giveReferralRewards(entry.getKey(), entry.getValue());
            }
            Cache.removeFromAwaitingRewardCache(uuid);
            DatabaseUtil.getDbThread().execute(() -> {
                Connection conn;
                try {
                    conn = DatabaseUtil.getConn();
                    PreparedStatement stmt = null;
                    if (conn != null) {
                        stmt = conn.prepareStatement("DELETE FROM `" + tablePrefix + "awaiting-reward` WHERE `uuid`=?;");
                        stmt.setString(1, String.valueOf(uuid));
                        stmt.execute();
                        stmt.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });


        }
    }
}
