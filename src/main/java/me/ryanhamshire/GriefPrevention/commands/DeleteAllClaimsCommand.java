package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.CustomLogEntryTypes;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.TextMode;
import me.ryanhamshire.GriefPrevention.Visualization;
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

public final class DeleteAllClaimsCommand implements CommandExecutor, TabCompleter
{
    // reference to plugin main class
    private final GriefPrevention plugin;

    /**
     * Class constructor
     * @param plugin reference to main class
     */
    public DeleteAllClaimsCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("deleteallclaims")).setExecutor(this);
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
        // command can be issued by player or console
        Player player = null;
        if (sender instanceof Player)
        {
            player = (Player) sender;
        }

        //requires exactly one parameter, the other player's name
        if (args.length != 1) return false;

        //try to find that player
        OfflinePlayer otherPlayer = plugin.resolvePlayerByName(args[0]);
        if (otherPlayer == null)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.PlayerNotFound2);
            return true;
        }

        //delete all that player's claims
        plugin.dataStore.deleteClaimsForPlayer(otherPlayer.getUniqueId(), true);

        GriefPrevention.sendMessage(player, TextMode.Success, Messages.DeleteAllSuccess, otherPlayer.getName());
        if (player != null)
        {
            GriefPrevention.AddLogEntry(player.getName() + " deleted all claims belonging to " + otherPlayer.getName() + ".", CustomLogEntryTypes.AdminActivity);

            //revert any current visualization
            Visualization.Revert(player);
        }

        return true;
    }
}
