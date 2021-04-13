package com.stanexe.playerreferrals.commands;

import com.stanexe.playerreferrals.PlayerReferrals;
import com.stanexe.playerreferrals.util.RefUser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

import static com.stanexe.playerreferrals.util.StringTools.colors;

public class ReferralCommand implements CommandExecutor {
    final PlayerReferrals plugin = PlayerReferrals.getInstance();
    final FileConfiguration messages = plugin.getMessagesConfig();

    @Override
    @SuppressWarnings("deprecation")
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            String msg = messages.getString("nonplayer");
            if (msg != null) {
                sender.sendMessage(colors(msg));
            }

            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            RefUser refUser = new RefUser(player.getUniqueId());
            UUID referrer = refUser.getReferrer();
            String referralScore = messages.getString("referral.score");
            String message = null;
            if (referralScore != null) {
                message = referralScore + "&r";
            }
            if (referrer != null) {
                String referralReferred = messages.getString("referral.referred");
                if (referralReferred != null) {
                    message = message + "\n" + referralReferred + "&r";
                }

            }
            if (refUser.isInTime() && referrer == null) {
                String referralNoRefer = messages.getString("referral.no-refer");
                if (referralNoRefer != null) {
                    message = message + "\n" + referralNoRefer + "&r";
                }
                String referralExplanation = messages.getString("referral.explanation");
                if (referralExplanation != null) {
                    message = message + "\n" + referralExplanation + "&r";
                }
            }
            if (message == null) {
                return true;
            }
            sendMessage(player, refUser, message);
        } else {

                RefUser refUser = new RefUser(player.getUniqueId());
                UUID referrerUUID = refUser.getReferrer();
                // Check if player can refer
                if (referrerUUID != null) {
                    String msg = messages.getString("already-referred");
                    if (msg == null) {
                        return true;
                    }
                    sendMessage(player, refUser, msg);
                    return true;
                } else if (!refUser.isInTime()) {
                    String msg = messages.getString("out-of-time");
                    if (msg == null) {
                        return true;
                    }
                    sendMessage(player, refUser, msg);
                    return true;
                }
                // Check if player is self
                String providedUsername = args[0];
                OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(providedUsername);
                if (oPlayer.getUniqueId() == player.getUniqueId()) {
                    String msg = messages.getString("refer-self");
                    if (msg == null) {
                        return true;
                    }
                    sendMessage(player, refUser, msg);
                    return true;
                }
                RefUser oRefUser = new RefUser(oPlayer.getUniqueId());

                // Check if player is referrer of provided player
                if (!(plugin.getConfig().getBoolean("refer-each-other"))) {
                    if (oRefUser.getReferrer() == player.getUniqueId()) {
                        String msg = messages.getString("refer-by-referrer");
                        if (msg == null) {
                            return true;
                        }
                        sendMessage(player, refUser, msg);
                        return true;
                    }
                }

                // Check if player exists
                if (!oPlayer.hasPlayedBefore()) {
                    String msg = messages.getString("player-not-exist");
                    if (msg == null) {
                        return true;
                    }
                    sendMessage(player, refUser, msg);
                    return true;
                }
                // Check if ip match

                if (plugin.getConfig().getBoolean("ip-check")) {
                    String oIp = oRefUser.getStoredIP();
                    String ip = refUser.getStoredIP();
                    if (ip != null && ip.equals(oIp)) {
                        String msg = messages.getString("ip-match");
                        if (msg == null) {
                            return true;
                        }
                        sendMessage(player, refUser, msg);
                        return true;
                    }
                }

                referrerUUID = oPlayer.getUniqueId();
                refUser.setReferrer(referrerUUID);
                refUser.giveReferredRewards(oPlayer.getUniqueId());
                oRefUser.adjustPlayerScore(1);
                if (oPlayer.isOnline()) {
                    oRefUser.giveReferralRewards(player.getUniqueId(), oRefUser.getPlayerScore());
                } else {
                    oRefUser.setOfflineRewards(player.getUniqueId(), oRefUser.getPlayerScore() + 1);
                }
        }
        return true;
    }

    private void sendMessage(Player player, RefUser refUser, String msg) {
        msg = msg.replace("%username%", player.getName());
        msg = msg.replace("%score%", String.valueOf(refUser.getPlayerScore()));
        UUID referrerUUID = refUser.getReferrer();
        if (referrerUUID != null) {
            msg = msg.replace("%referrerUsername%", Objects.requireNonNull(Bukkit.getOfflinePlayer(refUser.getReferrer()).getName()));
        }
        msg = msg.replace("%minutesRemaining%", String.valueOf(refUser.getMinutesRemaining()));
        player.sendMessage(colors(msg));
    }

}
