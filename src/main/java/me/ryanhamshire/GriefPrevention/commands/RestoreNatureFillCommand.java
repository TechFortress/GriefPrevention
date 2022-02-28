package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.PlayerData;
import me.ryanhamshire.GriefPrevention.ShovelMode;
import me.ryanhamshire.GriefPrevention.TextMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class RestoreNatureFillCommand implements CommandExecutor
{
    // reference to plugin main class
    private final GriefPrevention plugin;

    /**
     * Class constructor
     * @param plugin reference to main class
     */
    public RestoreNatureFillCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("restorenaturefill")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args)
    {
        // command sender must be in game player
        if (!(sender instanceof Player player)) return false;

        //change shovel mode
        PlayerData playerData = plugin.dataStore.getPlayerData(player.getUniqueId());
        playerData.shovelMode = ShovelMode.RestoreNatureFill;

        //set radius based on arguments
        playerData.setFillRadius(2);
        if (args.length > 0)
        {
            try
            {
                playerData.setFillRadius(Integer.parseInt(args[0]));
            }
            catch (Exception exception) {
                // failed to parse integer
            }
        }

        if (playerData.getFillRadius() < 0) playerData.setFillRadius(2);

        GriefPrevention.sendMessage(player, TextMode.Success, Messages.FillModeActive, String.valueOf(playerData.getFillRadius()));
        return true;
    }
}
