package com.griefprevention.visualization.impl;

import com.griefprevention.util.IntVector;
import com.griefprevention.visualization.BlockBoundaryVisualization;
import com.griefprevention.visualization.BlockElement;
import com.griefprevention.visualization.Boundary;
import com.griefprevention.visualization.BoundaryVisualization;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * A {@link BoundaryVisualization} implementation that displays clientside blocks along
 * {@link com.griefprevention.visualization.Boundary Boundaries}.
 */
public class FakeBlockVisualization extends BlockBoundaryVisualization
{
    private final boolean waterTransparent;

    /**
     * Construct a new {@code FakeBlockVisualization}.
     *
     * @param world the {@link World} being visualized in
     * @param visualizeFrom the {@link IntVector} representing the world coordinate being visualized from
     * @param height the height of the visualization
     */
    public FakeBlockVisualization(@NotNull World world, @NotNull IntVector visualizeFrom, int height) {
        super(world, visualizeFrom, height);
        waterTransparent = visualizeFrom.toBlock(world).getType() == Material.WATER;
    }

    @Override
    protected @NotNull Function<@NotNull IntVector, @NotNull BlockElement> cornerBlock(@NotNull Boundary boundary)
    {
        BlockData fakeData;
        switch (boundary.type())
        {
            case SUBDIVISION -> fakeData = Material.IRON_BLOCK.createBlockData();
            case INITIALIZE_ZONE, NATURE_RESTORATION_ZONE -> fakeData = Material.DIAMOND_BLOCK.createBlockData();
            case CONFLICT_ZONE -> {
                fakeData = Material.REDSTONE_ORE.createBlockData();
                ((Lightable) fakeData).setLit(true);
            }
            default -> fakeData = Material.GLOWSTONE.createBlockData();
        }

        return vector -> {
            Block visibleLocation = getVisibleLocation(vector);
            return new FakeBlockElement(new IntVector(visibleLocation), visibleLocation.getBlockData(), fakeData);
        };
    }


    @Override
    protected @NotNull Function<@NotNull IntVector, @NotNull BlockElement> sideBlock(@NotNull Boundary boundary)
    {
        BlockData fakeData;
        switch (boundary.type())
        {
            case ADMIN_CLAIM -> fakeData = Material.PUMPKIN.createBlockData();
            case SUBDIVISION -> fakeData = Material.WHITE_WOOL.createBlockData();
            case INITIALIZE_ZONE, NATURE_RESTORATION_ZONE -> fakeData = Material.DIAMOND_BLOCK.createBlockData();
            case CONFLICT_ZONE -> fakeData = Material.NETHERRACK.createBlockData();
            default -> fakeData = Material.GOLD_BLOCK.createBlockData();
        }

        return vector -> {
            Block visibleLocation = getVisibleLocation(vector);
            return new FakeBlockElement(new IntVector(visibleLocation), visibleLocation.getBlockData(), fakeData);
        };
    }

    /**
     * Find a location that should be visible to players. This causes the visualization to "cling" to the ground.
     *
     * @param vector the {@link IntVector} of the display location
     * @return the located {@link Block}
     */
    private Block getVisibleLocation(@NotNull IntVector vector)
    {
        Block block = vector.toBlock(world);
        BlockFace direction = (isTransparent(block)) ? BlockFace.DOWN : BlockFace.UP;

        while (block.getY() >= world.getMinHeight() &&
                block.getY() < world.getMaxHeight() - 1 &&
                (!isTransparent(block.getRelative(BlockFace.UP)) || isTransparent(block)))
        {
            block = block.getRelative(direction);
        }

        return block;
    }

    /**
     * Helper method for determining if a {@link Block} is transparent from the top down.
     *
     * @param block the {@code block}
     * @return true if transparent
     */
    private boolean isTransparent(@NotNull Block block)
    {
        Material blockMaterial = block.getType();

        // Custom per-material definitions.
        switch (blockMaterial)
        {
            case WATER:
                return waterTransparent;
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
