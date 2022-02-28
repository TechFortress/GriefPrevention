package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.CustomLogEntryTypes;
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

public final class SoftMuteCommand implements CommandExecutor, TabCompleter
{
    // reference to plugin main class
    private final GriefPrevention plugin;

    /**
     * Class constructor
     * @param plugin reference to main class
     */
    public SoftMuteCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("softmute")).setExecutor(this);
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

        //requires one parameter
        if (args.length != 1) return false;

        //find the specified player
        OfflinePlayer targetPlayer = plugin.resolvePlayerByName(args[0]);
        if (targetPlayer == null)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.PlayerNotFound2);
            return true;
        }

        //toggle mute for player
        boolean isMuted = plugin.dataStore.toggleSoftMute(targetPlayer.getUniqueId());
        if (isMuted)
        {
            GriefPrevention.sendMessage(player, TextMode.Success, Messages.SoftMuted, targetPlayer.getName());
            String executorName = "console";
            if (player != null)
            {
                executorName = player.getName();
            }

            GriefPrevention.AddLogEntry(executorName + " muted " + targetPlayer.getName() + ".", CustomLogEntryTypes.AdminActivity, true);
        }
        else
        {
            GriefPrevention.sendMessage(player, TextMode.Success, Messages.UnSoftMuted, targetPlayer.getName());
        }

        return true;
    }
}
