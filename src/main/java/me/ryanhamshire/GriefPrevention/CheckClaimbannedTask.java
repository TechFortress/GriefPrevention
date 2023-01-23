package me.ryanhamshire.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CheckClaimbannedTask implements Runnable {

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(p.getLocation(), false, null);
            if (claim != null) {
                PlayerData whoData = GriefPrevention.instance.dataStore.getPlayerData(p.getUniqueId());
                if (!whoData.ignoreClaims && claim.checkBanned(whoData.playerID)) {
                    p.eject();
                    GriefPrevention.ejectPlayerFromBannedClaim(p);
                }
            }
        }
    }

}
