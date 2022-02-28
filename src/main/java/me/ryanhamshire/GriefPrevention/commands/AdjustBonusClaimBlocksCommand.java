package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.CustomLogEntryTypes;
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
import java.util.UUID;

public final class AdjustBonusClaimBlocksCommand implements CommandExecutor, TabCompleter
{
    // reference to plugin main class
    private final GriefPrevention plugin;

    /**
     * Class constructor
     * @param plugin reference to main class
     */
    public AdjustBonusClaimBlocksCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("adjustbonusclaimblocks")).setExecutor(this);
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

        //requires exactly two parameters, the other player or group's name and the adjustment
        if (args.length != 2) return false;

        //parse the adjustment amount
        int adjustment;
        try
        {
            adjustment = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException numberFormatException)
        {
            return false;  //causes usage to be displayed
        }

        //if granting blocks to all players with a specific permission
        if (args[0].startsWith("[") && args[0].endsWith("]"))
        {
            String permissionIdentifier = args[0].substring(1, args[0].length() - 1);
            int newTotal = plugin.dataStore.adjustGroupBonusBlocks(permissionIdentifier, adjustment);

            GriefPrevention.sendMessage(player, TextMode.Success, Messages.AdjustGroupBlocksSuccess, permissionIdentifier, String.valueOf(adjustment), String.valueOf(newTotal));
            if (player != null)
                GriefPrevention.AddLogEntry(player.getName() + " adjusted " + permissionIdentifier + "'s bonus claim blocks by " + adjustment + ".");

            return true;
        }

        //otherwise, find the specified player
        OfflinePlayer targetPlayer;
        try
        {
            UUID playerID = UUID.fromString(args[0]);
            targetPlayer = plugin.getServer().getOfflinePlayer(playerID);

        }
        catch (IllegalArgumentException e)
        {
            targetPlayer = plugin.resolvePlayerByName(args[0]);
        }

        if (targetPlayer == null)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.PlayerNotFound2);
            return true;
        }

        //give blocks to player
        PlayerData playerData = plugin.dataStore.getPlayerData(targetPlayer.getUniqueId());
        playerData.setBonusClaimBlocks(playerData.getBonusClaimBlocks() + adjustment);
        plugin.dataStore.savePlayerData(targetPlayer.getUniqueId(), playerData);

        GriefPrevention.sendMessage(player, TextMode.Success, Messages.AdjustBlocksSuccess, targetPlayer.getName(), String.valueOf(adjustment), String.valueOf(playerData.getBonusClaimBlocks()));
        if (player != null)
            GriefPrevention.AddLogEntry(player.getName() + " adjusted " + targetPlayer.getName() + "'s bonus claim blocks by " + adjustment + ".", CustomLogEntryTypes.AdminActivity);

        return true;
    }
}
