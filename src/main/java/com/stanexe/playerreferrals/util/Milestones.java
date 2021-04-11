package com.stanexe.playerreferrals.util;

import com.stanexe.playerreferrals.PlayerReferrals;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Set;

public class Milestones {

    PlayerReferrals plugin = PlayerReferrals.getInstance();
    boolean enabled;
    static ConfigurationSection milestonesSection;
    private static final ArrayList<Integer> milestones = new ArrayList<>();

    public Milestones() {
        enabled = plugin.getConfig().getBoolean("enable-milestones");
        if (enabled) {
            milestonesSection = plugin.getConfig().getConfigurationSection("milestones");
            if (milestonesSection == null) {
                Bukkit.getLogger().warning("\"milestones\" section does not exist, even though it is enabled in the config.");
            }
            Set<String> keys = milestonesSection.getKeys(false);
            for (String key : keys) {
                try {
                    int score = Integer.parseInt(key);
                    milestones.add(score);
                } catch (NumberFormatException e) {
                    Bukkit.getLogger().warning("Could not resolve \"" + key + "\"as a valid score. Ignoring it.");
                }

            }


        }
    }

    public static boolean isMilestone(int score) {
        return milestones.contains(score);
    }



}
