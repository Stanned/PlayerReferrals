package com.stanexe.playerreferrals.commands;

import com.google.common.collect.Lists;
import com.stanexe.playerreferrals.PlayerReferrals;
import com.stanexe.playerreferrals.util.RefUser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.stanexe.playerreferrals.util.StringTools.colors;

public class ReferralAdminCommand implements TabExecutor {
    private final PlayerReferrals plugin = PlayerReferrals.getInstance();
    private final FileConfiguration messagesConfig = plugin.getMessagesConfig();
    private final String prefix = colors(messagesConfig.getString("prefix"));
    private final FileConfiguration messages = plugin.getMessagesConfig();
    private final String noPermission = messages.getString("admin-no-permission");
    @Override
    @SuppressWarnings("deprecation")
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("playerreferrals.admin.command")) {
            if (noPermission != null) {sender.sendMessage(colors(noPermission));}
            return true;
        }
        if (args.length == 0) {
            sendHelpMessage(sender, label);
            return true;
        }
        switch (args[0]) {
            default:
                sendHelpMessage(sender, label);
                break;
            case "check":
                if (!sender.hasPermission("playerreferrals.admin.check")) {
                    if (noPermission != null) {sender.sendMessage(colors(noPermission));}
                    return true;
                }
                if (args.length != 1) {
                    OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(args[1]);
                    if (oPlayer.hasPlayedBefore()) {

                            RefUser refUser = new RefUser(oPlayer.getUniqueId());
                            long score = refUser.getPlayerScore();
                            if (score == -1) {
                                sender.sendMessage("It appears a database error has occurred. If this is a bug, please report it.");
                            } else {
                                String message = null;
                                String checkScore = messages.getString("check.score");
                                if (checkScore != null) {
                                    message = checkScore + "&r";
                                }
                                UUID referrerUUID = refUser.getReferrer();
                                if (referrerUUID == null) {
                                    String checkReferredNo = messages.getString("check.referred-no");
                                    if (checkReferredNo != null) {
                                        message = message + "\n" + checkReferredNo + "&r";
                                    }
                                } else {
                                    String checkReferredYes = messages.getString("check.referred-yes");
                                    if (checkReferredYes != null) {
                                        message = message + "\n" + checkReferredYes + "&r";
                                    }
                                }
                                if (message == null) {
                                    return true;
                                }
                                sendMessage(oPlayer, refUser, sender, message);
                            }
                    } else {
                        String msg = messages.getString("admin-check-no-player-found");
                        if (msg == null) {
                            return true;
                        }
                        sendMessage(oPlayer, new RefUser(oPlayer.getUniqueId()), sender, msg);
                    }
                } else {
                    String msg = messages.getString("admin-check-usage");
                    if (msg == null) {
                        return true;
                    }
                    sendMessage(sender, msg);

                }
                break;
            case "set":
                if (!sender.hasPermission("playerreferrals.admin.set")) {
                    if (noPermission != null) {sender.sendMessage(colors(noPermission));}
                    return true;
                }
                if (args.length >= 3) {
                    OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(args[1]);
                    if (oPlayer.hasPlayedBefore()) {
                        int newScore;

                        try {
                            newScore = Integer.parseInt(args[2]);
                        } catch (NumberFormatException e) {
                            return true;
                        }
                            RefUser refUser = new RefUser(oPlayer.getUniqueId());
                            refUser.setPlayerScore(newScore);
                            String msg = messages.getString("admin-set-success");
                            if (msg == null) {
                                return true;
                            }
                            sendMessage(oPlayer, refUser, sender, msg);
                    } else {
                        String msg = messages.getString("admin-set-no-player-found");
                        if (msg == null) {
                            return true;
                        }
                        sendMessage(oPlayer, new RefUser(oPlayer.getUniqueId()), sender, msg);
                    }
                } else {
                    String msg = messages.getString("admin-set-usage");
                    if (msg == null) {
                        return true;
                    }
                    sendMessage(sender, msg);

                }
                break;
            case "adjust":
                if (!sender.hasPermission("playerreferrals.admin.adjust")) {
                    if (noPermission != null) {sender.sendMessage(colors(noPermission));}
                    return true;
                }
                if (args.length != 2) {
                    OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(args[1]);
                    if (oPlayer.hasPlayedBefore()) {
                        int value;
                        try {
                            value = Integer.parseInt(args[2]);
                        } catch (NumberFormatException e) {
                            return true;
                        }
                            RefUser refUser = new RefUser(oPlayer.getUniqueId());
                            refUser.adjustPlayerScore(value);
                            String msg = messages.getString("admin-adjust-success");
                            if (msg == null) {
                                return true;
                            }
                            sendMessage(oPlayer, new RefUser(oPlayer.getUniqueId()), sender, msg, value);

                    } else {
                        String msg = messages.getString("admin-adjust-no-player-found");
                        if (msg == null) {
                            return true;
                        }
                        sendMessage(oPlayer, new RefUser(oPlayer.getUniqueId()), sender, msg);
                    }
                } else {
                    String msg = messages.getString("admin-adjust-usage");
                    if (msg == null) {
                        return true;
                    }
                    sendMessage(sender, msg);
                }
                break;
            case "reload":
                if (!sender.hasPermission("playerreferrals.admin.reload")) {
                    if (noPermission != null) {sender.sendMessage(colors(noPermission));}
                    return true;
                }
                Bukkit.getPluginManager().disablePlugin(PlayerReferrals.getInstance());
                Bukkit.getPluginManager().enablePlugin(PlayerReferrals.getInstance());
                break;
            case "about":
                if (!sender.hasPermission("playerreferrals.admin.about")) {
                    if (noPermission != null) {sender.sendMessage(colors(noPermission));}
                }
                String version = Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("PlayerReferrals")).getDescription().getVersion();
                sender.sendMessage(colors("&bPlayerReferrals &3is made by &bStanEXE&3.&r\n" +
                        "&3You are running version &b" + version));
                break;
            case "reset":
                if (!sender.hasPermission("playerreferrals.admin.reset")) {
                    if (noPermission != null) {sender.sendMessage(colors(noPermission));}
                    return true;
                }
                if (args.length >= 2) {
                    OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(args[1]);
                    if (oPlayer.hasPlayedBefore()) {
                            RefUser refUser = new RefUser(oPlayer.getUniqueId());
                            refUser.resetReferrer();
                            String msg = messages.getString("admin-reset-success");
                            if (msg == null) {
                                return true;
                            }
                            sendMessage(oPlayer, refUser, sender, msg);
                    } else {
                        String msg = messages.getString("admin-reset-no-player-found");
                        if (msg == null) {
                            return true;
                        }
                        sendMessage(oPlayer, new RefUser(oPlayer.getUniqueId()), sender, msg);
                    }
                } else {
                    String msg = messages.getString("admin-reset-usage");
                    if (msg == null) {
                        return true;
                    }
                    sendMessage(sender, msg);

                }
                break;

        }

        return true;
    }

    private void sendMessage(OfflinePlayer oPlayer, RefUser refUser, CommandSender sender, String msg, int value) {
        msg = msg.replace("%username%", Objects.requireNonNull(oPlayer.getName()));
        msg = msg.replace("%score%", String.valueOf(refUser.getPlayerScore()));
        UUID referrerUUID = refUser.getReferrer();
        if (referrerUUID != null) {
            msg = msg.replace("%referrerUsername%", Objects.requireNonNull(Bukkit.getOfflinePlayer(refUser.getReferrer()).getName()));
        }
        msg = msg.replace("%value%", String.valueOf(value));
        sender.sendMessage(prefix + colors(msg));
    }

    private void sendMessage(CommandSender sender, String msg) {
        sender.sendMessage(prefix + colors(msg));
    }

    private void sendMessage(OfflinePlayer oPlayer, RefUser refUser, CommandSender sender, String msg) {
        msg = msg.replace("%username%", Objects.requireNonNull(oPlayer.getName()));
        msg = msg.replace("%score%", String.valueOf(refUser.getPlayerScore()));
        UUID referrerUUID = refUser.getReferrer();
        if (referrerUUID != null) {
            msg = msg.replace("%referrerUsername%", Objects.requireNonNull(Bukkit.getOfflinePlayer(refUser.getReferrer()).getName()));
        }

        sender.sendMessage(prefix + colors(msg));
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> arguments = Arrays.asList("help", "check", "set", "adjust", "reset", "about", "reload");
        List<String> Flist = Lists.newArrayList();
        if (args.length == 1) {
            for (String s : arguments) {
                if (s.toLowerCase().startsWith(args[0].toLowerCase()) && sender.hasPermission("playerreferrals.admin." + s)) {
                    Flist.add(s);
                }
            }
            return Flist;
        }
        return null;
    }
}
