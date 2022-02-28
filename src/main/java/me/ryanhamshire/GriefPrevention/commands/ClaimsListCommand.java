package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.Claim;
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
import java.util.Vector;

public final class ClaimsListCommand implements CommandExecutor, TabCompleter
{
    // reference to plugin main class
    private final GriefPrevention plugin;

    /**
     * Class constructor
     * @param plugin reference to main class
     */
    public ClaimsListCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("claimslist")).setExecutor(this);
    }

    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args)
    {
        if (args.length == 1)
        {
            // match first argument with other online players
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

        //at most one parameter
        if (args.length > 1) return false;

        //player whose claims will be listed
        OfflinePlayer otherPlayer;

        //if another player isn't specified, assume current player
        if (args.length < 1)
        {
            if (player != null)
                otherPlayer = player;
            else
                return false;
        }

        //otherwise if no permission to delve into another player's claims data
        else if (player != null && !player.hasPermission("griefprevention.claimslistother"))
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.ClaimsListNoPermission);
            return true;
        }

        //otherwise try to find the specified player
        else
        {
            otherPlayer = plugin.resolvePlayerByName(args[0]);
            if (otherPlayer == null)
            {
                GriefPrevention.sendMessage(player, TextMode.Err, Messages.PlayerNotFound2);
                return true;
            }
        }

        //load the target player's data
        PlayerData playerData = plugin.dataStore.getPlayerData(otherPlayer.getUniqueId());
        Vector<Claim> claims = playerData.getClaims();
        GriefPrevention.sendMessage(player, TextMode.Instr, Messages.StartBlockMath,
                String.valueOf(playerData.getAccruedClaimBlocks()),
                String.valueOf((playerData.getBonusClaimBlocks() + plugin.dataStore.getGroupBonusBlocks(otherPlayer.getUniqueId()))),
                String.valueOf((playerData.getAccruedClaimBlocks() + playerData.getBonusClaimBlocks() + plugin.dataStore.getGroupBonusBlocks(otherPlayer.getUniqueId()))));
        if (claims.size() > 0)
        {
            GriefPrevention.sendMessage(player, TextMode.Instr, Messages.ClaimsListHeader);
            for (int i = 0; i < playerData.getClaims().size(); i++)
            {
                Claim claim = playerData.getClaims().get(i);
                GriefPrevention.sendMessage(player, TextMode.Instr, GriefPrevention.getfriendlyLocationString(claim.getLesserBoundaryCorner()) + plugin.dataStore.getMessage(Messages.ContinueBlockMath, String.valueOf(claim.getArea())));
            }

            GriefPrevention.sendMessage(player, TextMode.Instr, Messages.EndBlockMath, String.valueOf(playerData.getRemainingClaimBlocks()));
        }

        //drop the data we just loaded, if the player isn't online
        if (!otherPlayer.isOnline())
            plugin.dataStore.clearCachedPlayerData(otherPlayer.getUniqueId());

        return true;
    }
}
