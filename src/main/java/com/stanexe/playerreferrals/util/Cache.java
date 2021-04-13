package com.stanexe.playerreferrals.util;

import com.stanexe.playerreferrals.PlayerReferrals;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Cache {

    private final static HashMap<UUID, Integer> scoresCache = new HashMap<>();
    private final static HashMap<UUID, UUID> referralsCache = new HashMap<>();
    private final static HashMap<UUID, String> ipCache = new HashMap<>();
    private final static HashMap<UUID, ArrayList<Map.Entry<UUID, Integer>>> awaitingRewardCache = new HashMap<>();
    private final PlayerReferrals plugin = PlayerReferrals.getInstance();
    private final String tablePrefix = plugin.getConfig().getString("table-prefix");

    public Cache() {
        DatabaseUtil.getDbThread().execute(() -> {
            boolean tablesInitialized = false;
            tablesInitialized = DatabaseUtil.initializeTables(DatabaseUtil.getConn());
            while (!tablesInitialized) {
                return;
            }
            Connection conn = DatabaseUtil.getConn();
            try {
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `" + tablePrefix + "referral-scores`;");
                ResultSet rsScores = stmt.executeQuery();
                int i = 0;
                while (rsScores.next()) {
                    scoresCache.put(UUID.fromString(rsScores.getString("uuid")), rsScores.getInt("score"));
                    i++;
                }
                stmt.close();

                PreparedStatement stmt2 = conn.prepareStatement("SELECT * FROM `" + tablePrefix + "referrals`;");
                ResultSet rsReferrals = stmt2.executeQuery();
                int x = 0;
                while (rsReferrals.next()) {
                    referralsCache.put(UUID.fromString(rsReferrals.getString("uuid")), UUID.fromString(rsReferrals.getString("referrer-uuid")));
                    x++;
                }

                PreparedStatement stmt3 = conn.prepareStatement("SELECT * FROM `" + tablePrefix + "ip-addresses`;");
                ResultSet rsIps = stmt3.executeQuery();
                int y = 0;
                while (rsIps.next()) {
                    ipCache.put(UUID.fromString(rsIps.getString("uuid")), rsIps.getString("ip"));
                    y++;
                }

                PreparedStatement stmt4 = conn.prepareStatement("SELECT * FROM `" + tablePrefix + "awaiting-reward`");
                ResultSet rsAwaiting = stmt4.executeQuery();
                int z = 0;
                while (rsAwaiting.next()) {
                    UUID uuid = UUID.fromString(rsAwaiting.getString("uuid"));
                    ArrayList<Map.Entry<UUID, Integer>> rewards;
                    if (awaitingRewardCache.containsKey(uuid)) {
                        rewards = awaitingRewardCache.get(uuid);
                    } else {
                        rewards = new ArrayList<>();
                    }
                    rewards.add(new AbstractMap.SimpleEntry<>(UUID.fromString(rsAwaiting.getString("uuid")),
                            rsAwaiting.getInt("reward-score")));
                    awaitingRewardCache.put(uuid, rewards);
                    z++;
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    public static HashMap<UUID, Integer> getScoresCache() {
        return scoresCache;
    }

    public static void addToScoresCache(UUID uuid, Integer score) {
        scoresCache.put(uuid, score);
    }

    public static HashMap<UUID, UUID> getReferralsCache() {
        return referralsCache;
    }

    public static void addToReferralsCache(UUID uuid, UUID referrerUUID) {
        referralsCache.put(uuid, referrerUUID);
    }

    public static void removeFromReferralsCache(UUID uuid) {
        referralsCache.remove(uuid);
    }

    public static void addToIpCache(UUID uuid, String ip) {
        ipCache.put(uuid, ip);
    }

    public static HashMap<UUID, String> getIpCache() {
        return ipCache;
    }

    public static HashMap<UUID, ArrayList<Map.Entry<UUID, Integer>>> getAwaitingRewardCache() {
        return awaitingRewardCache;
    }

    public static void removeFromAwaitingRewardCache(UUID uuid) {
        awaitingRewardCache.remove(uuid);
    }

    public static void addToAwaitingRewardCache(UUID uuid, UUID referralUUID, Integer score) {
        ArrayList<Map.Entry<UUID, Integer>> rewards = new ArrayList<>();
        if (awaitingRewardCache.containsKey(uuid)) {
            rewards = awaitingRewardCache.get(uuid);
        }
        rewards.add(new AbstractMap.SimpleEntry<>(referralUUID, score));
        awaitingRewardCache.put(uuid, rewards);
    }

}
