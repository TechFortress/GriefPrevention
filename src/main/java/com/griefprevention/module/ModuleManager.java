package com.griefprevention.module;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;
import java.util.stream.Stream;

/**
 * A container for configurable behaviors for GriefPrevention.
 */
public final class ModuleManager
{

    private final @NotNull @Unmodifiable Set<Module> modules;

    /**
     * Construct a new {@code ModuleManager} instance.
     *
     * @param plugin the {@link GriefPrevention} plugin instance
     */
    public ModuleManager(@NotNull GriefPrevention plugin)
    {
        this.modules = Set.of(
                new WelcomeModule(plugin)
        );
    }

    /**
     * Load modules' configurations.
     *
     * @param in the user-editable configuration
     * @param out the sanitized configuration copy
     */
    public void loadConfig(@NotNull Configuration in, @NotNull Configuration out)
    {
        for (Module module : getModules())
        {
            // Use "modules" section for all modules.
            String path = "modules." + module.getName();
            ConfigurationSection inSection = in.getConfigurationSection(path);
            if (inSection == null)
            {
                inSection = in.createSection(path);
            }

            // Out config is entirely plugin-written, no need to look for existing data.
            module.getConfig().load(inSection, out.createSection(path));
        }
    }

    /**
     * Enable or disable modules based on their configuration.
     */
    public void updateModules()
    {
        for (Module module : getModules())
        {
            // Enable or disable modules based on config.
            // Modules track state and will not attempt to enable or disable if already in correct state.
            if (module.getConfig().enabled())
            {
                module.enable();
            }
            else
            {
                module.disable();
            }
        }
    }

    /**
     * Get an unmodifiable {@link Set} of all available modules.
     *
     * @return the available modules
     */
    public @NotNull @Unmodifiable Set<Module> getModules()
    {
        return modules;
    }

    /**
     * Get a {@link Stream} of all enabled modules.
     *
     * @return the available modules
     */
    public @NotNull Stream<Module> getEnabledModules()
    {
        return getModules().stream().filter(Module::isEnabled);
    }

}
