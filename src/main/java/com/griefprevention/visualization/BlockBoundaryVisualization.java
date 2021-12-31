package com.griefprevention.visualization;

import com.griefprevention.util.IntVector;
import me.ryanhamshire.GriefPrevention.util.BoundingBox;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Function;

public abstract class BlockBoundaryVisualization extends BoundaryVisualization
{

    private final int step;
    private final BoundingBox displayZoneArea;
    private final Collection<BlockElement> elements = new HashSet<>();

    /**
     * Construct a new {@code BlockBoundaryVisualization} with a step size of {@code 10} and a display radius of
     * {@code 75}.
     *
     * @param world the {@link World} being visualized in
     * @param visualizeFrom the {@link IntVector} representing the world coordinate being visualized from
     * @param height the height of the visualization
     */
    protected BlockBoundaryVisualization(@NotNull World world, @NotNull IntVector visualizeFrom, int height)
    {
        this(world, visualizeFrom, height, 10, 75);
    }

    /**
     * Construct a new {@code BlockBoundaryVisualization}.
     *
     * @param world the {@link World} being visualized in
     * @param visualizeFrom the {@link IntVector} representing the world coordinate being visualized from
     * @param height the height of the visualization
     * @param step the distance between individual side elements
     * @param displayZoneRadius the radius in which elements are visible from the visualization location
     */
    protected BlockBoundaryVisualization(
            @NotNull World world,
            @NotNull IntVector visualizeFrom,
            int height,
            int step,
            int displayZoneRadius)
    {
        super(world, visualizeFrom, height);
        this.step = step;
        this.displayZoneArea = new BoundingBox(
                visualizeFrom.add(-displayZoneRadius, -displayZoneRadius, -displayZoneRadius),
                visualizeFrom.add(displayZoneRadius, displayZoneRadius, displayZoneRadius));;
    }

    @Override
    protected void draw(@NotNull Player player, @NotNull Boundary boundary)
    {
        BoundingBox area = boundary.bounds();

        // Trim to area - allows for simplified display containment check later.
        BoundingBox displayZone = displayZoneArea.intersection(area);

        // If area is not inside display zone, there is nothing to display.
        if (displayZone == null) return;

        Function<@NotNull IntVector, @NotNull BlockElement> getCorner = createCorner(boundary);
        Function<@NotNull IntVector, @NotNull BlockElement> getSide = createSide(boundary);

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

    /**
     * Create {@link Function} yielding a corner {@link BlockElement} for the given {@link IntVector} coordinate based
     * on the {@link Boundary} being drawn.
     *
     * @param boundary the {@code Boundary}
     * @return the corner element function
     */
    protected abstract @NotNull Function<@NotNull IntVector, @NotNull BlockElement> createCorner(@NotNull Boundary boundary);

    /**
     * Create {@link Function} yielding a side {@link BlockElement} for the given {@link IntVector} coordinate based
     * on the {@link Boundary} being drawn.
     *
     * @param boundary the {@code Boundary}
     * @return the corner element function
     */
    protected abstract @NotNull Function<@NotNull IntVector, @NotNull BlockElement> createSide(@NotNull Boundary boundary);

    protected boolean isAccessible(@NotNull BoundingBox displayZone, @NotNull IntVector coordinate)
    {
        return displayZone.contains2d(coordinate) && coordinate.isChunkLoaded(world);
    }

    /**
     * Add a display element if accessible.
     *
     * @param displayZone the zone in which elements may be displayed
     * @param coordinate the coordinate being displayed
     * @param getElement the function for obtaining the element displayed
     */
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
    public void revert(@Nullable Player player)
    {
        // If the player cannot visualize the blocks, they should already be effectively reverted.
        if (!canVisualize(player))
        {
            return;
        }

        this.elements.forEach(element -> element.erase(player, world));
    }

    @Override
    protected void erase(@NotNull Player player, @NotNull Boundary boundary)
    {
        this.elements.forEach(element -> element.erase(player, world));
    }

}
