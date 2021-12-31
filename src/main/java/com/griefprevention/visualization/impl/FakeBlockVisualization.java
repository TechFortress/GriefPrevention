package com.griefprevention.visualization.impl;

import com.griefprevention.util.IntVector;
import com.griefprevention.visualization.BoundaryVisualization;
import com.griefprevention.visualization.VisualizationType;
import me.ryanhamshire.GriefPrevention.util.BoundingBox;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link BoundaryVisualization} implementation that displays clientside blocks along
 * {@link com.griefprevention.visualization.Boundary Boundaries}.
 */
public class FakeBlockVisualization extends BoundaryVisualization
{

    /** Distance between side elements. */
    private static final int STEP = 10;
    /** Radius to render within. */
    private static final int DISPLAY_ZONE_RADIUS = 75;

    /**
     * Construct a new {@code FakeBlockVisualization}.
     *
     * @param world the {@link World} being visualized in
     * @param visualizeFrom the {@link IntVector} representing the world coordinate being visualized from
     * @param height the height of the visualization
     */
    public FakeBlockVisualization(@NotNull World world, @NotNull IntVector visualizeFrom, int height) {
        super(world, visualizeFrom, height);
    }

    @Override
    protected void addElements(@NotNull BoundingBox bounds, @NotNull VisualizationType type)
    {
        BlockData cornerBlockData;
        BlockData accentBlockData;

        switch (type)
        {
            case ADMIN_CLAIM -> {
                cornerBlockData = Material.GLOWSTONE.createBlockData();
                accentBlockData = Material.PUMPKIN.createBlockData();
            }
            case SUBDIVISION -> {
                cornerBlockData = Material.IRON_BLOCK.createBlockData();
                accentBlockData = Material.WHITE_WOOL.createBlockData();
            }
            case INITIALIZE_ZONE, NATURE_RESTORATION_ZONE -> {
                cornerBlockData = Material.DIAMOND_BLOCK.createBlockData();
                accentBlockData = Material.DIAMOND_BLOCK.createBlockData();
            }
            case CONFLICT_ZONE -> {
                cornerBlockData = Material.REDSTONE_ORE.createBlockData();
                ((Lightable) cornerBlockData).setLit(true);
                accentBlockData = Material.NETHERRACK.createBlockData();
            }
            default -> {
                cornerBlockData = Material.GLOWSTONE.createBlockData();
                accentBlockData = Material.GOLD_BLOCK.createBlockData();
            }
        }

        addElements(bounds, cornerBlockData, accentBlockData);
    }

    /**
     * Add new elements for a {@link BoundingBox} with the specified fake blocks.
     *
     * @param area the {@code BoundingBox defined}
     * @param cornerBlockData the {@link BlockData} for the corner blocks
     * @param accentBlockData the {@link BlockData} for the non-corner blocks
     */
    //adds a general claim cuboid (represented by min and max) visualization to the current visualization
    private void addElements(@NotNull BoundingBox area, @NotNull BlockData cornerBlockData, @NotNull BlockData accentBlockData) {
        // Set water transparency based on current location.
        boolean waterTransparent = visualizeFrom.toBlock(world).getType() == Material.WATER;

        // Square with display radius centered on current location.
        BoundingBox displayZone = new BoundingBox(
                visualizeFrom.add(-DISPLAY_ZONE_RADIUS, 0, -DISPLAY_ZONE_RADIUS),
                visualizeFrom.add(DISPLAY_ZONE_RADIUS, 0, DISPLAY_ZONE_RADIUS));
        // Trim to area - allows for simplified display containment check later..
        displayZone = displayZone.intersection(area);

        // If area is not inside display zone, there is nothing to display.
        if (displayZone == null) return;

        // North and south boundaries
        for (int x = Math.max(area.getMinX() + STEP, displayZone.getMinX()); x < area.getMaxX() - STEP / 2 && x < displayZone.getMaxX(); x += STEP)
        {
            addDisplayed(displayZone, new IntVector(x, height, area.getMaxZ()), accentBlockData, waterTransparent);
            addDisplayed(displayZone, new IntVector(x, height, area.getMinZ()), accentBlockData, waterTransparent);
        }
        // First and last step are always directly adjacent to corners
        addDisplayed(displayZone, new IntVector(area.getMinX() + 1, height, area.getMaxZ()), accentBlockData, waterTransparent);
        addDisplayed(displayZone, new IntVector(area.getMinX() + 1, height, area.getMinZ()), accentBlockData, waterTransparent);
        addDisplayed(displayZone, new IntVector(area.getMaxX() - 1, height, area.getMaxZ()), accentBlockData, waterTransparent);
        addDisplayed(displayZone, new IntVector(area.getMaxX() - 1, height, area.getMinZ()), accentBlockData, waterTransparent);

        // East and west boundaries
        for (int z = Math.max(area.getMinZ() + STEP, displayZone.getMinZ()); z < area.getMaxZ() - STEP / 2 && z < displayZone.getMaxZ(); z += STEP)
        {
            addDisplayed(displayZone, new IntVector(area.getMinX(), height, z), accentBlockData, waterTransparent);
            addDisplayed(displayZone, new IntVector(area.getMaxX(), height, z), accentBlockData, waterTransparent);
        }
        addDisplayed(displayZone, new IntVector(area.getMinX(), height, area.getMinZ() + 1), accentBlockData, waterTransparent);
        addDisplayed(displayZone, new IntVector(area.getMaxX(), height, area.getMinZ() + 1), accentBlockData, waterTransparent);
        addDisplayed(displayZone, new IntVector(area.getMinX(), height, area.getMaxZ() - 1), accentBlockData, waterTransparent);
        addDisplayed(displayZone, new IntVector(area.getMaxX(), height, area.getMaxZ() - 1), accentBlockData, waterTransparent);

        // Add corners last to override any other elements created by very small claims.
        addDisplayed(displayZone, new IntVector(area.getMinX(), height, area.getMaxZ()), cornerBlockData, waterTransparent);
        addDisplayed(displayZone, new IntVector(area.getMaxX(), height, area.getMaxZ()), cornerBlockData, waterTransparent);
        addDisplayed(displayZone, new IntVector(area.getMinX(), height, area.getMinZ()), cornerBlockData, waterTransparent);
        addDisplayed(displayZone, new IntVector(area.getMaxX(), height, area.getMinZ()), cornerBlockData, waterTransparent);
    }

    /**
     * Attempt to add a visible display element.
     *
     * @param displayZone the {@link BoundingBox} of displayed area
     * @param vector the {@link IntVector} of the display location
     * @param data the {@link BlockData} of the fake marker block
     * @param waterTransparent whether water is considered transparent for display purposes
     */
    private void addDisplayed(
            @NotNull BoundingBox displayZone,
            @NotNull IntVector vector,
            @NotNull BlockData data,
            boolean waterTransparent)
    {
        if (!displayZone.contains2d(vector) || !vector.isChunkLoaded(world)) {
            return;
        }

        Block displayLoc = getVisibleLocation(vector, waterTransparent);
        this.addElement(new FakeBlockElement(new IntVector(displayLoc), displayLoc.getBlockData(), data));
    }

    /**
     * Find a location that should be visible to players. This causes the visualization to "cling" to the ground.
     *
     * @param vector the {@link IntVector} of the display location
     * @param waterTransparent whether water is considered transparent for display purposes
     * @return the located {@link Block}
     */
    private Block getVisibleLocation(@NotNull IntVector vector, boolean waterTransparent)
    {
        Block block = vector.toBlock(world);
        BlockFace direction = (isTransparent(block, waterTransparent)) ? BlockFace.DOWN : BlockFace.UP;

        while (block.getY() >= world.getMinHeight() &&
                block.getY() < world.getMaxHeight() - 1 &&
                (!isTransparent(block.getRelative(BlockFace.UP), waterTransparent) || isTransparent(block, waterTransparent)))
        {
            block = block.getRelative(direction);
        }

        return block;
    }

    /**
     * Helper method for determining if a {@link Block} is transparent from the top down.
     *
     * @param block the {@code block}
     * @param waterIsTransparent whether water is considered transparent for display purposes
     * @return true if transparent
     */
    private boolean isTransparent(@NotNull Block block, boolean waterIsTransparent)
    {
        Material blockMaterial = block.getType();

        // Custom per-material definitions.
        switch (blockMaterial)
        {
            case WATER:
                return waterIsTransparent;
            case SNOW:
                return false;
        }

        if (blockMaterial.isAir()
                || Tag.FENCES.isTagged(blockMaterial)
                || Tag.FENCE_GATES.isTagged(blockMaterial)
                || Tag.SIGNS.isTagged(blockMaterial)
                || Tag.WALLS.isTagged(blockMaterial)
                || Tag.WALL_SIGNS.isTagged(blockMaterial))
            return true;

        return block.getType().isTransparent();
    }

}
