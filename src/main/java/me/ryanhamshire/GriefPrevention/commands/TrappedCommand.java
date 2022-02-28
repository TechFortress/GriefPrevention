package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.PlayerData;
import me.ryanhamshire.GriefPrevention.PlayerRescueTask;
import me.ryanhamshire.GriefPrevention.TextMode;
import me.ryanhamshire.GriefPrevention.events.SaveTrappedPlayerEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class TrappedCommand implements CommandExecutor
{
    // reference to plugin main class
    private final GriefPrevention plugin;

    /**
     * Class constructor
     * @param plugin reference to main class
     */
    public TrappedCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("trapped")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args)
    {
        // command sender must be in game player
        if (!(sender instanceof Player player)) return false;

        //FEATURE: empower players who get "stuck" in an area where they don't have permission to build to save themselves

        PlayerData playerData = plugin.dataStore.getPlayerData(player.getUniqueId());
        Claim claim = plugin.dataStore.getClaimAt(player.getLocation(), false, playerData.lastClaim);

        //if another /trapped is pending, ignore this slash command
        if (playerData.pendingTrapped)
        {
            return true;
        }

        //if the player isn't in a claim or has permission to build, tell him to man up
        if (claim == null || claim.checkPermission(player, ClaimPermission.Build, null) == null)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.NotTrappedHere);
            return true;
        }

        //rescue destination may be set by GPFlags or other plugin, ask to find out
        SaveTrappedPlayerEvent event = new SaveTrappedPlayerEvent(claim);
        Bukkit.getPluginManager().callEvent(event);

        //if the player is in the nether or end, he's screwed (there's no way to programmatically find a safe place for him)
        if (player.getWorld().getEnvironment() != World.Environment.NORMAL && event.getDestination() == null)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.TrappedWontWorkHere);
            return true;
        }

        //if the player is in an administrative claim and AllowTrappedInAdminClaims is false, he should contact an admin
        if (!GriefPrevention.instance.config_claims_allowTrappedInAdminClaims && claim.isAdminClaim() && event.getDestination() == null)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.TrappedWontWorkHere);
            return true;
        }
        //send instructions
        GriefPrevention.sendMessage(player, TextMode.Instr, Messages.RescuePending);

        //create a task to rescue this player in a little while
        PlayerRescueTask task = new PlayerRescueTask(player, player.getLocation(), event.getDestination());
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 200L);  //20L ~ 1 second

        return true;
    }
}
