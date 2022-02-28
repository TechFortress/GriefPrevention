package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.TextMode;
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

public final class UnseparateCommand implements CommandExecutor, TabCompleter
{
    // reference to plugin main class
    private final GriefPrevention plugin;

    /**
     * Class constructor
     * @param plugin reference to main class
     */
    public UnseparateCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("unseparate")).setExecutor(this);
    }

    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args)
    {
        if (args.length == 1)
        {
            // match first argument with other online players
            return plugin.commandHandler.matchOnlinePlayers(sender, args[0]);
        }
        else if (args.length == 2)
        {
            // match second argument with other online players, then remove first argument from list
            List<String> returnList = plugin.commandHandler.matchOnlinePlayers(sender, args[1]);
            returnList.remove(args[0]);
            return returnList;
        }
        // return empty list if not first or second argument
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

        //requires two player names
        if (args.length < 2) return false;

        //validate target players
        OfflinePlayer targetPlayer = plugin.resolvePlayerByName(args[0]);
        if (targetPlayer == null)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.PlayerNotFound2);
            return true;
        }

        OfflinePlayer targetPlayer2 = plugin.resolvePlayerByName(args[1]);
        if (targetPlayer2 == null)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.PlayerNotFound2);
            return true;
        }

        plugin.setIgnoreStatus(targetPlayer, targetPlayer2, GriefPrevention.IgnoreMode.None);
        plugin.setIgnoreStatus(targetPlayer2, targetPlayer, GriefPrevention.IgnoreMode.None);

        GriefPrevention.sendMessage(player, TextMode.Success, Messages.UnSeparateConfirmation);

        return true;
    }
}
