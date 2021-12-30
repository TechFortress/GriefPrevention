package com.griefprevention.visualization;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public interface VisualizationProvider
{

    @NotNull BoundaryVisualization create(World world);

}
