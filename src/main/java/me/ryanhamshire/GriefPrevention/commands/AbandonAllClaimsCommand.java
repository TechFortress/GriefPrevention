package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.PlayerData;
import me.ryanhamshire.GriefPrevention.TextMode;
import me.ryanhamshire.GriefPrevention.Visualization;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class AbandonAllClaimsCommand implements CommandExecutor
{
    // reference to plugin main class
    private final GriefPrevention plugin;

    /**
     * Class constructor
     * @param plugin reference to main class
     */
    public AbandonAllClaimsCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("abandonallclaims")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String label, final String[] args)
    {
        // command sender must be in game player
        if (!(sender instanceof Player player)) return false;

        if (args.length > 1) return false;

        if (args.length != 1 || !"confirm".equalsIgnoreCase(args[0]))
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.ConfirmAbandonAllClaims);
            return true;
        }

        //count claims
        PlayerData playerData = plugin.dataStore.getPlayerData(player.getUniqueId());
        int originalClaimCount = playerData.getClaims().size();

        //check count
        if (originalClaimCount == 0)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.YouHaveNoClaims);
            return true;
        }

        if (plugin.config_claims_abandonReturnRatio != 1.0D)
        {
            //adjust claim blocks
            for (Claim claim : playerData.getClaims())
            {
                playerData.setAccruedClaimBlocks(playerData.getAccruedClaimBlocks() - (int) Math.ceil((claim.getArea() * (1 - plugin.config_claims_abandonReturnRatio))));
            }
        }


        //delete them
        plugin.dataStore.deleteClaimsForPlayer(player.getUniqueId(), false);

        //inform the player
        int remainingBlocks = playerData.getRemainingClaimBlocks();
        GriefPrevention.sendMessage(player, TextMode.Success, Messages.SuccessfulAbandon, String.valueOf(remainingBlocks));

        //revert any current visualization
        Visualization.Revert(player);

        return true;
    }
}
