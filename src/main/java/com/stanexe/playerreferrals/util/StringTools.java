package com.stanexe.playerreferrals.util;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class StringTools {

    public static String colors(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static String placeholders(String string, Player p) {
        string = string.replaceAll("%player%", p.getName());
        string = string.replaceAll("%uuid%", p.getUniqueId().toString());
        return string;
    }

}
