package com.griefprevention.events;

import com.griefprevention.module.Module;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * An {@link Event} called when a {@link Module} is enabled.
 */
public class ModuleEnableEvent extends Event
{
    private final @NotNull Module module;

    /**
     * Construct a new {@code ModuleEnableEvent}.
     *
     * @param module the module
     */
    public ModuleEnableEvent(@NotNull Module module)
    {
        this.module = module;
    }

    /**
     * Get the {@link Module}.
     *
     * @return the module
     */
    public @NotNull Module getModule()
    {
        return module;
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

}
