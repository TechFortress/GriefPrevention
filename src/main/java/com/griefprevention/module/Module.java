package com.griefprevention.module;

import com.griefprevention.events.ModuleDisableEvent;
import com.griefprevention.events.ModuleEnableEvent;
import me.ryanhamshire.GriefPrevention.CustomLogEntryTypes;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * A configurable GriefPrevention module.
 */
public abstract class Module implements Listener
{

    private final @NotNull GriefPrevention plugin;
    private final @NotNull Logger logger;
    private final @NotNull ModuleConfig config;
    private boolean enabled = false;

    /**
     * Construct a new GriefPrevention module.
     *
     * @param plugin the {@link GriefPrevention} instance
     */
    protected Module(@NotNull GriefPrevention plugin)
    {
        this.plugin = plugin;
        this.logger = new ModuleLogger(this);
        this.config = createConfig();
    }

    /**
     * Get the name of the module.
     *
     * @return the module name
     */
    public abstract @NotNull String getName();

    /**
     * Enable the module if it is not enabled, logging if the state has changed.
     */
    public final void enable()
    {
        if (!enabled && getConfig().enabled())
        {
            onEnable();
            enabled = true;
            getLogger().info("Module enabled!");

            // Call event after finishing enable process so that it is set up for dependents.
            getPlugin().getServer().getPluginManager().callEvent(new ModuleEnableEvent(this));
        }
    }

    /**
     * Enable module functionality.
     */
    protected void onEnable()
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Disable the module if it is enabled, logging if the state has changed.
     */
    public final void disable()
    {
        if (enabled)
        {
            // Call event before starting disable process so any dependents may shut down first.
            getPlugin().getServer().getPluginManager().callEvent(new ModuleDisableEvent(this));

            onDisable();
            enabled = false;
            getLogger().info("Module disabled!");
        }
    }

    /**
     * Disable module functionality.
     */
    protected void onDisable()
    {
        HandlerList.unregisterAll(this);
    }

    /**
     * Get whether the module is enabled.
     *
     * @return true if the module is enabled
     */
    public final boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Instantiate a new copy of the module configuration.
     *
     * @return the new config instance
     */
    @Contract("-> new")
    protected abstract @NotNull ModuleConfig createConfig();

    /**
     * Get the configuration for the module.
     *
     * @return the module configuration
     */
    public @NotNull ModuleConfig getConfig()
    {
        return config;
    }

    /**
     * Get the GriefPrevention instance loading the module.
     *
     * @return the loading instance
     */
    protected @NotNull GriefPrevention getPlugin()
    {
        return plugin;
    }

    /**
     * Get a {@link Logger} that prefixes messages with the plugin and module name.
     *
     * @return the module logger
     */
    protected final @NotNull Logger getLogger()
    {
        return logger;
    }

    /**
     * Print a debugging line if debugging is enabled.
     *
     * @param message a {@link Supplier} for debug information
     */
    protected final void debug(Supplier<String> message)
    {
        if (getConfig().debug())
        {
            getLogger().info(message);
            GriefPrevention.AddLogEntry(message.get(), CustomLogEntryTypes.DebugModule, true);
        }
    }

}
