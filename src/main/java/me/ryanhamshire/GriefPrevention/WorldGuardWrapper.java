package me.ryanhamshire.GriefPrevention;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

class WorldGuardWrapper
{
    private final WorldGuardPlugin worldGuard;

    public WorldGuardWrapper() throws IllegalArgumentException, IllegalStateException, ClassCastException
    {
        this.worldGuard = JavaPlugin.getPlugin(WorldGuardPlugin.class);
    }

    public boolean canBuild(Location lesserCorner, Location greaterCorner, Player creatingPlayer)
    {
        try
        {
            if (lesserCorner.getWorld() == null)
            {
                return true;
            }

            LocalPlayer localPlayer = this.worldGuard.wrapPlayer(creatingPlayer);
            WorldGuardPlatform platform = WorldGuard.getInstance().getPlatform();
            World world = BukkitAdapter.adapt(lesserCorner.getWorld());

            if (platform.getSessionManager().hasBypass(localPlayer, world))
            {
                return true;
            }

            RegionManager manager = platform.getRegionContainer().get(world);
            if (manager == null)
            {
                return true;
            }

            ProtectedCuboidRegion tempRegion = new ProtectedCuboidRegion(
                    "GP_TEMP",
                    BlockVector3.at(lesserCorner.getX(), 0, lesserCorner.getZ()),
                    BlockVector3.at(greaterCorner.getX(), world.getMaxY(), greaterCorner.getZ()));

            return manager.getApplicableRegions(tempRegion).queryState(localPlayer, Flags.BUILD) == StateFlag.State.ALLOW;
        }
        catch (Throwable rock)
        {
            GriefPrevention.AddLogEntry("WorldGuard was found but unable to hook into. It could be that you're " +
                    "using an outdated version or WorldEdit broke their API... again." +
                    "Consider updating/downgrading/removing WorldGuard or disable WorldGuard integration in GP's config " +
                    "(CreationRequiresWorldGuardBuildPermission). If you're going to report this please be kind because " +
                    "WorldEdit's API hasn't been :c", CustomLogEntryTypes.Debug, false);
        }

        return true;
    }
}