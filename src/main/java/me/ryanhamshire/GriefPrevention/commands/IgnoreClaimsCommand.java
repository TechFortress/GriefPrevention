package me.ryanhamshire.GriefPrevention.commands;

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

public final class IgnoreClaimsCommand implements CommandExecutor
{
    // reference to plugin main class
    private final GriefPrevention plugin;

    /**
     * Class constructor
     * @param plugin reference to main class
     */
    public IgnoreClaimsCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("ignoreclaims")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args)
    {
        // command sender must be in game player
        if (!(sender instanceof Player player)) return false;

        PlayerData playerData = plugin.dataStore.getPlayerData(player.getUniqueId());

        playerData.ignoreClaims = !playerData.ignoreClaims;

        //toggle ignore claims mode on or off
        if (!playerData.ignoreClaims)
        {
            GriefPrevention.sendMessage(player, TextMode.Success, Messages.RespectingClaims);
        }
        else
        {
            GriefPrevention.sendMessage(player, TextMode.Success, Messages.IgnoringClaims);
        }

        return true;
    }

}
