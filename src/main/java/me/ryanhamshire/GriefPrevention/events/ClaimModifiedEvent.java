package me.ryanhamshire.GriefPrevention.events;


import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @deprecated replaced by more descriptive {@link ClaimResizeEvent} and generic {@link ClaimChangeEvent}
 *
 * An {@link Event} for when a {@link Claim} is changed.
 *
 * <p>If cancelled, the resulting changes will not be made.
 *
 * @author Narimm on 5/08/2018.
 */
@Deprecated
public class ClaimModifiedEvent extends ClaimChangeEvent
{

    private final @Nullable CommandSender modifier;

    /**
     * Construct a new {@code ClaimModifiedEvent}.
     *
     * <p>Note that the actor causing modification may not be present if done by plugins.
     *
     * @param from the unmodified {@link Claim}
     * @param to the resulting {@code Claim}
     * @param modifier the {@link CommandSender} causing modification
     */
    public ClaimModifiedEvent(@NotNull Claim from, @NotNull Claim to, @Nullable CommandSender modifier)
    {
        super(from, to);
        this.modifier = modifier;
    }

    /**
     * Get the resulting {@link Claim} after modification.
     *
     * @return the resulting {@code Claim}
     * @deprecated Use {@link #getTo()} instead.
     */
    @Deprecated
    public @NotNull Claim getClaim()
    {
        return getTo();
    }

    /**
     * Get the {@link CommandSender} modifying the {@link Claim}. May be {@code null} if caused by a plugin.
     *
     * @return the actor causing creation
     */
    public @Nullable CommandSender getModifier()
    {
        return modifier;
    }
}
