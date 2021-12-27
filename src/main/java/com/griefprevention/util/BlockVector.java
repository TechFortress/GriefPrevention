package com.griefprevention.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record BlockVector(int x, int y, int z)
{

    public BlockVector(@NotNull Block block)
    {
        this(block.getX(), block.getY(), block.getZ());
    }

    public BlockVector(@NotNull Location location)
    {
        this(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public BlockVector(@NotNull Vector vector)
    {
        this(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    public Block toBlock(@NotNull World world)
    {
        return world.getBlockAt(x(), y(), z());
    }

    public Location toLocation(@Nullable World world)
    {
        return new Location(world, x(), y(), z());
    }

    public BlockVector toVector()
    {
        return new BlockVector(x(), y(), z());
    }

    public boolean isChunkLoaded(@NotNull World world)
    {
        // Note: Location#getChunk et cetera load the chunk!
        // Only World#isChunkLoaded is safe without a Chunk object.
        return world.isChunkLoaded(x() >> 4, z() >> 4);
    }

}
