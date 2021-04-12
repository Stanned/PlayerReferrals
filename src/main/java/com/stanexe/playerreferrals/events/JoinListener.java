package com.stanexe.playerreferrals.events;

import com.stanexe.playerreferrals.PlayerReferrals;
import com.stanexe.playerreferrals.util.DatabaseUtil;
import com.stanexe.playerreferrals.util.RefUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    final PlayerReferrals plugin = PlayerReferrals.getInstance();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        DatabaseUtil.getDbThread().execute(() -> {
            RefUser refUser = new RefUser(e.getPlayer().getUniqueId());

            if (plugin.getConfig().getBoolean("ip-check")) {
                refUser.storeIP();
            }


            refUser.claimPendingRewards();
        });
    }
}
