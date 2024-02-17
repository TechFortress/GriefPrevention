package com.griefprevention.module;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Configuration for a {@link Module}.
 */
public class ModuleConfig
{

    protected boolean enabled = false;
    protected boolean debug = false;

    /**
     * Constructor for a new module configuration.
     */
    protected ModuleConfig()
    {
    }

    /**
     * Load user configuration and write a sanitized version.
     *
     * @param in the user configuration
     * @param out the sanitized configuration
     */
    protected void load(@NotNull ConfigurationSection in, @NotNull ConfigurationSection out)
    {
        // TODO may want utility for this to reduce config work.
        enabled = in.getBoolean("enabled", true);
        out.set("enabled", enabled);
        out.setComments("enabled", List.of("Enable module"));

        debug = in.getBoolean("debug", false);
        out.set("debug", debug);
        out.setComments("debug", List.of("Enable debug logging"));
    }

    /**
     * Get whether the module is enabled.
     *
     * @return true if the module is enabled
     */
    public boolean enabled()
    {
        return enabled;
    }

    /**
     * Get whether the module should do debug logging.
     *
     * @return true if the module should do debug logging
     */
    public final boolean debug()
    {
        return debug;
    }

}
