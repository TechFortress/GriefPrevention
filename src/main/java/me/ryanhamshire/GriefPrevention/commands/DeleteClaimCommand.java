package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.CustomLogEntryTypes;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.PlayerData;
import me.ryanhamshire.GriefPrevention.TextMode;
import me.ryanhamshire.GriefPrevention.Visualization;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class DeleteClaimCommand implements CommandExecutor
{
    // reference to plugin main class
    private final GriefPrevention plugin;

    /**
     * Class constructor
     * @param plugin reference to main class
     */
    public DeleteClaimCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("deleteclaim")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args)
    {
        // command sender must be in game player
        if (!(sender instanceof Player player)) return false;

        //determine which claim the player is standing in
        Claim claim = plugin.dataStore.getClaimAt(player.getLocation(), true /*ignore height*/, null);

        if (claim == null)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.DeleteClaimMissing);
        }
        else
        {
            //deleting an admin claim additionally requires the adminclaims permission
            if (!claim.isAdminClaim() || player.hasPermission("griefprevention.adminclaims"))
            {
                PlayerData playerData = plugin.dataStore.getPlayerData(player.getUniqueId());
                if (claim.children.size() > 0 && !playerData.warnedAboutMajorDeletion)
                {
                    GriefPrevention.sendMessage(player, TextMode.Warn, Messages.DeletionSubdivisionWarning);
                    playerData.warnedAboutMajorDeletion = true;
                }
                else
                {
                    claim.removeSurfaceFluids(null);
                    plugin.dataStore.deleteClaim(claim, true, true);

                    //if in a creative mode world, /restorenature the claim
                    if (GriefPrevention.instance.creativeRulesApply(claim.getLesserBoundaryCorner()) || GriefPrevention.instance.config_claims_survivalAutoNatureRestoration)
                    {
                        GriefPrevention.instance.restoreClaim(claim, 0);
                    }

                    GriefPrevention.sendMessage(player, TextMode.Success, Messages.DeleteSuccess);
                    GriefPrevention.AddLogEntry(player.getName() + " deleted " + claim.getOwnerName() + "'s claim at " + GriefPrevention.getfriendlyLocationString(claim.getLesserBoundaryCorner()), CustomLogEntryTypes.AdminActivity);

                    //revert any current visualization
                    Visualization.Revert(player);

                    playerData.warnedAboutMajorDeletion = false;
                }
            }
            else
            {
                GriefPrevention.sendMessage(player, TextMode.Err, Messages.CantDeleteAdminClaim);
            }
        }

        return true;
    }
}
