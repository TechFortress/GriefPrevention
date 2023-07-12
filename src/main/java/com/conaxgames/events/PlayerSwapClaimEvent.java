package com.conaxgames.events;

import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * An {@link org.bukkit.event.Event Event} called when a {@link Player} moves between a claimed area.
 * The objects from and to can be null, for example when a player is moving from a claim to the "wilderness",
 * the 'to' object will be null.
 *
 * @author BOWP on 18/04/2022
 */
public class PlayerSwapClaimEvent extends PlayerEvent implements Cancellable
{

    private final Claim from;
    private final Claim to;

    // Cancellable requirements
    private boolean cancelled = false;

    /**
     * Construct a new {@code PlayerSwapClaimEvent}.
     * @param player the affected {@link Player}
     * @param from the claim the player left, can be null.
     * @param to the claim the player entered, can be null.
     */
    public PlayerSwapClaimEvent(@NotNull Player player, Claim from, Claim to)
    {
        super(player);
        this.from = from;
        this.to = to;
    }

    /**
     * Get the claim {@link Player} the player has just left.
     *
     * @return the claim the player left.
     */
    public Claim getFrom()
    {
        return this.from;
    }

    /**
     * Get the claim {@link Player} the player has just entered.
     *
     * @return the claim the player entered.
     */
    public Claim getTo()
    {
        return this.to;
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
