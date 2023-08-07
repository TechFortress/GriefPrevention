/*
    GriefPrevention Server Plugin for Minecraft
    Copyright (C) 2012 Ryan Hamshire

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.ryanhamshire.GriefPrevention;

import com.griefprevention.util.persistence.DataKeys;
import com.griefprevention.util.persistence.DataTypes;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

//players can be "trapped" in a portal frame if they don't have permission to break
//solid blocks blocking them from exiting the frame
//if that happens, we detect the problem and send them back through the portal.
class CheckForPortalTrapTask extends BukkitRunnable
{
    GriefPrevention instance;
    //player who recently teleported via nether portal
    private final Player player;

    //where to send the player back to if he hasn't left the portal frame
    private final Location returnLocation;

    public CheckForPortalTrapTask(
            @NotNull Player player,
            @NotNull GriefPrevention plugin,
            @NotNull Location locationToReturn)
    {
        this.player = player;
        this.instance = plugin;
        this.returnLocation = locationToReturn;
        player.getPersistentDataContainer().set(DataKeys.PORTAL_TRAP, DataTypes.LOCATION, locationToReturn);
    }

    @Override
    public void run()
    {
        if (!player.isOnline() || player.getPortalCooldown() < 1)
        {
            return;
        }
        PersistentDataContainer container = player.getPersistentDataContainer();
        if (container.has(DataKeys.PORTAL_TRAP, DataTypes.LOCATION))
        {
            GriefPrevention.AddLogEntry("Rescued " + player.getName() + " from a nether portal.\nTeleported from " + player.getLocation() + " to " + returnLocation, CustomLogEntryTypes.Debug);
            player.teleport(returnLocation);
            container.remove(DataKeys.PORTAL_TRAP);
        }
        instance.portalReturnTaskMap.remove(player.getUniqueId());
    }
}
