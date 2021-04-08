package com.stanexe.playerreferrals;

import com.stanexe.playerreferrals.commands.ReferralAdminCommand;
import com.stanexe.playerreferrals.commands.ReferralCommand;
import com.stanexe.playerreferrals.util.DatabaseUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.Objects;

public final class PlayerReferrals extends JavaPlugin {

    private static PlayerReferrals instance;
    private FileConfiguration messagesConfig;
    private static Connection conn;

    public static PlayerReferrals getInstance() {
        return instance;
    }
    static String dbType;
    @Override
    public void onEnable() {
        instance = this;
        // Save default configs
        this.saveDefaultConfig();
        createMessagesConfig();

        // Connect to database
        DatabaseUtil.initializeTables(DatabaseUtil.getConn());

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


    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

}
