package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.EconomyHandler;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.PlayerData;
import me.ryanhamshire.GriefPrevention.TextMode;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class BuyClaimBlocksCommand implements CommandExecutor
{
    // reference to plugin main class
    private final GriefPrevention plugin;

    /**
     * Class constructor
     * @param plugin reference to main class
     */
    public BuyClaimBlocksCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("buyclaimblocks")).setExecutor(this);
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

        //if purchase disabled, send error message
        if (GriefPrevention.instance.config_economy_claimBlocksPurchaseCost == 0)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.OnlySellBlocks);
            return true;
        }

        Economy economy = economyWrapper.getEconomy();

        //if no parameter, just tell player cost per block and balance
        if (args.length != 1)
        {
            GriefPrevention.sendMessage(player, TextMode.Info, Messages.BlockPurchaseCost, String.valueOf(GriefPrevention.instance.config_economy_claimBlocksPurchaseCost), String.valueOf(economy.getBalance(player)));
            return false;
        }
        else
        {
            PlayerData playerData = plugin.dataStore.getPlayerData(player.getUniqueId());

            //try to parse number of blocks
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

            //if the player can't afford his purchase, send error message
            double balance = economy.getBalance(player);
            double totalCost = blockCount * GriefPrevention.instance.config_economy_claimBlocksPurchaseCost;
            if (totalCost > balance)
            {
                GriefPrevention.sendMessage(player, TextMode.Err, Messages.InsufficientFunds, String.valueOf(totalCost), String.valueOf(balance));
            }

            //otherwise carry out transaction
            else
            {
                int newBonusClaimBlocks = playerData.getBonusClaimBlocks() + blockCount;

                //if the player is going to reach max bonus limit, send error message
                int bonusBlocksLimit = GriefPrevention.instance.config_economy_claimBlocksMaxBonus;
                if (bonusBlocksLimit != 0 && newBonusClaimBlocks > bonusBlocksLimit)
                {
                    GriefPrevention.sendMessage(player, TextMode.Err, Messages.MaxBonusReached, String.valueOf(blockCount), String.valueOf(bonusBlocksLimit));
                    return true;
                }

                //withdraw cost
                economy.withdrawPlayer(player, totalCost);

                //add blocks
                playerData.setBonusClaimBlocks(playerData.getBonusClaimBlocks() + blockCount);
                plugin.dataStore.savePlayerData(player.getUniqueId(), playerData);

                //inform player
                GriefPrevention.sendMessage(player, TextMode.Success, Messages.PurchaseConfirmation, String.valueOf(totalCost), String.valueOf(playerData.getRemainingClaimBlocks()));
            }

            return true;
        }
    }
}
