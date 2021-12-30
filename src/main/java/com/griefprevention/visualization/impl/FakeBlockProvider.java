package com.griefprevention.visualization.impl;

import com.griefprevention.util.IntVector;
import com.griefprevention.visualization.BoundaryVisualization;
import com.griefprevention.visualization.VisualizationProvider;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class FakeBlockProvider implements VisualizationProvider
{

    @Override
    public @NotNull BoundaryVisualization create(@NotNull World world, @NotNull IntVector visualizeFrom, int height)
    {
        return new FakeBlockVisualization(world, visualizeFrom, height);
    }

}
