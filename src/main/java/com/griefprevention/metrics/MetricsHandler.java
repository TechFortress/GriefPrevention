package com.griefprevention.metrics;

import me.ryanhamshire.GriefPrevention.GriefPrevention;

/**
 * Created on 3/2/2024.
 *
 * @author RoboMWM
 */
public class MetricsHandler
{
    private final Metrics metrics;

    public MetricsHandler(GriefPrevention plugin)
    {
        metrics = new Metrics(plugin, 3294);

        try
        {
            metrics.addCustomChart(new Metrics.SimplePie("bukkit_implementation", () -> plugin.getServer().getVersion().split("-")[1]));
        }
        catch (Throwable ignored) {}
    }
}
