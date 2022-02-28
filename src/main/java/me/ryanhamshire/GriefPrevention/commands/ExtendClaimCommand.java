package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.PlayerData;
import me.ryanhamshire.GriefPrevention.TextMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

public final class ExtendClaimCommand implements CommandExecutor
{
    // reference to plugin main class
    private final GriefPrevention plugin;

    /**
     * Class constructor
     * @param plugin reference to main class
     */
    public ExtendClaimCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("extendclaim")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args)
    {
        // command sender must be in game player
        if (!(sender instanceof Player player)) return false;

        if (args.length < 1)
        {
            //link to a video demo of land claiming, based on world type
            if (GriefPrevention.instance.creativeRulesApply(player.getLocation()))
            {
                GriefPrevention.sendMessage(player, TextMode.Instr, Messages.CreativeBasicsVideo2, DataStore.CREATIVE_VIDEO_URL);
            }
            else if (GriefPrevention.instance.claimsEnabledForWorld(player.getLocation().getWorld()))
            {
                GriefPrevention.sendMessage(player, TextMode.Instr, Messages.SurvivalBasicsVideo2, DataStore.SURVIVAL_VIDEO_URL);
            }
            return false;
        }

        int amount;
        try
        {
            amount = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException e)
        {
            //link to a video demo of land claiming, based on world type
            if (GriefPrevention.instance.creativeRulesApply(player.getLocation()))
            {
                GriefPrevention.sendMessage(player, TextMode.Instr, Messages.CreativeBasicsVideo2, DataStore.CREATIVE_VIDEO_URL);
            }
            else if (GriefPrevention.instance.claimsEnabledForWorld(player.getLocation().getWorld()))
            {
                GriefPrevention.sendMessage(player, TextMode.Instr, Messages.SurvivalBasicsVideo2, DataStore.SURVIVAL_VIDEO_URL);
            }
            return false;
        }

        //requires claim modification tool in hand
        if (player.getGameMode() != GameMode.CREATIVE && player.getItemInHand().getType() != GriefPrevention.instance.config_claims_modificationTool)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.MustHoldModificationToolForThat);
            return true;
        }

        //must be standing in a land claim
        PlayerData playerData = plugin.dataStore.getPlayerData(player.getUniqueId());
        Claim claim = plugin.dataStore.getClaimAt(player.getLocation(), true, playerData.lastClaim);
        if (claim == null)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.StandInClaimToResize);
            return true;
        }

        //must have permission to edit the land claim you're in
        Supplier<String> errorMessage = claim.checkPermission(player, ClaimPermission.Edit, null);
        if (errorMessage != null)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.NotYourClaim);
            return true;
        }

        //determine new corner coordinates
        org.bukkit.util.Vector direction = player.getLocation().getDirection();
        if (direction.getY() > .75)
        {
            GriefPrevention.sendMessage(player, TextMode.Info, Messages.ClaimsExtendToSky);
            return true;
        }

        if (direction.getY() < -.75)
        {
            GriefPrevention.sendMessage(player, TextMode.Info, Messages.ClaimsAutoExtendDownward);
            return true;
        }

        Location lc = claim.getLesserBoundaryCorner();
        Location gc = claim.getGreaterBoundaryCorner();
        int newx1 = lc.getBlockX();
        int newx2 = gc.getBlockX();
        int newy1 = lc.getBlockY();
        int newy2 = gc.getBlockY();
        int newz1 = lc.getBlockZ();
        int newz2 = gc.getBlockZ();

        //if changing Z only
        if (Math.abs(direction.getX()) < .3)
        {
            if (direction.getZ() > 0)
            {
                newz2 += amount;  //north
            }
            else
            {
                newz1 -= amount;  //south
            }
        }

        //if changing X only
        else if (Math.abs(direction.getZ()) < .3)
        {
            if (direction.getX() > 0)
            {
                newx2 += amount;  //east
            }
            else
            {
                newx1 -= amount;  //west
            }
        }

        //diagonals
        else
        {
            if (direction.getX() > 0)
            {
                newx2 += amount;
            }
            else
            {
                newx1 -= amount;
            }

            if (direction.getZ() > 0)
            {
                newz2 += amount;
            }
            else
            {
                newz1 -= amount;
            }
        }

        //attempt resize
        playerData.claimResizing = claim;
        plugin.dataStore.resizeClaimWithChecks(player, playerData, newx1, newx2, newy1, newy2, newz1, newz2);
        playerData.claimResizing = null;

        return true;
    }
}
