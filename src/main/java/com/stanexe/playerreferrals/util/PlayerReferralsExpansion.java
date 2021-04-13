package com.stanexe.playerreferrals.util;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlayerReferralsExpansion extends PlaceholderExpansion {

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
            return Bukkit.getOfflinePlayer(refUser.getReferrer()).getName();
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
