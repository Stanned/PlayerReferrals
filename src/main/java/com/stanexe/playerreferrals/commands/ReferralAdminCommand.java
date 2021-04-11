package com.stanexe.playerreferrals.commands;

import com.stanexe.playerreferrals.PlayerReferrals;
import com.stanexe.playerreferrals.util.DatabaseUtil;
import com.stanexe.playerreferrals.util.RefUser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Objects;
import java.util.UUID;

import static com.stanexe.playerreferrals.util.StringTools.colors;

public class ReferralAdminCommand implements CommandExecutor {
    private final PlayerReferrals plugin = PlayerReferrals.getInstance();
    private final FileConfiguration messagesConfig = plugin.getMessagesConfig();
    private final String prefix = colors(messagesConfig.getString("prefix"));

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender, label);
            return true;
        }
        switch (args[0]) {
            default:
                sendHelpMessage(sender, label);
                break;
            case "check":
                if (args.length != 1) {
                    OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(args[1]);
                    if (oPlayer.hasPlayedBefore()) {
                        DatabaseUtil.getDbThread().execute(() -> {
                            RefUser refUser = new RefUser(oPlayer.getUniqueId());
                            long score = refUser.getPlayerScore();
                            if (score == -1) {
                                sender.sendMessage("It appears a database error has occurred. If this is a bug, please report it.");
                            } else {
                                String message = "&b" + oPlayer.getName()
                                        + "\n&3Score: &b" + score;
                                UUID referrerUUID = refUser.getReferrer();
                                if (referrerUUID == null) {
                                    message = message + "\n&3They have not been referred by anyone.";
                                } else {
                                    message = message + "\n&3They have been referred by &b" + Bukkit.getOfflinePlayer(referrerUUID).getName();
                                }
                                sender.sendMessage(colors(message));
                            }
                        });
                    } else {
                        sender.sendMessage(prefix + colors(messagesConfig.getString("admin-no-player-found")));
                    }
                } else {
                    sender.sendMessage(colors(prefix + colors(messagesConfig.getString("admin-check-usage"))));

                }
                break;
            case "set":
                if (args.length != 2) {
                    OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(args[1]);
                    if (oPlayer.hasPlayedBefore()) {
                        int newScore;

                        try {
                            newScore = Integer.parseInt(args[2]);
                        } catch (NumberFormatException e) {
                            return true;
                        }
                        DatabaseUtil.getDbThread().execute(() -> {
                            new RefUser(oPlayer.getUniqueId()).setPlayerScore(newScore);
                            sender.sendMessage(colors("&3The score of &b" + oPlayer.getName() + " &3has been set to &b" + newScore + "&3."));
                        });
                    } else {
                        sender.sendMessage(prefix + colors(messagesConfig.getString("admin-no-player-found")));
                    }
                } else {
                    sender.sendMessage(colors(prefix + colors(messagesConfig.getString("admin-set-usage"))));

                }
                break;
            case "adjust":
                if (args.length != 2) {
                    OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(args[1]);
                    if (oPlayer.hasPlayedBefore()) {
                        int value;
                        try {
                            value = Integer.parseInt(args[2]);
                        } catch (NumberFormatException e) {
                            return true;
                        }
                        DatabaseUtil.getDbThread().execute(() -> {
                            RefUser refUser = new RefUser(oPlayer.getUniqueId());
                            refUser.adjustPlayerScore(value);
                            sender.sendMessage(colors("&3The score of &b" + oPlayer.getName() + " &3has been adjusted by &b" + value + "&3.&r\n" +
                                    "&3Their score is now &b" + refUser.getPlayerScore() + "&3."));
                        });
                    } else {
                        sender.sendMessage(prefix + colors(messagesConfig.getString("admin-no-player-found")));
                    }
                } else {
                    sender.sendMessage(colors(prefix + colors("&cUsage: /referraladmin adjust <player> <value>")));
                }
                break;
            case "reload":
                // FIXME: VERY INEFFICIENT
                Bukkit.getPluginManager().disablePlugin(PlayerReferrals.getInstance());
                Bukkit.getPluginManager().enablePlugin(PlayerReferrals.getInstance());
                break;
            case "about":
                String version = Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("PlayerReferrals")).getDescription().getVersion();
                sender.sendMessage(colors("&bPlayerReferrals &3is made by &bStanEXE&3.&r\n" +
                        "&3You are running version &b" + version));
                break;
            case "reset":
                if (args.length >= 2) {
                    OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(args[1]);
                    if (oPlayer.hasPlayedBefore()) {
                        DatabaseUtil.getDbThread().execute(() -> {
                            RefUser refUser = new RefUser(oPlayer.getUniqueId());
                            refUser.resetReferrer();
                            sender.sendMessage(colors("&3The referral status of &b" + oPlayer.getName() + " &3has been reset. (Not their score)"));
                        });
                    } else {
                        sender.sendMessage(prefix + colors(messagesConfig.getString("admin-no-player-found")));
                    }
                } else {
                    sender.sendMessage(colors(prefix + colors("&cUsage: /referraladmin reset <player>")));

                }
                break;

        }

        return true;
    }

    private void sendHelpMessage(CommandSender sender, String label) {
        sender.sendMessage(colors("&3/" + label + " help &8- &bShowcases this help message&r" +
                "\n&3/" + label + " check <player> &8- &bCheck the amount of referrals a player has and who they were referred by&r" +
                "\n&3/" + label + " set <player> <value> &8- &bSet the referral score of a player to a value&r" +
                "\n&3/" + label + " adjust <player> <value> &8- &bAdjust the referral score of a player using positive or negative numbers&r" +
                "\n&3/" + label + " reset <player> &8- &bResets the referrer of a player, does not alter their score&r" +
                "\n&3/" + label + " about &8- &bInformation about the plugin&r" +
                "\n&3/" + label + " reload &8- &bReloads the config and messages file&r"
        ));
    }

}
