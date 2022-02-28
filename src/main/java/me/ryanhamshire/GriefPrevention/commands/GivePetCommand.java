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

public final class GivePetCommand implements CommandExecutor, TabCompleter
{
    // reference to plugin main class
    private final GriefPrevention plugin;

    /**
     * Class constructor
     * @param plugin reference to main class
     */
    public GivePetCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("givepet")).setExecutor(this);
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
        // command sender must be in game player
        if (!(sender instanceof Player player)) return false;

        //requires one parameter
        if (args.length < 1) return false;

        PlayerData playerData = plugin.dataStore.getPlayerData(player.getUniqueId());

        //special case: cancellation
        if (args[0].equalsIgnoreCase("cancel"))
        {
            playerData.setPetGiveawayRecipient(null);
            GriefPrevention.sendMessage(player, TextMode.Success, Messages.PetTransferCancellation);
            return true;
        }

        //find the specified player
        OfflinePlayer targetPlayer = plugin.resolvePlayerByName(args[0]);
        if (targetPlayer == null)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.PlayerNotFound2);
            return true;
        }

        //remember the player's ID for later pet transfer
        playerData.setPetGiveawayRecipient(targetPlayer);

        //send instructions
        GriefPrevention.sendMessage(player, TextMode.Instr, Messages.ReadyToTransferPet);

        return true;
    }
}
