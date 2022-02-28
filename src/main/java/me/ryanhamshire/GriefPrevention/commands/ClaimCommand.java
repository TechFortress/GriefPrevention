package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.CreateClaimResult;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.PlayerData;
import me.ryanhamshire.GriefPrevention.TextMode;
import me.ryanhamshire.GriefPrevention.Visualization;
import me.ryanhamshire.GriefPrevention.VisualizationType;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class ClaimCommand implements CommandExecutor
{
    // reference to plugin main class
    private final GriefPrevention plugin;

    /**
     * Class constructor
     * @param plugin reference to main class
     */
    public ClaimCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("claim")).setExecutor(this);
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label, final String[] args)
    {
        // command sender must be in game player
        if (!(sender instanceof Player player)) return false;

        if (!GriefPrevention.instance.claimsEnabledForWorld(player.getWorld()))
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.ClaimsDisabledWorld);
            return true;
        }

        PlayerData playerData = plugin.dataStore.getPlayerData(player.getUniqueId());

        //if he's at the claim count per player limit already and doesn't have permission to bypass, display an error message
        if (GriefPrevention.instance.config_claims_maxClaimsPerPlayer > 0 &&
                !player.hasPermission("griefprevention.overrideclaimcountlimit") &&
                playerData.getClaims().size() >= GriefPrevention.instance.config_claims_maxClaimsPerPlayer)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.ClaimCreationFailedOverClaimCountLimit);
            return true;
        }

        //default is chest claim radius, unless -1
        int radius = GriefPrevention.instance.config_claims_automaticClaimsForNewPlayersRadius;
        if (radius < 0) radius = (int) Math.ceil(Math.sqrt(GriefPrevention.instance.config_claims_minArea) / 2);

        //if player has any claims, respect claim minimum size setting
        if (playerData.getClaims().size() > 0)
        {
            //if player has exactly one land claim, this requires the claim modification tool to be in hand (or creative mode player)
            if (playerData.getClaims().size() == 1 && player.getGameMode() != GameMode.CREATIVE && player.getItemInHand().getType() != GriefPrevention.instance.config_claims_modificationTool)
            {
                GriefPrevention.sendMessage(player, TextMode.Err, Messages.MustHoldModificationToolForThat);
                return true;
            }

            radius = (int) Math.ceil(Math.sqrt(GriefPrevention.instance.config_claims_minArea) / 2);
        }

        //allow for specifying the radius
        if (args.length > 0)
        {
            if (playerData.getClaims().size() < 2 && player.getGameMode() != GameMode.CREATIVE && player.getItemInHand().getType() != GriefPrevention.instance.config_claims_modificationTool)
            {
                GriefPrevention.sendMessage(player, TextMode.Err, Messages.RadiusRequiresGoldenShovel);
                return true;
            }

            int specifiedRadius;
            try
            {
                specifiedRadius = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException e)
            {
                return false;
            }

            if (specifiedRadius < radius)
            {
                GriefPrevention.sendMessage(player, TextMode.Err, Messages.MinimumRadius, String.valueOf(radius));
                return true;
            }
            else
            {
                radius = specifiedRadius;
            }
        }

        if (radius < 0) radius = 0;

        Location lc = player.getLocation().add(-radius, 0, -radius);
        Location gc = player.getLocation().add(radius, 0, radius);

        //player must have sufficient unused claim blocks
        int area = Math.abs((gc.getBlockX() - lc.getBlockX() + 1) * (gc.getBlockZ() - lc.getBlockZ() + 1));
        int remaining = playerData.getRemainingClaimBlocks();
        if (remaining < area)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.CreateClaimInsufficientBlocks, String.valueOf(area - remaining));
            GriefPrevention.instance.dataStore.tryAdvertiseAdminAlternatives(player);
            return true;
        }

        CreateClaimResult result = plugin.dataStore.createClaim(lc.getWorld(),
                lc.getBlockX(), gc.getBlockX(),
                lc.getBlockY() - GriefPrevention.instance.config_claims_claimsExtendIntoGroundDistance - 1,
                gc.getWorld().getHighestBlockYAt(gc) - GriefPrevention.instance.config_claims_claimsExtendIntoGroundDistance - 1,
                lc.getBlockZ(), gc.getBlockZ(),
                player.getUniqueId(), null, null, player);
        if (!result.succeeded)
        {
            if (result.claim != null)
            {
                GriefPrevention.sendMessage(player, TextMode.Err, Messages.CreateClaimFailOverlapShort);

                Visualization visualization = Visualization.FromClaim(result.claim, player.getEyeLocation().getBlockY(), VisualizationType.ErrorClaim, player.getLocation());
                Visualization.Apply(player, visualization);
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
            if (GriefPrevention.instance.creativeRulesApply(player.getLocation()))
            {
                GriefPrevention.sendMessage(player, TextMode.Instr, Messages.CreativeBasicsVideo2, DataStore.CREATIVE_VIDEO_URL);
            }
            else if (GriefPrevention.instance.claimsEnabledForWorld(player.getLocation().getWorld()))
            {
                GriefPrevention.sendMessage(player, TextMode.Instr, Messages.SurvivalBasicsVideo2, DataStore.SURVIVAL_VIDEO_URL);
            }
            Visualization visualization = Visualization.FromClaim(result.claim, player.getEyeLocation().getBlockY(), VisualizationType.Claim, player.getLocation());
            Visualization.Apply(player, visualization);
            playerData.claimResizing = null;
            playerData.lastShovelLocation = null;

            plugin.autoExtendClaim(result.claim);
        }

        return true;
    }

}
