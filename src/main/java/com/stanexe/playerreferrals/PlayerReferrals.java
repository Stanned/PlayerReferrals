package com.stanexe.playerreferrals;

import com.stanexe.playerreferrals.commands.ReferralAdminCommand;
import com.stanexe.playerreferrals.commands.ReferralCommand;
import com.stanexe.playerreferrals.events.JoinListener;
import com.stanexe.playerreferrals.util.DatabaseUtil;
import com.stanexe.playerreferrals.util.Milestones;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public final class PlayerReferrals extends JavaPlugin {

    private static PlayerReferrals instance;
    private FileConfiguration messagesConfig;

    public static PlayerReferrals getInstance() {
        return instance;
    }
    @Override
    public void onEnable() {
        instance = this;
        // Save default configs
        this.saveDefaultConfig();
        createMessagesConfig();

        // Connect to database
        DatabaseUtil.getDbThread().execute(() -> DatabaseUtil.initializeTables(DatabaseUtil.getConn()));

        // Init milestones
        new Milestones();

        // Events
        Bukkit.getPluginManager().registerEvents(new JoinListener(), this);

        // Commands
        Objects.requireNonNull(getCommand("referraladmin")).setExecutor(new ReferralAdminCommand());
        Objects.requireNonNull(getCommand("referral")).setExecutor(new ReferralCommand());



        getLogger().info("PlayerReferrals has been enabled.");

    }

    @Override
    public void onDisable() {
        getLogger().info("PlayerReferrals has been disabled.");
    }

    private void createMessagesConfig() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            messagesFile.getParentFile().mkdirs();
            saveResource("messages.yml", false);
        }
        messagesConfig = new YamlConfiguration();
        try {
            messagesConfig.load(messagesFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }


    public void reloadConfigs() {
        reloadConfig();
        File messagesFile = new File(getDataFolder(), "messages.yml");
        try {
            messagesConfig.load(messagesFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

}
