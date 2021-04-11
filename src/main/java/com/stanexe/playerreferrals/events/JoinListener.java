package com.stanexe.playerreferrals.events;

import com.stanexe.playerreferrals.util.DatabaseUtil;
import com.stanexe.playerreferrals.util.RefUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        DatabaseUtil.getDbThread().execute(() -> {
            RefUser refUser = new RefUser(e.getPlayer().getUniqueId());
            refUser.storeIP();

            refUser.claimPendingRewards();
        });
    }
}
