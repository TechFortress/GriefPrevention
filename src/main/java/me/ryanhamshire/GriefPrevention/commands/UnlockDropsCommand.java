package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.PlayerData;
import me.ryanhamshire.GriefPrevention.TextMode;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class UnlockDropsCommand implements CommandExecutor
{
    // reference to plugin main class
    private final GriefPrevention plugin;

    /**
     * Class constructor
     * @param plugin reference to main class
     */
    public UnlockDropsCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("unlockdrops")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args)
    {
        // command sender must be in game player
        if (!(sender instanceof Player player)) return false;

        PlayerData playerData;

        if (player.hasPermission("griefprevention.unlockothersdrops") && args.length == 1)
        {
            Player otherPlayer = Bukkit.getPlayer(args[0]);
            if (otherPlayer == null)
            {
                GriefPrevention.sendMessage(player, TextMode.Err, Messages.PlayerNotFound2);
                return true;
            }

            playerData = plugin.dataStore.getPlayerData(otherPlayer.getUniqueId());
            GriefPrevention.sendMessage(player, TextMode.Success, Messages.DropUnlockOthersConfirmation, otherPlayer.getName());
        }
        else
        {
            playerData = plugin.dataStore.getPlayerData(player.getUniqueId());
            GriefPrevention.sendMessage(player, TextMode.Success, Messages.DropUnlockConfirmation);
        }

        playerData.setDropsAreUnlocked(true);

        return true;
    }
}
