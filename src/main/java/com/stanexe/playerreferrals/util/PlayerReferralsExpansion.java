package com.stanexe.playerreferrals.util;

import com.stanexe.playerreferrals.PlayerReferrals;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;



public class PlayerReferralsExpansion extends PlaceholderExpansion {
    private final PlayerReferrals plugin = PlayerReferrals.getInstance();
    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "playerreferrals";
    }

    @Override
    public @NotNull String getAuthor() {
        return "StanEXE";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {

        if (identifier.equals("score")) {
            RefUser refUser = new RefUser(player.getUniqueId());
            return String.valueOf(refUser.getPlayerScore());
        }

        if (identifier.equals("referrerign")) {
            RefUser refUser = new RefUser(player.getUniqueId());
            UUID uuid = refUser.getReferrer();
            if (uuid != null) {
                return Bukkit.getOfflinePlayer(uuid).getName();
            } else {
                String text = plugin.getMessagesConfig().getString("referrerign-placeholder-empty");
                if (text == null) {
                    return "";
                }
                return StringTools.colors(text);
            }

        }

        if (identifier.equals("minutesremaining")) {
            RefUser refUser = new RefUser(player.getUniqueId());
            long minutes = refUser.getMinutesRemaining();
            if (minutes < 0) {
                minutes = 0;
            }
            return String.valueOf(minutes);
        }

        return null;
    }

}
