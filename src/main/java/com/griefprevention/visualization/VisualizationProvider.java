package com.griefprevention.visualization;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.VisualizationType;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface VisualizationProvider
{

    @Nullable BoundaryVisualization of(
            @NotNull Iterable<Claim> claims,
            int height,
            @NotNull VisualizationType visualizationType,
            Location location);

}
