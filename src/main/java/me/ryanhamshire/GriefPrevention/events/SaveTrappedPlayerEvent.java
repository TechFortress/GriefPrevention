package me.ryanhamshire.GriefPrevention.events;

import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

//if destination field is set, then GriefPrevention will send the player to that location instead of searching for one
public class SaveTrappedPlayerEvent extends ClaimEvent implements Cancellable
{

    private Location destination = null;

    public SaveTrappedPlayerEvent(Claim claim)
    {
        super(claim);
    }

    public Location getDestination()
    {
        return destination;
    }

    public void setDestination(Location destination)
    {
        this.destination = destination;
    }

    // Listenable event requirements
    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return HANDLERS;
    }

    // Cancellable requirements
    private boolean cancelled = false;

    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }

}