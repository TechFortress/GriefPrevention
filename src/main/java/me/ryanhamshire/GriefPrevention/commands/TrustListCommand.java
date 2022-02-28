package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.TextMode;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Supplier;

public final class TrustListCommand implements CommandExecutor
{
    // reference to plugin main class
    private final GriefPrevention plugin;

    /**
     * Class constructor
     * @param plugin reference to main class
     */
    public TrustListCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("trustlist")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args)
    {
        // command sender must be in game player
        if (!(sender instanceof Player player)) return false;

        Claim claim = plugin.dataStore.getClaimAt(player.getLocation(), true, null);

        //if no claim here, error message
        if (claim == null)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.TrustListNoClaim);
            return true;
        }

        //if no permission to manage permissions, error message
        Supplier<String> errorMessage = claim.checkPermission(player, ClaimPermission.Manage, null);
        if (errorMessage != null)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, errorMessage.get());
            return true;
        }

        //otherwise build a list of explicit permissions by permission level
        //and send that to the player
        ArrayList<String> builders = new ArrayList<>();
        ArrayList<String> containers = new ArrayList<>();
        ArrayList<String> accessors = new ArrayList<>();
        ArrayList<String> managers = new ArrayList<>();
        claim.getPermissions(builders, containers, accessors, managers);

        GriefPrevention.sendMessage(player, TextMode.Info, Messages.TrustListHeader);

        StringBuilder permissions = new StringBuilder();
        permissions.append(ChatColor.GOLD).append('>');

        if (managers.size() > 0)
        {
            for (String manager : managers)
                permissions.append(plugin.trustEntryToPlayerName(manager)).append(' ');
        }

        player.sendMessage(permissions.toString());
        permissions = new StringBuilder();
        permissions.append(ChatColor.YELLOW).append('>');

        if (builders.size() > 0)
        {
            for (String builder : builders)
                permissions.append(plugin.trustEntryToPlayerName(builder)).append(' ');
        }

        player.sendMessage(permissions.toString());
        permissions = new StringBuilder();
        permissions.append(ChatColor.GREEN).append('>');

        if (containers.size() > 0)
        {
            for (String container : containers)
                permissions.append(plugin.trustEntryToPlayerName(container)).append(' ');
        }

        player.sendMessage(permissions.toString());
        permissions = new StringBuilder();
        permissions.append(ChatColor.BLUE).append('>');

        if (accessors.size() > 0)
        {
            for (String accessor : accessors)
                permissions.append(plugin.trustEntryToPlayerName(accessor)).append(' ');
        }

        player.sendMessage(permissions.toString());

        player.sendMessage(
                ChatColor.GOLD + plugin.dataStore.getMessage(Messages.Manage) + " " +
                        ChatColor.YELLOW + plugin.dataStore.getMessage(Messages.Build) + " " +
                        ChatColor.GREEN + plugin.dataStore.getMessage(Messages.Containers) + " " +
                        ChatColor.BLUE + plugin.dataStore.getMessage(Messages.Access));

        if (claim.getSubclaimRestrictions())
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.HasSubclaimRestriction);
        }

        return true;
    }
}
