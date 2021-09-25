package me.ryanhamshire.GriefPrevention.events;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * An {@link Event} called when a {@link Claim} requires a specific level of trust.
 * If the denial reason is {@code null}, the trust requirements are met and the action is allowed.
 */
public class ClaimPermissionCheckEvent extends ClaimEvent implements MessageCancellable
{

    private final @Nullable Player checkedPlayer;
    private final @NotNull UUID checkedUUID;
    private final @NotNull ClaimPermission requiredPermission;
    private final @Nullable Event triggeringEvent;

    /**
     * Construct a new {@code ClaimPermissionCheckEvent}.
     *
     * @param checked the {@link Player} being checked for permissions
     * @param claim the {@link Claim} in which permissions are being checked
     * @param required the {@link ClaimPermission} level required
     * @param triggeringEvent the {@link Event} triggering the permission check
     */
    public ClaimPermissionCheckEvent(
            @NotNull Player checked,
            @NotNull Claim claim,
            @NotNull ClaimPermission required,
            @Nullable Event triggeringEvent)
    {
        this(checked, checked.getUniqueId(), claim, required, triggeringEvent);
    }

    /**
     * Construct a new {@code ClaimPermissionCheckEvent}.
     *
     * @param checked the {@link UUID} being checked for permissions
     * @param claim the {@link Claim} in which permissions are being checked
     * @param required the {@link ClaimPermission} level required
     * @param triggeringEvent the {@link Event} triggering the permission check
     */
    public ClaimPermissionCheckEvent(
            @NotNull UUID checked,
            @NotNull Claim claim,
            @NotNull ClaimPermission required,
            @Nullable Event triggeringEvent)
    {
        this(Bukkit.getPlayer(checked), checked, claim, required, triggeringEvent);
    }

    private ClaimPermissionCheckEvent(
            @Nullable Player checkedPlayer,
            @NotNull UUID checkedUUID,
            @NotNull Claim claim,
            @NotNull ClaimPermission required,
            @Nullable Event triggeringEvent)
    {
        super(claim);
        this.checkedPlayer = checkedPlayer;
        this.checkedUUID = checkedUUID;
        this.requiredPermission = required;
        this.triggeringEvent = triggeringEvent;
    }

    /**
     * Get the {@link Player} being checked for permission if online.
     *
     * @return the {@code Player} being checked or null if offline
     */
    public @Nullable Player getCheckedPlayer()
    {
        return checkedPlayer;
    }

    /**
     * Get the {@link UUID} being checked for permission.
     *
     * @return the {@code UUID} being checked for permission
     */
    public @NotNull UUID getCheckedUUID()
    {
        return checkedUUID;
    }

    /**
     * Get the {@link ClaimPermission} being checked for.
     *
     * @return the {@code ClaimPermission} being checked for
     */
    public @NotNull ClaimPermission getRequiredPermission()
    {
        return requiredPermission;
    }

    /**
     * Get the {@link Event} causing this {@code ClaimPermissionCheckEvent} to fire.
     *
     * @return the triggering {@code Event} or null if none was provided
     */
    public @Nullable Event getTriggeringEvent()
    {
        return triggeringEvent;
    }

    // MessageCancellable requirements
    private @Nullable Supplier<String> cancelReason = null;

    @Override
    public boolean isCancelled()
    {
        return cancelReason != null;
    }

    @Override
    @Deprecated
    public void setCancelled(boolean cancelled)
    {
        if (!cancelled)
        {
            this.cancelReason = null;
        }
        else
        {
            this.cancelReason = () -> "";
        }
    }

    @Override
    public @NotNull Optional<Supplier<String>> getCancelReason()
    {
        return Optional.ofNullable(this.cancelReason);
    }

    @Override
    public void setCancelled(@Nullable Supplier<String> reason)
    {
        this.cancelReason = reason;
    }

    /**
     * Get the reason the ClaimPermission check failed.
     * If the check did not fail, the message will be null.
     *
     * @return the denial reason or null if permission is granted
     * @see MessageCancellable#getCancelReason()
     * @see MessageCancellable#isCancelled()
     */
    public @Nullable Supplier<String> getDenialReason()
    {
        return cancelReason;
    }

    /**
     * Set the reason for denial.
     *
     * @param denial the denial reason
     * @see MessageCancellable#setCancelled(Supplier)
     */
    public void setDenialReason(@Nullable Supplier<String> denial)
    {
        this.cancelReason = denial;
    }

    // Listenable event requirements
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return handlers;
    }

}
