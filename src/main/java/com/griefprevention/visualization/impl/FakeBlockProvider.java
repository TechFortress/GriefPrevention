package com.griefprevention.visualization.impl;

import com.griefprevention.visualization.BoundaryVisualization;
import com.griefprevention.visualization.VisualizationProvider;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.VisualizationType;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;

public class FakeBlockProvider implements VisualizationProvider
{

    @Override
    public @Nullable BoundaryVisualization of(@NotNull Iterable<Claim> claims, int height, @NotNull VisualizationType visualizationType, Location location)
    {
        Iterator<Claim> iterator = claims.iterator();

        // No claims mean nothing to visualize.
        if (!iterator.hasNext()) return null;

        Claim claim = iterator.next();
        World world = claim.getLesserBoundaryCorner().getWorld();

        // World must be set.
        if (world == null) return null;

        FakeBlockVisualization visualization = new FakeBlockVisualization(world);

        // Do while to always include first claim.
        do visualization.addElements(claim, height, visualizationType, location);
        while (iterator.hasNext() && Objects.equals((claim = iterator.next()).getLesserBoundaryCorner().getWorld(), world));

        return visualization;
    }

}
