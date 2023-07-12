package com.conaxgames.claim;

import com.conaxgames.events.PlayerSwapClaimEvent;
import com.conaxgames.util.StuckUtil;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class SwapClaimListener implements Listener
{

    private final DataStore dataStore;

    public SwapClaimListener(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to != null)
        {
            // ensuring the player has actually moved a block.
            if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ())
            {

                Claim fromClaim = this.dataStore.getClaimAt(from, false, null);
                Claim toClaim = this.dataStore.getClaimAt(to, false, null);

                PlayerSwapClaimEvent playerSwapClaimEvent = new PlayerSwapClaimEvent(event.getPlayer(), fromClaim, toClaim);
                Bukkit.getPluginManager().callEvent(playerSwapClaimEvent);

                if (playerSwapClaimEvent.isCancelled())
                {
                    event.setTo(event.getFrom());
                    event.setCancelled(true);
                }

            }
        }
    }

    @EventHandler
    public void onPlayerSwapClaimEvent(PlayerSwapClaimEvent event)
    {
        Claim to = event.getTo();
        Player player = event.getPlayer();

        if (to != null) {
            if (GriefPrevention.instance.conaxGP.claimBans.isBlocked(to.getOwnerID(), player.getUniqueId()))
            {
                if (!player.hasPermission("griefprevention.bypass"))
                {
                    player.sendMessage(ChatColor.RED + "You are not permitted to enter this claim.");
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerTeleportEvent(PlayerTeleportEvent event)
    {
        Location to = event.getTo();
        Claim toClaim = this.dataStore.getClaimAt(to, false, null);
        Player player = event.getPlayer();

        if (toClaim != null) {
            if (GriefPrevention.instance.conaxGP.claimBans.isBlocked(toClaim.getOwnerID(), player.getUniqueId()))
            {
                if (!player.hasPermission("griefprevention.bypass"))
                {
                    player.sendMessage(ChatColor.RED + "You are not permitted to teleport into this claim.");
                    event.setTo(event.getFrom());
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Location to = event.getPlayer().getLocation();
        Claim toClaim = this.dataStore.getClaimAt(to, false, null);
        Player player = event.getPlayer();

        if (toClaim != null) {
            if (GriefPrevention.instance.conaxGP.claimBans.isBlocked(toClaim.getOwnerID(), player.getUniqueId()))
            {
                if (!player.hasPermission("griefprevention.bypass"))
                {
                    player.sendMessage(ChatColor.RED + "You logged in while standing in a claim you're banned from! Teleporting you out...");
                    Location safe = StuckUtil.getSafeNearbyTeleportLocation(player);
                    if (safe != null)
                    {
                        player.teleport(safe, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    }
                }
            }
        }
    }


}
