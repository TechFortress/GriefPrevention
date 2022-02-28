package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.FluidCollisionMode;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class GPBlockInfoCommand implements CommandExecutor
{
    // reference to plugin main class
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final GriefPrevention plugin;

    /**
     * Class constructor
     *
     * @param plugin reference to main class
     */
    public GPBlockInfoCommand(final GriefPrevention plugin)
    {
        // set reference to main class
        this.plugin = plugin;

        // register this class as command executor
        Objects.requireNonNull(plugin.getCommand("gpblockinfo")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args)
    {
        // command sender must be in game player
        if (!(sender instanceof Player player)) return false;

        ItemStack inHand = player.getInventory().getItemInMainHand();
        player.sendMessage("In Hand: " + inHand.getType().name());

        Block inWorld = player.getTargetBlockExact(300, FluidCollisionMode.ALWAYS);
        if (inWorld == null) inWorld = player.getEyeLocation().getBlock();
        player.sendMessage("In World: " + inWorld.getType().name());

        return true;
    }
}
