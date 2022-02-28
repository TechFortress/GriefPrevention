package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.PlayerData;
import me.ryanhamshire.GriefPrevention.TextMode;
import me.ryanhamshire.GriefPrevention.events.TrustChangedEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class UntrustCommand implements CommandExecutor, TabCompleter
{
    // reference to plugin main class
    private final GriefPrevention plugin;

    /**
     * Class constructor
     * @param plugin reference to main class
     */
    public UntrustCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("untrust")).setExecutor(this);
    }

    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args)
    {
        // command sender must be in game player
        if (!(sender instanceof Player player)) return Collections.emptyList();

        if (args.length == 1)
        {
            // match first argument with other online players
            // NOTE: getting list of players with claim trust appears to be too expensive to perform here
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

        //requires exactly one parameter, the other player's name
        if (args.length != 1) return false;

        //determine which claim the player is standing in
        Claim claim = plugin.dataStore.getClaimAt(player.getLocation(), true /*ignore height*/, null);

        //determine whether a single player or clearing permissions entirely
        boolean clearPermissions = false;
        OfflinePlayer otherPlayer = null;
        if (args[0].equals("all"))
        {
            if (claim == null || claim.checkPermission(player, ClaimPermission.Edit, null) == null)
            {
                clearPermissions = true;
            }
            else
            {
                GriefPrevention.sendMessage(player, TextMode.Err, Messages.ClearPermsOwnerOnly);
                return true;
            }
        }
        else
        {
            //validate player argument or group argument
            if (!args[0].startsWith("[") || !args[0].endsWith("]"))
            {
                otherPlayer = plugin.resolvePlayerByName(args[0]);
                if (!clearPermissions && otherPlayer == null && !args[0].equals("public"))
                {
                    //bracket any permissions - at this point it must be a permission without brackets
                    if (args[0].contains("."))
                    {
                        args[0] = "[" + args[0] + "]";
                    }
                    else
                    {
                        GriefPrevention.sendMessage(player, TextMode.Err, Messages.PlayerNotFound2);
                        return true;
                    }
                }

                //correct to proper casing
                if (otherPlayer != null)
                    args[0] = otherPlayer.getName();
            }
        }

        //if no claim here, apply changes to all his claims
        if (claim == null)
        {
            PlayerData playerData = plugin.dataStore.getPlayerData(player.getUniqueId());

            String idToDrop = args[0];
            if (otherPlayer != null)
            {
                idToDrop = otherPlayer.getUniqueId().toString();
            }

            //calling event
            TrustChangedEvent event = new TrustChangedEvent(player, playerData.getClaims(), null, false, idToDrop);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled())
            {
                return true;
            }

            //dropping permissions
            for (Claim targetClaim : event.getClaims()) {
                claim = targetClaim;

                //if untrusting "all" drop all permissions
                if (clearPermissions)
                {
                    claim.clearPermissions();
                }

                //otherwise drop individual permissions
                else
                {
                    claim.dropPermission(idToDrop);
                    claim.managers.remove(idToDrop);
                }

                //save changes
                plugin.dataStore.saveClaim(claim);
            }

            //beautify for output
            if (args[0].equals("public"))
            {
                args[0] = "the public";
            }

            //confirmation message
            if (!clearPermissions)
            {
                GriefPrevention.sendMessage(player, TextMode.Success, Messages.UntrustIndividualAllClaims, args[0]);
            }
            else
            {
                GriefPrevention.sendMessage(player, TextMode.Success, Messages.UntrustEveryoneAllClaims);
            }
        }

        //otherwise, apply changes to only this claim
        else if (claim.checkPermission(player, ClaimPermission.Manage, null) != null)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.NoPermissionTrust, claim.getOwnerName());
            return true;
        }
        else
        {
            //if clearing all
            if (clearPermissions)
            {
                //requires owner
                if (claim.checkPermission(player, ClaimPermission.Edit, null) != null)
                {
                    GriefPrevention.sendMessage(player, TextMode.Err, Messages.UntrustAllOwnerOnly);
                    return true;
                }

                //calling the event
                TrustChangedEvent event = new TrustChangedEvent(player, claim, null, false, args[0]);
                Bukkit.getPluginManager().callEvent(event);

                if (event.isCancelled())
                {
                    return true;
                }

                event.getClaims().forEach(Claim::clearPermissions);
                GriefPrevention.sendMessage(player, TextMode.Success, Messages.ClearPermissionsOneClaim);
            }

            //otherwise individual permission drop
            else
            {
                String idToDrop = args[0];
                if (otherPlayer != null)
                {
                    idToDrop = otherPlayer.getUniqueId().toString();
                }
                boolean targetIsManager = claim.managers.contains(idToDrop);
                if (targetIsManager && claim.checkPermission(player, ClaimPermission.Edit, null) != null)  //only claim owners can untrust managers
                {
                    GriefPrevention.sendMessage(player, TextMode.Err, Messages.ManagersDontUntrustManagers, claim.getOwnerName());
                    return true;
                }
                else
                {
                    //calling the event
                    TrustChangedEvent event = new TrustChangedEvent(player, claim, null, false, idToDrop);
                    Bukkit.getPluginManager().callEvent(event);

                    if (event.isCancelled())
                    {
                        return true;
                    }

                    event.getClaims().forEach(targetClaim -> targetClaim.dropPermission(event.getIdentifier()));

                    //beautify for output
                    if (args[0].equals("public"))
                    {
                        args[0] = "the public";
                    }

                    GriefPrevention.sendMessage(player, TextMode.Success, Messages.UntrustIndividualSingleClaim, args[0]);
                }
            }

            //save changes
            plugin.dataStore.saveClaim(claim);
        }

        return true;
    }
}
