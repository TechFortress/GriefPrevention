package com.conaxgames.claim;

import com.conaxgames.util.Config;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.UUID;

public class ClaimEntryProvider
{

    public Config cfg;
    public FileConfiguration config;

    public ClaimEntryProvider() {
        cfg = new Config("claim-bans", GriefPrevention.instance);
        config = cfg.getConfig();
    }

    /**
     * If the player is blocked from entering the claim.
     * @param claimOwner Owner of the claim being entered.
     * @param attemptingToEnter UUID of the player attempting to enter.
     * @return true if the attempting player is blocked.
     */
    public boolean isBlocked(UUID claimOwner, UUID attemptingToEnter)
    {
        if (config.contains(claimOwner.toString()))
        {
            List<String> bans = config.getStringList(claimOwner.toString());
            return bans.contains(attemptingToEnter.toString());
        }

        return false;
    }

    /**
     * If the player is blocked from entering the claim.
     * @param player Owner player who is blocking toBlock.
     * @param toBlock UUID of the player to add to the blocked list.
     */
    public void block(UUID player, UUID toBlock)
    {
        List<String> bans = cfg.getConfig().getStringList(player.toString());
        if (!bans.contains(toBlock.toString())) {
            bans.add(toBlock.toString());
        }
        cfg.getConfig().set(player.toString(), bans);
        cfg.save();
    }

    /**
     * If the player is blocked from entering the claim.
     * @param player Owner player who is unblocking toUnblock.
     * @param toUnblock UUID of the player to remove from the blocked list.
     */
    public void unblock(UUID player, UUID toUnblock)
    {
        List<String> bans = cfg.getConfig().getStringList(player.toString());
        bans.remove(toUnblock.toString());
        cfg.getConfig().set(player.toString(), bans);
        cfg.save();
    }

    /**
     * If the player is blocked from entering the claim.
     * @param player Owner player who is unblocking toUnblock.
     */
    public List<String> list(UUID player)
    {
        return cfg.getConfig().getStringList(player.toString());
    }
}
