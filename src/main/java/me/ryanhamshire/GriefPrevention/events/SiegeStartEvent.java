package me.ryanhamshire.GriefPrevention.events;

import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * An {@link org.bukkit.event.Event Event} called when player is about to siege another player in {@link Claim}.
 */
public class SiegeStartEvent extends ClaimEvent implements Cancellable
{
    private Entity attacker, defender;

    /**
     * Construct a new {@code SiegeStartEvent}.
     *
     * @param claim {@link Claim} the claim where siege occurs
     * @param attacker {@link Entity} the player about to siege
     * @param defender {@link Entity} the player about to sieged
     *
     */
    public SiegeStartEvent(@NotNull Claim claim, @NotNull Entity attacker, @NotNull Entity defender)
    {
        super(claim);
        this.attacker = attacker;
        this.defender = defender;
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

    public @NotNull Entity getAttacker() {
        return this.attacker;
    }

    public @NotNull Entity getDefender() {
        return this.defender;
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
