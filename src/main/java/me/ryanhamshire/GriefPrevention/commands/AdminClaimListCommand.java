package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.TextMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Vector;

public final class AdminClaimListCommand implements CommandExecutor
{
    // reference to plugin main class
    private final GriefPrevention plugin;

    /**
     * Class constructor
     *
     * @param plugin reference to main class
     */
    public AdminClaimListCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("adminclaimslist")).setExecutor(this);
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

        //find admin claims
        Vector<Claim> claims = new Vector<>();
        for (Claim claim : plugin.dataStore.getClaims())
        {
            if (claim.ownerID == null)  //admin claim
            {
                claims.add(claim);
            }
        }
        if (claims.size() > 0)
        {
            GriefPrevention.sendMessage(player, TextMode.Instr, Messages.ClaimsListHeader);
            for (Claim claim : claims)
            {
                GriefPrevention.sendMessage(player, TextMode.Instr, GriefPrevention.getfriendlyLocationString(claim.getLesserBoundaryCorner()));
            }
        }

        return true;
    }
}
