package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.CustomLogEntryTypes;
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
import java.util.UUID;

public final class AdjustBonusClaimBlocksAllCommand implements CommandExecutor
{
    // reference to plugin main class
    private final GriefPrevention plugin;

    /**
     * Class constructor
     * @param plugin reference to main class
     */
    public AdjustBonusClaimBlocksAllCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("adjustbonusclaimblocksall")).setExecutor(this);
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

        // requires exactly one parameter, the amount of adjustment
        if (args.length != 1) return false;

        // parse the adjustment amount
        int adjustment;
        try
        {
            adjustment = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException numberFormatException)
        {
            return false;  //causes usage to be displayed
        }

        //for each online player
        StringBuilder builder = new StringBuilder();
        int count = plugin.getServer().getOnlinePlayers().size();
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers())
        {
            UUID playerID = onlinePlayer.getUniqueId();
            PlayerData playerData = plugin.dataStore.getPlayerData(playerID);
            playerData.setBonusClaimBlocks(playerData.getBonusClaimBlocks() + adjustment);
            plugin.dataStore.savePlayerData(playerID, playerData);
            builder.append(onlinePlayer.getName()).append(' ');
        }

        GriefPrevention.sendMessage(player, TextMode.Success, Messages.AdjustBlocksAllSuccess, String.valueOf(adjustment));
        GriefPrevention.AddLogEntry("Adjusted all " + count + "players' bonus claim blocks by " + adjustment + ".  " + builder.toString(), CustomLogEntryTypes.AdminActivity);

        return true;
    }
}
