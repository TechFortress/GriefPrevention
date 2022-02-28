package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.TextMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

public final class ClaimExplosionsCommand implements CommandExecutor
{
    // reference to plugin main class
    private final GriefPrevention plugin;

    /**
     * Class constructor
     * @param plugin reference to main class
     */
    public ClaimExplosionsCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("claimexplosions")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args)
    {
        // command sender must be in game player
        if (!(sender instanceof Player player)) return false;

        //determine which claim the player is standing in
        Claim claim = plugin.dataStore.getClaimAt(player.getLocation(), true /*ignore height*/, null);

        if (claim == null)
        {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.DeleteClaimMissing);
        }
        else
        {
            Supplier<String> noBuildReason = claim.checkPermission(player, ClaimPermission.Build, null);
            if (noBuildReason != null)
            {
                GriefPrevention.sendMessage(player, TextMode.Err, noBuildReason.get());
                return true;
            }

            if (claim.areExplosivesAllowed)
            {
                claim.areExplosivesAllowed = false;
                GriefPrevention.sendMessage(player, TextMode.Success, Messages.ExplosivesDisabled);
            }
            else
            {
                claim.areExplosivesAllowed = true;
                GriefPrevention.sendMessage(player, TextMode.Success, Messages.ExplosivesEnabled);
            }
        }

        return true;
    }
}
