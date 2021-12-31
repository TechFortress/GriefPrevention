package com.griefprevention.visualization;

import com.griefprevention.util.IntVector;
import me.ryanhamshire.GriefPrevention.util.BoundingBox;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Function;

public abstract class BlockBoundaryVisualization extends BoundaryVisualization
{

    /** Distance between side elements. */
    private final int step;
    /** Radius to render within. */
    private final int displayZoneRadius;
    /** Drawn elements */
    private final Collection<BlockElement> elements = new HashSet<>();

    protected BlockBoundaryVisualization(@NotNull World world, @NotNull IntVector visualizeFrom, int height)
    {
        this(world, visualizeFrom, height, 10, 75);
    }

    protected BlockBoundaryVisualization(
            @NotNull World world,
            @NotNull IntVector visualizeFrom,
            int height,
            int step,
            int displayZoneRadius)
    {
        super(world, visualizeFrom, height);
        this.step = step;
        this.displayZoneRadius = displayZoneRadius;
    }

    @Override
    protected void draw(@NotNull Player player, @NotNull Boundary boundary)
    {
        elements.clear();

        // Square with display radius centered on current location.
        BoundingBox displayZone = new BoundingBox(
                visualizeFrom.add(-displayZoneRadius, 0, -displayZoneRadius),
                visualizeFrom.add(displayZoneRadius, 0, displayZoneRadius));

        BoundingBox area = boundary.bounds();

        // Trim to area - allows for simplified display containment check later.
        displayZone = displayZone.intersection(area);

        // If area is not inside display zone, there is nothing to display.
        if (displayZone == null) return;

        Function<@NotNull IntVector, @NotNull BlockElement> getCorner = cornerBlock(boundary);
        Function<@NotNull IntVector, @NotNull BlockElement> getSide = sideBlock(boundary);

        // North and south boundaries
        for (int x = Math.max(area.getMinX() + step, displayZone.getMinX()); x < area.getMaxX() - step / 2 && x < displayZone.getMaxX(); x += step)
        {
            addDisplayed(displayZone, new IntVector(x, height, area.getMaxZ()), getSide);
            addDisplayed(displayZone, new IntVector(x, height, area.getMinZ()), getSide);
        }
        // First and last step are always directly adjacent to corners
        if (area.getLength() > 2)
        {
            addDisplayed(displayZone, new IntVector(area.getMinX() + 1, height, area.getMaxZ()), getSide);
            addDisplayed(displayZone, new IntVector(area.getMinX() + 1, height, area.getMinZ()), getSide);
            addDisplayed(displayZone, new IntVector(area.getMaxX() - 1, height, area.getMaxZ()), getSide);
            addDisplayed(displayZone, new IntVector(area.getMaxX() - 1, height, area.getMinZ()), getSide);
        }

        // East and west boundaries
        for (int z = Math.max(area.getMinZ() + step, displayZone.getMinZ()); z < area.getMaxZ() - step / 2 && z < displayZone.getMaxZ(); z += step)
        {
            addDisplayed(displayZone, new IntVector(area.getMinX(), height, z), getSide);
            addDisplayed(displayZone, new IntVector(area.getMaxX(), height, z), getSide);
        }
        if (area.getWidth() > 2)
        {
            addDisplayed(displayZone, new IntVector(area.getMinX(), height, area.getMinZ() + 1), getSide);
            addDisplayed(displayZone, new IntVector(area.getMaxX(), height, area.getMinZ() + 1), getSide);
            addDisplayed(displayZone, new IntVector(area.getMinX(), height, area.getMaxZ() - 1), getSide);
            addDisplayed(displayZone, new IntVector(area.getMaxX(), height, area.getMaxZ() - 1), getSide);
        }

        // Add corners last to override any other elements created by very small claims.
        addDisplayed(displayZone, new IntVector(area.getMinX(), height, area.getMaxZ()), getCorner);
        addDisplayed(displayZone, new IntVector(area.getMaxX(), height, area.getMaxZ()), getCorner);
        addDisplayed(displayZone, new IntVector(area.getMinX(), height, area.getMinZ()), getCorner);
        addDisplayed(displayZone, new IntVector(area.getMaxX(), height, area.getMinZ()), getCorner);

        elements.forEach(element -> element.draw(player, world));
    }

    protected abstract @NotNull Function<@NotNull IntVector, @NotNull BlockElement> cornerBlock(@NotNull Boundary boundary);

    protected abstract @NotNull Function<@NotNull IntVector, @NotNull BlockElement> sideBlock(@NotNull Boundary boundary);

    protected boolean isAccessible(@NotNull BoundingBox displayZone, @NotNull IntVector coordinate)
    {
        return displayZone.contains2d(coordinate) && coordinate.isChunkLoaded(world);
    }

    private void addDisplayed(
            @NotNull BoundingBox displayZone,
            @NotNull IntVector coordinate,
            @NotNull Function<@NotNull IntVector,
                    @NotNull BlockElement> getElement)
    {
        if (isAccessible(displayZone, coordinate)) {
            this.elements.add(getElement.apply(coordinate));
        }
    }

    @Override
    protected void erase(@NotNull Player player, @NotNull Boundary boundary)
    {
        this.elements.forEach(element -> element.erase(player, world));
    }

}
