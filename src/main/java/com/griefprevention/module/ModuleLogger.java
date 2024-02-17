package com.griefprevention.module;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Similar to a {@link org.bukkit.plugin.PluginLogger PluginLogger}, a small {@link Logger} extension that prefixes
 * lines with the module name.
 */
final class ModuleLogger extends Logger
{

    /**
     * Create a new ModuleLogger that extracts the name from a module.
     *
     * @param module the module
     */
    ModuleLogger(@NotNull Module module)
    {
        super(module.getPlugin().getName() + "/" + module.getName(), null);
        setParent(module.getPlugin().getLogger());
        setLevel(Level.ALL);
    }

    @Override
    public void log(@NotNull LogRecord logRecord)
    {
        super.log(logRecord);
    }

}
