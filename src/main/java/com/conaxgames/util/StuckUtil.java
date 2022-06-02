package com.conaxgames.util;

import com.google.common.base.Preconditions;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class StuckUtil
{

    public static Location getSafeNearbyTeleportLocation(Player player) {
        DataStore dataStore = GriefPrevention.instance.dataStore;

        Claim claimAt = dataStore.getClaimAt(player.getLocation(), true, null);
        if (claimAt != null) {
            Location greaterBoundaryCorner = claimAt.getGreaterBoundaryCorner();

            for (int i = 1; i < 5; i++)
            {
                Location toCheck = greaterBoundaryCorner.add(i, 0, 0);
                Claim possibleLoc = dataStore.getClaimAt(toCheck, true, null);

                if (possibleLoc == null) {
                    return getHighestLocation(toCheck, null).add(0, 1.5, 0);
                }
            }
        }
        return player.getLocation();
    }

    public static Location getHighestLocation(Location origin, Location def) {
        Preconditions.checkNotNull((Object)origin, "The location cannot be null");
        Location cloned = origin.clone();
        World world = cloned.getWorld();
        if (world != null)
        {
            int x = cloned.getBlockX();
            int y = world.getMaxHeight();
            int z = cloned.getBlockZ();
            while (y > origin.getBlockY())
            {
                Block block;
                if ((block = world.getBlockAt(x, --y, z)).isEmpty()) continue;
                Location next = block.getLocation();
                next.setPitch(origin.getPitch());
                next.setYaw(origin.getYaw());
                return next;
            }
        }
        return def;
    }

}
