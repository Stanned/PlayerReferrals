package com.stanexe.playerreferrals.commands;

import com.stanexe.playerreferrals.PlayerReferrals;
import com.stanexe.playerreferrals.util.DatabaseUtil;
import com.stanexe.playerreferrals.util.RefUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReferralLeaderboardCommand implements CommandExecutor {

    final PlayerReferrals plugin = PlayerReferrals.getInstance();
    final FileConfiguration messages = plugin.getMessagesConfig();
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!plugin.getConfig().getBoolean("enable-leaderboard")) {
            commandSender.sendMessage(ChatColor.RED + "The leaderboard is disabled.");
        }
        List<RefUser> topPlayers = DatabaseUtil.getTopPlayers();
        StringBuilder message = new StringBuilder(ChatColor.DARK_AQUA + "Referral Leaderboard:");
        int i = 1;
        if (topPlayers == null) {
            commandSender.sendMessage(ChatColor.RED + "There are no players with referrals yet.");
        } else {
            for (RefUser user : topPlayers) {
                message.append("\n").append(i).append(". ").append(Bukkit.getOfflinePlayer(user.getUUID()).getName()).append(" - ").append(user.getPlayerScore());
                i++;
            }
            commandSender.sendMessage(message.toString());
        }


        return true;
    }
}
