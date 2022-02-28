package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.PlayerData;
import me.ryanhamshire.GriefPrevention.TextMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class SiegeCommand implements CommandExecutor, TabCompleter
{
    // reference to plugin main class
    private final GriefPrevention plugin;

    /**
     * Class constructor
     * @param plugin reference to main class
     */
    public SiegeCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("siege")).setExecutor(this);
    }

    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args)
    {
        if (args.length == 1)
        {
            // match first argument with online players
            return plugin.commandHandler.matchOnlinePlayers(sender, args[0]);
        }
        // if not first argument, return empty list
        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args)
    {
        // command sender must be in game player
        if (!(sender instanceof Player player)) return false;

        //error message for when siege mode is disabled
        if (!plugin.siegeEnabledForWorld(player.getWorld()))
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.NonSiegeWorld);
            return true;
        }

        //requires one argument
        if (args.length > 1)
        {
            return false;
        }

        //can't start a siege when you're already involved in one
        Player attacker = player;
        PlayerData attackerData = plugin.dataStore.getPlayerData(attacker.getUniqueId());
        if (attackerData.siegeData != null)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.AlreadySieging);
            return true;
        }

        //can't start a siege when you're protected from pvp combat
        if (attackerData.pvpImmune)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.CantFightWhileImmune);
            return true;
        }

        //if a player name was specified, use that
        Player defender = null;
        if (args.length >= 1)
        {
            defender = plugin.getServer().getPlayer(args[0]);
            if (defender == null)
            {
                GriefPrevention.sendMessage(player, TextMode.Err, Messages.PlayerNotFound2);
                return true;
            }
        }

        //otherwise use the last player this player was in pvp combat with
        else if (attackerData.lastPvpPlayer.length() > 0)
        {
            defender = plugin.getServer().getPlayer(attackerData.lastPvpPlayer);
            if (defender == null)
            {
                return false;
            }
        }
        else
        {
            return false;
        }

        // First off, you cannot siege yourself, that's just
        // silly:
        if (attacker.getName().equals(defender.getName()))
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.NoSiegeYourself);
            return true;
        }

        //victim must not have the permission which makes him immune to siege
        if (defender.hasPermission("griefprevention.siegeimmune"))
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.SiegeImmune);
            return true;
        }

        //victim must not be under siege already
        PlayerData defenderData = plugin.dataStore.getPlayerData(defender.getUniqueId());
        if (defenderData.siegeData != null)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.AlreadyUnderSiegePlayer);
            return true;
        }

        //victim must not be pvp immune
        if (defenderData.pvpImmune)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.NoSiegeDefenseless);
            return true;
        }

        Claim defenderClaim = plugin.dataStore.getClaimAt(defender.getLocation(), false, null);

        //defender must have some level of permission there to be protected
        if (defenderClaim == null || defenderClaim.checkPermission(defender, ClaimPermission.Access, null) != null)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.NotSiegableThere);
            return true;
        }

        //attacker must be close to the claim he wants to siege
        if (!defenderClaim.isNear(attacker.getLocation(), 25))
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.SiegeTooFarAway);
            return true;
        }

        //claim can't be under siege already
        if (defenderClaim.siegeData != null)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.AlreadyUnderSiegeArea);
            return true;
        }

        //can't siege admin claims
        if (defenderClaim.isAdminClaim())
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.NoSiegeAdminClaim);
            return true;
        }

        //can't be on cooldown
        if (plugin.dataStore.onCooldown(attacker, defender, defenderClaim))
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.SiegeOnCooldown);
            return true;
        }

        //start the siege
        plugin.dataStore.startSiege(attacker, defender, defenderClaim);

        //confirmation message for attacker, warning message for defender
        GriefPrevention.sendMessage(defender, TextMode.Warn, Messages.SiegeAlert, attacker.getName());
        GriefPrevention.sendMessage(player, TextMode.Success, Messages.SiegeConfirmed, defender.getName());

        return true;
    }
}
