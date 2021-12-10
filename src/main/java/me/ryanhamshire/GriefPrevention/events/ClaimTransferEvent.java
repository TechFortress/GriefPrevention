package me.ryanhamshire.GriefPrevention.events;

import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * This Event is thrown when a claim is being transferred. If it is cancelled the claim will not be transferred.
 * <p>
 * Created by bertek41 on 30/10/2021.
 */

public class ClaimTransferEvent extends Event implements Cancellable
{

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    private final Claim claim;

    private UUID newOwner;

    private boolean cancelled = false;

    public ClaimTransferEvent(Claim claim, UUID newOwner) {
        this.claim = claim;
        this.newOwner = newOwner;
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

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

    /**
     * The Claim
     *
     * @return {@link Claim}
     */
    public Claim getClaim()
    {
        return claim;
    }

    /**
     * New owner of the claim
     *
     * @return the {@link java.util.UUID} of new owner or null
     */
    public UUID getNewOwner()
    {
        return newOwner;
    }

    /**
     * Sets the new owner of the claim
     *
     * @param {@link java.util.UUID} newOwner can be null
     */
    public void setNewOwner(UUID newOwner)
    {
        this.newOwner = newOwner;
    }
}
