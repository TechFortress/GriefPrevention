package me.ryanhamshire.GriefPrevention.events;

import me.ryanhamshire.GriefPrevention.Claim;
import org.jetbrains.annotations.NotNull;

/**
 * An {@link org.bukkit.event.Event Event} for when a {@link Claim Claim's} depth (lower Y bound) is to be extended.
 *
 * <p>Note that changes to the {@link #getTo() new claim} other than {@link #setNewDepth(int) setting new depth} will
 * not be respected.
 *
 * @author FrankHeijden
 */
public class ClaimExtendEvent extends ClaimChangeEvent
{

    /**
     * Construct a new {@code ClaimExtendEvent}.
     *
     * @param claim the {@link Claim} extending downwards
     * @param newDepth the new depth of the {@code Claim}
     */
    public ClaimExtendEvent(@NotNull Claim claim, int newDepth)
    {
        super(claim, new Claim(claim));
        this.getTo().getLesserBoundaryCorner().setY(newDepth);
    }

    /**
     * Get the resulting {@link Claim} after modification.
     *
     * @return the resulting {@code Claim}
     * @deprecated Use {@link #getTo() getTo} instead.
     */
    @Deprecated
    public @NotNull Claim getClaim()
    {
        return getTo();
    }

    /**
     * Get the new lowest depth that the {@link Claim} will encompass in the Y axis.
     *
     * @return the new depth
     */
    public int getNewDepth()
    {
        return getTo().getLesserBoundaryCorner().getBlockY();
    }

    /**
     * Set the new lowest depth that the {@link Claim} will encompass in the Y axis.
     *
     * <p>Note that this value is not necessarily final - it will be modified to respect configuration and world limits.
     *
     * @param newDepth the new depth
     */
    public void setNewDepth(int newDepth) {
        getTo().getLesserBoundaryCorner().setY(newDepth);
    }

}
