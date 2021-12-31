package com.griefprevention.visualization.impl;

import com.griefprevention.util.IntVector;
import com.griefprevention.visualization.VisualizationProvider;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link VisualizationProvider} that supplies {@link FakeBlockVisualization FakeBlockVisualizations}.
 */
public class FakeBlockProvider implements VisualizationProvider
{

    @Override
    public @NotNull FakeBlockVisualization create(@NotNull World world, @NotNull IntVector visualizeFrom, int height)
    {
        return new FakeBlockVisualization(world, visualizeFrom, height);
    }

}
