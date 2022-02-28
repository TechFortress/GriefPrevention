package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.PlayerData;
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

public final class UnignorePlayerCommand implements CommandExecutor, TabCompleter
{
    // reference to plugin main class
    private final GriefPrevention plugin;

    /**
     * Class constructor
     * @param plugin reference to main class
     */
    public UnignorePlayerCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("unignoreplayer")).setExecutor(this);
    }

    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args)
    {
        // command sender must be in game player
        if (sender instanceof Player)
        {
            if (args.length == 1)
            {
                // if first argument, match players in sender's ignored player list
                return plugin.commandHandler.matchIgnoredPlayers(sender, args[0]);
            }
        }
        // if not first argument, return empty list
        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args)
    {
        // command sender must be in game player
        if (!(sender instanceof Player player)) return false;

        //requires target player name
        if (args.length < 1) return false;

        //validate target player
        OfflinePlayer targetPlayer = plugin.resolvePlayerByName(args[0]);
        if (targetPlayer == null)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.PlayerNotFound2);
            return true;
        }

        PlayerData playerData = plugin.dataStore.getPlayerData(player.getUniqueId());
        Boolean ignoreStatus = playerData.ignoredPlayers.get(targetPlayer.getUniqueId());
        if (ignoreStatus == null || ignoreStatus == true)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.NotIgnoringPlayer);
            return true;
        }

        plugin.setIgnoreStatus(player, targetPlayer, GriefPrevention.IgnoreMode.None);

        GriefPrevention.sendMessage(player, TextMode.Success, Messages.UnIgnoreConfirmation);

        return true;
    }
}
