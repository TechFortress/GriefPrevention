package com.griefprevention.visualization;

import com.griefprevention.util.IntVector;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public interface VisualizationProvider
{

    @NotNull BoundaryVisualization create(@NotNull World world, @NotNull IntVector visualizeFrom, int height);

}
