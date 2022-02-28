package me.ryanhamshire.GriefPrevention;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A class that instantiates all commands and provides convenience methods for
 * matching various arguments as they are being typed for tab completion
 */
public final class CommandHandler
{
    // reference to plugin main class
    GriefPrevention plugin;

    /**
     * Class constructor
     * @param plugin reference to the plugin main class
     */
    CommandHandler(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // player commands
//        new AbandonAllClaimsCommand(plugin);
//        new AbandonClaimCommand(plugin);
//        new AbandonTopLevelClaimCommand(plugin);
//
//        new ClaimCommand(plugin);
//        new ClaimsListCommand(plugin);
//        new DeleteClaimCommand(plugin);
//        new ExtendClaimCommand(plugin);
//        new ClaimExplosionsCommand(plugin);
//
//        new GivePetCommand(plugin);
//        new SiegeCommand(plugin);
//        new TrappedCommand(plugin);
//
//        new SubdivideClaimsCommand(plugin);
//        new RestrictSubclaimCommand(plugin);
//
//        new TrustCommand(plugin);
//        new AccessTrustCommand(plugin);
//        new ContainerTrustCommand(plugin);
//        new PermissionTrustCommand(plugin);
//        new UntrustCommand(plugin);
//        new TrustListCommand(plugin);
//
//        new IgnorePlayerCommand(plugin);
//        new UnignorePlayerCommand(plugin);
//        new IgnoredPlayerListCommand(plugin);
//
//        new BuyClaimBlocksCommand(plugin);
//        new SellClaimBlocksCommand(plugin);
//
//        new DeleteAllClaimsCommand(plugin);
//        new DeleteClaimsInWorldCommand(plugin);
//        new DeleteUserClaimsInWorldCommand(plugin);
//        new DeleteAllAdminClaimsCommand(plugin);
//
//        // admin commands
//        new AdminClaimsCommand(plugin);
//        new BasicClaimsCommand(plugin);
//        new IgnoreClaimsCommand(plugin);
//
//        new ClaimBookCommand(plugin);
//        new AdminClaimListCommand(plugin);
//        new SoftMuteCommand(plugin);
//        new GPReloadCommand(plugin);
//        new GPBlockInfoCommand(plugin);
//
//        new RestoreNatureCommand(plugin);
//        new RestoreNatureAggressiveCommand(plugin);
//        new RestoreNatureFillCommand(plugin);
//
//        new AdjustBonusClaimBlocksCommand(plugin);
//        new AdjustBonusClaimBlocksAllCommand(plugin);
//        new SetAccruedClaimBlocksCommand(plugin);
//
//        new SeparateCommand(plugin);
//        new UnseparateCommand(plugin);
//
//        new TransferClaimCommand(plugin);
//        new UnlockDropsCommand(plugin);
    }

    /**
     * Getter for configuration variable to enable/disable tab completion in commands
     * @return true if tab completion is enable, false if it is disabled
     */
    public boolean tabCompletionEnabled() {
        //TODO: this will reference a configuration variable, once that variable has been defined in main class
        return true;
    }

    /**
     * Match online player names with prefix. Overloaded method to allow omitting {@code excludeSelf} parameter (defaults true).
     * @param sender the command sender
     * @param prefix the player name being typed
     * @return List of String of matching offline player names
     */
    public List<String> matchOnlinePlayers(final CommandSender sender, final String prefix)
    {
        return matchOnlinePlayers(sender, prefix, true);
    }

    /**
     * Match online player names with prefix
     * @param sender the command sender
     * @param prefix the player name being typed
     * @param excludeSelf should sender be removed from the returned list of names
     * @return List of String of matching offline player names
     */
    public List<String> matchOnlinePlayers(final CommandSender sender, final String prefix, final boolean excludeSelf)
    {
        List<String> results = new LinkedList<>();
        for (Player otherPlayer : plugin.getServer().getOnlinePlayers())
        {
            // if other player name matches prefix, add name to results list
            if (otherPlayer.getName().startsWith(prefix))
            {
                results.add(otherPlayer.getName());
            }
            // if command sender is in-game player, remove name from results list
            if (excludeSelf && sender instanceof Player player)
            {
                results.remove(player.getName());
            }
        }
        return results;
    }

    /**
     * Get matching players that are ignored by a specific player
     * @param sender the command sender and player whose list of ignored players is being matched
     * @param prefix the player name being typed
     * @return List of String - names of players ignored by sender
     */
    public List<String> matchIgnoredPlayers(final CommandSender sender, final String prefix)
    {
        if (sender instanceof Player player)
        {
            List<String> results = new LinkedList<>();
            PlayerData playerData = plugin.dataStore.getPlayerData(player.getUniqueId());

            for (Map.Entry<UUID, Boolean> entry : playerData.ignoredPlayers.entrySet())
            {
                if (entry.getValue() != null && entry.getValue().equals(true))
                {
                    String ignoredName = plugin.getServer().getOfflinePlayer(entry.getKey()).getName();
                    if (ignoredName != null && ignoredName.startsWith(prefix))
                    {
                        results.add(ignoredName);
                    }
                }
            }
            return results;
        }
        return Collections.emptyList();
    }

    /**
     * Get matching world names
     * @param prefix the world name being typed
     * @return List of String - world names that match
     */
    public List<String> matchWorldNames(String prefix)
    {
        // match first argument to all server world names
        List<String> returnList = new LinkedList<>();
        for (World world : plugin.getServer().getWorlds())
        {
            if (world.getName().startsWith(prefix))
            {
                returnList.add(world.getName());
            }
        }
        return returnList;
    }

}
