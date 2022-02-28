package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.CustomLogEntryTypes;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.TextMode;
import me.ryanhamshire.GriefPrevention.Visualization;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class DeleteAllAdminClaimsCommand implements CommandExecutor
{
    // reference to plugin main class
    private final GriefPrevention plugin;

    /**
     * Class constructor
     * @param plugin reference to main class
     */
    public DeleteAllAdminClaimsCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("deletealladminclaims")).setExecutor(this);
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

        if (!sender.hasPermission("griefprevention.deleteclaims"))
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.NoDeletePermission);
            return true;
        }

        //delete all admin claims
        plugin.dataStore.deleteClaimsForPlayer(null, true);  //null for owner id indicates an administrative claim

        GriefPrevention.sendMessage(player, TextMode.Success, Messages.AllAdminDeleted);
        if (player != null)
        {
            GriefPrevention.AddLogEntry(player.getName() + " deleted all administrative claims.", CustomLogEntryTypes.AdminActivity);

            //revert any current visualization
            Visualization.Revert(player);
        }

        return true;
    }
}
