package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.EconomyHandler;
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

public final class SellClaimBlocksCommand implements CommandExecutor
{
    // reference to plugin main class
    private final GriefPrevention plugin;

    /**
     * Class constructor
     * @param plugin reference to main class
     */
    public SellClaimBlocksCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("sellclaimblocks")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args)
    {
        // command sender must be in game player
        if (!(sender instanceof Player player)) return false;

        //if economy is disabled, don't do anything
        EconomyHandler.EconomyWrapper economyWrapper = plugin.economyHandler.getWrapper();
        if (economyWrapper == null)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.BuySellNotConfigured);
            return true;
        }

        if (!player.hasPermission("griefprevention.buysellclaimblocks"))
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.NoPermissionForCommand);
            return true;
        }

        //if disabled, error message
        if (GriefPrevention.instance.config_economy_claimBlocksSellValue == 0)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.OnlyPurchaseBlocks);
            return true;
        }

        //load player data
        PlayerData playerData = plugin.dataStore.getPlayerData(player.getUniqueId());
        int availableBlocks = playerData.getRemainingClaimBlocks();

        //if no amount provided, just tell player value per block sold, and how many he can sell
        if (args.length != 1)
        {
            GriefPrevention.sendMessage(player, TextMode.Info, Messages.BlockSaleValue, String.valueOf(GriefPrevention.instance.config_economy_claimBlocksSellValue), String.valueOf(availableBlocks));
            return false;
        }

        //parse number of blocks
        int blockCount;
        try
        {
            blockCount = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException numberFormatException)
        {
            return false;  //causes usage to be displayed
        }

        if (blockCount <= 0)
        {
            return false;
        }

        //if he doesn't have enough blocks, tell him so
        if (blockCount > availableBlocks)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.NotEnoughBlocksForSale);
        }

        //otherwise carry out the transaction
        else
        {
            //compute value and deposit it
            double totalValue = blockCount * GriefPrevention.instance.config_economy_claimBlocksSellValue;
            economyWrapper.getEconomy().depositPlayer(player, totalValue);

            //subtract blocks
            playerData.setBonusClaimBlocks(playerData.getBonusClaimBlocks() - blockCount);
            plugin.dataStore.savePlayerData(player.getUniqueId(), playerData);

            //inform player
            GriefPrevention.sendMessage(player, TextMode.Success, Messages.BlockSaleConfirmation, String.valueOf(totalValue), String.valueOf(playerData.getRemainingClaimBlocks()));
        }

        return true;
    }
}
