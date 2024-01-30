package me.ryanhamshire.GriefPrevention;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSignOpenEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerSignOpenHandler implements Listener
{

    private final GriefPrevention instance;

    PlayerSignOpenHandler(@NotNull GriefPrevention plugin)
    {
        this.instance = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    void onPlayerSignOpen(@NotNull PlayerSignOpenEvent event)
    {
        if (event.getCause() != PlayerSignOpenEvent.Cause.INTERACT || event.getSign().getBlock().getType() != event.getSign().getType())
        {
            // If the sign is not opened by interaction or the corresponding block is no longer a sign,
            // it is either the initial sign placement or another plugin is at work. Do not interfere.
            return;
        }

        Player player = event.getPlayer();
        String denial = instance.allowBuild(player, event.getSign().getLocation(), event.getSign().getType());

        // If user is allowed to build, do nothing.
        if (denial == null)
            return;

        // If user is not allowed to build, prevent sign UI opening and send message.
        GriefPrevention.sendMessage(player, TextMode.Err, denial);
        event.setCancelled(true);
    }

}
