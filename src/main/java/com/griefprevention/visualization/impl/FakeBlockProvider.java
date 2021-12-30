package com.griefprevention.visualization.impl;

import com.griefprevention.visualization.BoundaryVisualization;
import com.griefprevention.visualization.VisualizationProvider;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class FakeBlockProvider implements VisualizationProvider
{

    @Override
    public @NotNull BoundaryVisualization create(World world)
    {
        return new FakeBlockVisualization(world);
    }

}
