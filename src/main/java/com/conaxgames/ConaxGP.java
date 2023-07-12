package com.conaxgames;

import com.conaxgames.claim.ClaimEntryProvider;
import com.conaxgames.claim.SwapClaimListener;
import com.conaxgames.libraries.message.FormatUtil;
import com.conaxgames.libraries.util.CC;
import com.conaxgames.util.StuckUtil;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;
import java.util.UUID;

public class ConaxGP
{

    public ClaimEntryProvider claimBans;
    public GriefPrevention gp;
    public DataStore ds;

    public ConaxGP()
    {
        this.claimBans = new ClaimEntryProvider();
        this.gp = GriefPrevention.instance;
        this.ds = gp.dataStore;
        Bukkit.getServer().getPluginManager().registerEvents(new SwapClaimListener(ds), gp);
    }

    public boolean claimban(CommandSender sender, String target) {
        if (sender instanceof Player senderPlayer) {
            // requires a player name
            if (target == null || target.isEmpty()) return false;

            //validate target players
            OfflinePlayer targetPlayer = gp.resolvePlayerByName(target);
            if (targetPlayer == null) {
                GriefPrevention.sendMessage(senderPlayer, ChatColor.RED, Messages.PlayerNotFound2);
                return true;
            }

            if (targetPlayer.getUniqueId().equals(senderPlayer.getUniqueId())) {
                sender.sendMessage(CC.RED + "You can't claim ban yourself!");
                return true;
            }

            List<String> bans = this.claimBans.list(senderPlayer.getUniqueId());
            if (bans.contains(targetPlayer.getUniqueId().toString())) {
                sender.sendMessage(CC.RED + "You've already claim banned " + targetPlayer.getName());
                return true;
            }

            Player onlineTarget = Bukkit.getPlayer(targetPlayer.getUniqueId());
            if (onlineTarget != null) {
                Location targetLocation = onlineTarget.getLocation();

                Claim claim = this.ds.getClaimAt(targetLocation, false, null);
                if (claim != null) {
                    if (claim.getOwnerID().equals(senderPlayer.getUniqueId())) {
                        Location outOfClaim = StuckUtil.getSafeNearbyTeleportLocation(onlineTarget);
                        if (outOfClaim != null) {
                            onlineTarget.teleport(outOfClaim, PlayerTeleportEvent.TeleportCause.PLUGIN);
                            onlineTarget.sendMessage(CC.RED + "You have been banned from " +
                                    FormatUtil.possessiveString(senderPlayer.getName()) + " claims!");
                        }
                    }
                }
            }

            sender.sendMessage(CC.GREEN + "You've claim banned " + targetPlayer.getName() + "!");
            this.claimBans.block(senderPlayer.getUniqueId(), targetPlayer.getUniqueId());
            return true;
        }
        else
        {
            sender.sendMessage("Player only command!");
        }
        return false;
    }

    public boolean claimunban(CommandSender sender, String target) {
        if (sender instanceof Player senderPlayer) {
            // requires a player name
            if (target == null || target.isEmpty()) return false;

            //validate target players
            OfflinePlayer targetPlayer = gp.resolvePlayerByName(target);
            if (targetPlayer == null)
            {
                GriefPrevention.sendMessage(senderPlayer, ChatColor.RED, Messages.PlayerNotFound2);
                return true;
            }

            if (targetPlayer.getUniqueId().equals(senderPlayer.getUniqueId())) {
                sender.sendMessage(CC.RED + "You can't claim unban yourself!");
                return true;
            }

            List<String> bans = this.claimBans.list(senderPlayer.getUniqueId());
            if (!bans.contains(targetPlayer.getUniqueId().toString())) {
                sender.sendMessage(CC.RED + targetPlayer.getName() + " isn't currently claim banned!");
                return true;
            }

            sender.sendMessage(CC.GREEN + "You've claim un-banned " + targetPlayer.getName());
            this.claimBans.unblock(senderPlayer.getUniqueId(), targetPlayer.getUniqueId());
            return true;
        } else {
            sender.sendMessage("Player only command!");
        }
        return false;
    }

    public boolean claimbanlist(CommandSender sender) {
        if (sender instanceof Player senderPlayer) {
            List<String> bans = this.claimBans.list(senderPlayer.getUniqueId());
            if (bans != null) {
                if (bans.isEmpty())
                {
                    sender.sendMessage(CC.YELLOW + "You don't have anybody claim banned!");
                    return true;
                }

                sender.sendMessage(ChatColor.RED + "Claim Bans:");
                bans.forEach(ban -> {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(ban));
                    sender.sendMessage(CC.YELLOW + "- " + CC.RED + offlinePlayer.getName());
                });
            } else {
                sender.sendMessage(CC.YELLOW + "You don't have anybody claim banned!");
            }
            return true;
        } else {
            sender.sendMessage("Player only command!");
        }
        return false;
    }

}
