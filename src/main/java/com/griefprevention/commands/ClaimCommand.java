package com.griefprevention.commands;

import com.griefprevention.visualization.BoundaryVisualization;
import com.griefprevention.visualization.VisualizationType;
import me.ryanhamshire.GriefPrevention.AutoExtendClaimTask;
import me.ryanhamshire.GriefPrevention.CreateClaimResult;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.PlayerData;
import me.ryanhamshire.GriefPrevention.ShovelMode;
import me.ryanhamshire.GriefPrevention.TextMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class ClaimCommand extends CommandHandler
{
    public ClaimCommand(@NotNull GriefPrevention plugin)
    {
        super(plugin, "claim");
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args)
    {
        if (!(sender instanceof Player player))
            return false;

        World world = player.getWorld();
        if (!plugin.claimsEnabledForWorld(world))
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.ClaimsDisabledWorld);
            return true;
        }

        PlayerData playerData = plugin.dataStore.getPlayerData(player.getUniqueId());

        //if he's at the claim count per player limit already and doesn't have permission to bypass, display an error message
        if (plugin.config_claims_maxClaimsPerPlayer > 0 &&
                !player.hasPermission("griefprevention.overrideclaimcountlimit") &&
                playerData.getClaims().size() >= plugin.config_claims_maxClaimsPerPlayer)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.ClaimCreationFailedOverClaimCountLimit);
            return true;
        }

        int radius;

        //allow for specifying the radius
        if (args.length > 0)
        {
            if (needsShovel(playerData, player))
            {
                GriefPrevention.sendMessage(player, TextMode.Err, Messages.RadiusRequiresGoldenShovel);
                return true;
            }

            try
            {
                radius = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException e)
            {
                return false;
            }

            int minRadius = getClaimMinRadius();
            if (radius < minRadius)
            {
                GriefPrevention.sendMessage(player, TextMode.Err, Messages.MinimumRadius, String.valueOf(minRadius));
                return true;
            }
        }

        // If the player has no claims, allow them to create their starter claim via command instead of chest placement.
        else if (playerData.getClaims().isEmpty())
        {
            radius = plugin.config_claims_automaticClaimsForNewPlayersRadius;
            if (radius < 0)
                radius = getClaimMinRadius();
        }

        //if player has any claims, respect claim minimum size setting
        else
        {
            //if player has exactly one land claim, this requires the claim modification tool to be in hand (or creative mode player)
            if (needsShovel(playerData, player))
            {
                GriefPrevention.sendMessage(player, TextMode.Err, Messages.MustHoldModificationToolForThat);
                return true;
            }

            radius = getClaimMinRadius();
        }

        if (radius < 0) radius = 0;

        Location lc = player.getLocation().add(-radius, 0, -radius);
        Location gc = player.getLocation().add(radius, 0, radius);

        UUID playerId;
        if (playerData.shovelMode == ShovelMode.Admin)
        {
            playerId = null;
        } else
        {
            //player must have sufficient unused claim blocks
            int area = Math.abs((gc.getBlockX() - lc.getBlockX() + 1) * (gc.getBlockZ() - lc.getBlockZ() + 1));
            int remaining = playerData.getRemainingClaimBlocks();
            if (remaining < area)
            {
                GriefPrevention.sendMessage(player, TextMode.Err, Messages.CreateClaimInsufficientBlocks, String.valueOf(area - remaining));
                plugin.dataStore.tryAdvertiseAdminAlternatives(player);
                return true;
            }
            playerId = player.getUniqueId();
        }

        CreateClaimResult result = plugin.dataStore.createClaim(world,
                lc.getBlockX(), gc.getBlockX(),
                lc.getBlockY() - plugin.config_claims_claimsExtendIntoGroundDistance - 1,
                world.getHighestBlockYAt(gc) - plugin.config_claims_claimsExtendIntoGroundDistance - 1,
                lc.getBlockZ(), gc.getBlockZ(),
                playerId, null, null, player);
        if (!result.succeeded || result.claim == null)
        {
            if (result.claim != null)
            {
                GriefPrevention.sendMessage(player, TextMode.Err, Messages.CreateClaimFailOverlapShort);

                BoundaryVisualization.visualizeClaim(player, result.claim, VisualizationType.CONFLICT_ZONE);
            }
            else
            {
                GriefPrevention.sendMessage(player, TextMode.Err, Messages.CreateClaimFailOverlapRegion);
            }
        }
        else
        {
            GriefPrevention.sendMessage(player, TextMode.Success, Messages.CreateClaimSuccess);

            //link to a video demo of land claiming, based on world type
            if (plugin.creativeRulesApply(player.getLocation()))
            {
                GriefPrevention.sendMessage(player, TextMode.Instr, Messages.CreativeBasicsVideo2, DataStore.CREATIVE_VIDEO_URL);
            }
            else if (plugin.claimsEnabledForWorld(world))
            {
                GriefPrevention.sendMessage(player, TextMode.Instr, Messages.SurvivalBasicsVideo2, DataStore.SURVIVAL_VIDEO_URL);
            }
            BoundaryVisualization.visualizeClaim(player, result.claim, VisualizationType.CLAIM);
            playerData.claimResizing = null;
            playerData.lastShovelLocation = null;

            AutoExtendClaimTask.scheduleAsync(result.claim);

        }
        return true;
    }

    private boolean needsShovel(@NotNull PlayerData playerData, @NotNull Player player)
    {
        return playerData.getClaims().size() < 2
                && player.getGameMode() != GameMode.CREATIVE
                && player.getInventory().getItemInMainHand().getType() != plugin.config_claims_modificationTool;
    }

    private int getClaimMinRadius()
    {
        return (int) Math.ceil(Math.sqrt(plugin.config_claims_minArea) / 2);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args)
    {
        if (args.length != 1)
            return List.of();
        return TabCompletions.integer(args, 5, false);
    }

}
