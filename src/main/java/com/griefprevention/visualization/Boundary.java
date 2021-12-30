package com.griefprevention.visualization;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Boundary(
        @NotNull BoundingBox bounds,
        @NotNull VisualizationType type,
        @Nullable Claim claim)
{

    public Boundary(@NotNull BoundingBox bounds, @NotNull VisualizationType type)
    {
        this(bounds, type, null);
    }

    public Boundary(@NotNull Claim claim, @NotNull VisualizationType type)
    {
        this(new BoundingBox(claim), type, claim);
    }

}
