package com.stanexe.playerreferrals.commands;

import com.stanexe.playerreferrals.util.RefUser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static com.stanexe.playerreferrals.util.StringTools.colors;

public class ReferralCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(colors("This command can only be used by a player, for admin commands please use /referraladmin"));
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            RefUser refUser = new RefUser(player.getUniqueId());
            UUID referrer = refUser.getReferrer();
            String message = "&3Your referral score is &B" + refUser.getPlayerScore() + "&r";
            if (referrer == null) {
                message = message + "\n&3You did not enter a referral username yet.";
            } else {
                message = message + "\n&3You have been referred by: &b" + Bukkit.getOfflinePlayer(refUser.getReferrer()).getName() + "&r";
            }
            if (refUser.isInTime() && referrer == null) {
                message = message + "\n&3If you have been referred by someone, you have &b" + refUser.getMinutesRemaining() + " minutes &3remaining to enter their referral name using &b/referral <username>";
            }
            player.sendMessage(colors(message));
        } else {
            RefUser refUser = new RefUser(player.getUniqueId());
            UUID referrerUUID = refUser.getReferrer();
            if (referrerUUID != null) {
                player.sendMessage(colors("&3You have already been referred by someone and cannot enter another code."));
                return true;
            } else if (!refUser.isInTime()) {
                player.sendMessage(colors("&3Unfortunately, you have run out of time to enter a referral code. Sorry!"));
                return true;
            }
            String providedUsername = args[0];
            OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(providedUsername);
            if (!oPlayer.hasPlayedBefore()) {
                player.sendMessage(colors("&3That player has never joined before."));
                return true;
            }
            referrerUUID = oPlayer.getUniqueId();
            refUser.setReferrer(referrerUUID);



        }
        return true;
    }
}
