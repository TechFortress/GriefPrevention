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

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class IgnoredPlayerListCommand implements CommandExecutor
{
    // reference to plugin main class
    private final GriefPrevention plugin;

    /**
     * Class constructor
     * @param plugin reference to main class
     */
    public IgnoredPlayerListCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("ignoredplayerlist")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args)
    {
        // command sender must be in game player
        if (!(sender instanceof Player player)) return false;

        PlayerData playerData = plugin.dataStore.getPlayerData(player.getUniqueId());
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<UUID, Boolean> entry : playerData.ignoredPlayers.entrySet())
        {
            if (entry.getValue() != null)
            {
                //if not an admin ignore, add it to the list
                if (!entry.getValue())
                {
                    builder.append(GriefPrevention.lookupPlayerName(entry.getKey()));
                    builder.append(" ");
                }
            }
        }

        String list = builder.toString().trim();
        if (list.isEmpty())
        {
            GriefPrevention.sendMessage(player, TextMode.Info, Messages.NotIgnoringAnyone);
        }
        else
        {
            GriefPrevention.sendMessage(player, TextMode.Info, list);
        }

        return true;
    }
}
