package com.griefprevention.visualization.impl;

import com.griefprevention.visualization.BlockElement;
import com.griefprevention.util.IntVector;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link BlockElement} that displays itself as a clientside block.
 */
public final class FakeBlockElement extends BlockElement
{

    private final @NotNull BlockData realBlock;
    private final @NotNull BlockData visualizedBlock;

    public FakeBlockElement(
            @NotNull IntVector intVector,
            @NotNull BlockData realBlock,
            @NotNull BlockData visualizedBlock)
    {
        super(intVector);
        this.realBlock = realBlock;
        this.visualizedBlock = visualizedBlock;
    }

    @Override
    protected void draw(@NotNull Player player, @NotNull World world)
    {
        // Send the player a fake block change event only if the chunk is loaded.
        if (!getVector().isChunkLoaded(world)) return;
        player.sendBlockChange(getVector().toLocation(world), visualizedBlock);
    }

    @Override
    protected void erase(@NotNull Player player, @NotNull World world)
    {
        player.sendBlockChange(getVector().toLocation(world), realBlock);
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other) return true;
        if (!super.equals(other)) return false;
        if (getClass() != other.getClass()) return false;
        FakeBlockElement that = (FakeBlockElement) other;
        return realBlock.equals(that.realBlock) && visualizedBlock.equals(that.visualizedBlock);
    }

}
