package me.ryanhamshire.GriefPrevention.events;

import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * An interface defining behavior expanding on Bukkit's default cancellation behavior to include a cancellation reason.
 *
 * <p>Quickly copiable implementation:
 * <!-- Note: IntelliJ quick render does not render "&commat;" as "@" so we use "&#64;" instead. -->
 * <pre>
 * // MessageCancellable requirements
 * private &#64;Nullable Supplier&lt;String&gt; cancelReason = null;
 *
 * &#64;Override
 * public boolean isCancelled()
 * {
 *     return cancelReason != null;
 * }
 *
 * &#64;Override
 * &#64;Deprecated
 * public void setCancelled(boolean cancelled)
 * {
 *     if (!cancelled)
 *     {
 *         this.cancelReason = null;
 *     }
 *     else
 *     {
 *         this.cancelReason = () -> "";
 *     }
 * }
 *
 * &#64;Override
 * public Optional&lt;Supplier&lt;String&gt;&gt; getCancelReason()
 * {
 *     return Optional.ofNullable(this.cancelReason);
 * }
 *
 * &#64;Override
 * public void setCancelled(&#64;Nullable Supplier&lt;String&gt; reason)
 * {
 *     this.cancelReason = reason;
 * }
 * </pre>
 */
public interface MessageCancellable extends Cancellable
{

    /**
     * @deprecated use {@link #setCancelled(Supplier)}
     * @param cancel whether the event is cancelled
     */
    @Deprecated
    void setCancelled(boolean cancel);

    /**
     * Get the cancellation reason.
     *
     * @return the cancellation reason
     */
    @NotNull Optional<Supplier<String>> getCancelReason();

    /**
     * Set the cancellation reason.
     *
     * <p>May be {@code null} to un-cancel the event.
     *
     * @param reason the reason for event cancellation
     */
    void setCancelled(@Nullable Supplier<String> reason);

}
