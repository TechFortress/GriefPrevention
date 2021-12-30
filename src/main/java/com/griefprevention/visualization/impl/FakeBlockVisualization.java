package com.griefprevention.visualization.impl;

import com.griefprevention.util.BlockVector;
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

public class FakeBlockVisualization extends BoundaryVisualization
{

    private static final int STEP = 10;
    public static final int DISPLAY_ZONE_RADIUS = 75;

    public FakeBlockVisualization(@NotNull World world) {
        super(world);
    }

    @Override
    protected void addElements(@NotNull BoundingBox bounds, @NotNull BlockVector center, int height, @NotNull VisualizationType type)
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

        addElements(bounds, center, height, cornerBlockData, accentBlockData);
    }

    //adds a general claim cuboid (represented by min and max) visualization to the current visualization
    private void addElements(BoundingBox area, BlockVector center, int height, BlockData cornerBlockData, BlockData accentBlockData) {
        // Set water transparency based on current location.
        boolean waterTransparent = center.toBlock(world).getType() == Material.WATER;

        // Square with display radius centered on current location.
        BoundingBox displayZone = new BoundingBox(
                center.add(-DISPLAY_ZONE_RADIUS, 0, -DISPLAY_ZONE_RADIUS),
                center.add(DISPLAY_ZONE_RADIUS, 0, DISPLAY_ZONE_RADIUS));
        // Trim to area - allows for simplified display containment check later..
        displayZone = displayZone.intersection(area);

        // If area is not inside display zone, there is nothing to display.
        if (displayZone == null) return;

        // North and south boundaries
        for (int x = Math.max(area.getMinX() + STEP, displayZone.getMinX()); x < area.getMaxX() - STEP / 2 && x < displayZone.getMaxX(); x += STEP)
        {
            addDisplayed(displayZone, world, new BlockVector(x, height, area.getMaxZ()), accentBlockData, waterTransparent);
            addDisplayed(displayZone, world, new BlockVector(x, height, area.getMinZ()), accentBlockData, waterTransparent);
        }
        // First and last step are always directly adjacent to corners
        addDisplayed(displayZone, world, new BlockVector(area.getMinX() + 1, height, area.getMaxZ()), accentBlockData, waterTransparent);
        addDisplayed(displayZone, world, new BlockVector(area.getMinX() + 1, height, area.getMinZ()), accentBlockData, waterTransparent);
        addDisplayed(displayZone, world, new BlockVector(area.getMaxX() - 1, height, area.getMaxZ()), accentBlockData, waterTransparent);
        addDisplayed(displayZone, world, new BlockVector(area.getMaxX() - 1, height, area.getMinZ()), accentBlockData, waterTransparent);

        // East and west boundaries
        for (int z = Math.max(area.getMinZ() + STEP, displayZone.getMinZ()); z < area.getMaxZ() - STEP / 2 && z < displayZone.getMaxZ(); z += STEP)
        {
            addDisplayed(displayZone, world, new BlockVector(area.getMinX(), height, z), accentBlockData, waterTransparent);
            addDisplayed(displayZone, world, new BlockVector(area.getMaxX(), height, z), accentBlockData, waterTransparent);
        }
        addDisplayed(displayZone, world, new BlockVector(area.getMinX(), height, area.getMinZ() + 1), accentBlockData, waterTransparent);
        addDisplayed(displayZone, world, new BlockVector(area.getMaxX(), height, area.getMinZ() + 1), accentBlockData, waterTransparent);
        addDisplayed(displayZone, world, new BlockVector(area.getMinX(), height, area.getMaxZ() - 1), accentBlockData, waterTransparent);
        addDisplayed(displayZone, world, new BlockVector(area.getMaxX(), height, area.getMaxZ() - 1), accentBlockData, waterTransparent);

        // Add corners last to override any other elements created by very small claims.
        addDisplayed(displayZone, world, new BlockVector(area.getMinX(), height, area.getMaxZ()), cornerBlockData, waterTransparent);
        addDisplayed(displayZone, world, new BlockVector(area.getMaxX(), height, area.getMaxZ()), cornerBlockData, waterTransparent);
        addDisplayed(displayZone, world, new BlockVector(area.getMinX(), height, area.getMinZ()), cornerBlockData, waterTransparent);
        addDisplayed(displayZone, world, new BlockVector(area.getMaxX(), height, area.getMinZ()), cornerBlockData, waterTransparent);
    }

    private void addDisplayed(BoundingBox displayZone, World world, BlockVector vector, BlockData data, boolean waterTransparent)
    {
        if (!displayZone.contains2d(vector) || !vector.isChunkLoaded(world)) {
            return;
        }

        Block displayLoc = getVisibleLocation(world, vector, waterTransparent);
        this.addElement(new FakeBlockElement(new BlockVector(displayLoc), displayLoc.getBlockData(), data));
    }

    //finds a block the player can probably see.  this is how visualizations "cling" to the ground
    private Block getVisibleLocation(World world, BlockVector vector, boolean waterTransparent)
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

    //helper method for above.  allows visualization blocks to sit underneath partly transparent blocks like grass and fence
    private boolean isTransparent(Block block, boolean waterIsTransparent)
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
