package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.PlayerData;
import me.ryanhamshire.GriefPrevention.TextMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class RestrictSubclaimCommand implements CommandExecutor
{
    // reference to plugin main class
    private final GriefPrevention plugin;

    /**
     * Class constructor
     * @param plugin reference to main class
     */
    public RestrictSubclaimCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("restrictsubclaim")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args)
    {
        // command sender must be in game player
        if (!(sender instanceof Player player)) return false;

        PlayerData playerData = plugin.dataStore.getPlayerData(player.getUniqueId());
        Claim claim = plugin.dataStore.getClaimAt(player.getLocation(), true, playerData.lastClaim);
        if (claim == null || claim.parent == null)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.StandInSubclaim);
            return true;
        }

        // If player has /ignoreclaims on, continue
        // If admin claim, fail if this user is not an admin
        // If not an admin claim, fail if this user is not the owner
        if (!playerData.ignoreClaims && (claim.isAdminClaim() ? !player.hasPermission("griefprevention.adminclaims") : !player.getUniqueId().equals(claim.parent.ownerID)))
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.OnlyOwnersModifyClaims, claim.getOwnerName());
            return true;
        }

        if (claim.getSubclaimRestrictions())
        {
            claim.setSubclaimRestrictions(false);
            GriefPrevention.sendMessage(player, TextMode.Success, Messages.SubclaimUnrestricted);
        }
        else
        {
            claim.setSubclaimRestrictions(true);
            GriefPrevention.sendMessage(player, TextMode.Success, Messages.SubclaimRestricted);
        }
        plugin.dataStore.saveClaim(claim);
        return true;
    }
}
