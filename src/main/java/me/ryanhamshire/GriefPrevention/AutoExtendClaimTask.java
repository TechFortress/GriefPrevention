package me.ryanhamshire.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

//automatically extends a claim downward based on block types detected
class AutoExtendClaimTask implements Runnable
{
    private final Claim claim;
    private final ArrayList<ChunkSnapshot> chunks;
    private final Environment worldType;
    private final Map<Biome, Set<Material>> biomePlayerMaterials = new HashMap<>();
    private final int minY;
    private final int lowestExistingY;

    public AutoExtendClaimTask(
            @NotNull Claim claim,
            @NotNull ArrayList<@NotNull ChunkSnapshot> chunks,
            @NotNull Environment worldType,
            int lowestExistingY)
    {
        this.claim = claim;
        this.chunks = chunks;
        this.worldType = worldType;
        this.lowestExistingY = Math.min(lowestExistingY, claim.getLesserBoundaryCorner().getBlockY());
        this.minY = Math.max(
                Objects.requireNonNull(claim.getLesserBoundaryCorner().getWorld()).getMinHeight(),
                GriefPrevention.instance.config_claims_maxDepth);
    }

    @Override
    public void run()
    {
        int newY = this.getLowestBuiltY();
        if (newY < this.claim.getLesserBoundaryCorner().getBlockY())
        {
            Bukkit.getScheduler().runTask(GriefPrevention.instance, new ExecuteExtendClaimTask(claim, newY));
        }
    }

    private int getLowestBuiltY()
    {
        int y = this.lowestExistingY;

        if (yTooSmall(y)) return this.minY;

        for (ChunkSnapshot chunk : this.chunks)
        {
            y = findLowerBuiltY(chunk, y);

            // If already at minimum Y, stop searching.
            if (yTooSmall(y)) return this.minY;
        }

        return y;
    }

    private int findLowerBuiltY(ChunkSnapshot chunkSnapshot, int y)
    {
        // Specifically not using yTooSmall here to allow protecting bottom layer.
        nextY: for (int newY = y - 1; newY >= this.minY; newY--)
        {
            for (int x = 0; x < 16; x++)
            {
                for (int z = 0; z < 16; z++)
                {
                    // If the block is natural, ignore it and continue searching the same Y level.
                    if (!isPlayerBlock(chunkSnapshot, x, newY, z)) continue;

                    // If the block is player-placed and we're at the minimum Y allowed, we're done searching.
                    if (yTooSmall(y)) return this.minY;

                    // Because we found a player block, repeatedly check the next block in the column.
                    while (isPlayerBlock(chunkSnapshot, x, newY--, z))
                    {
                        // If we've hit minimum Y we're done searching.
                        if (yTooSmall(y)) return this.minY;
                    }

                    // Undo increment for unsuccessful player block check.
                    newY++;

                    // Move built level down to current level.
                    y = newY;

                    // Because the level is now protected, continue downwards.
                    continue nextY;
                }
            }
        }

        // Return provided value or last located player block level.
        return y;
    }

    private boolean yTooSmall(int y)
    {
        return y <= this.minY;
    }

    private boolean isPlayerBlock(ChunkSnapshot chunkSnapshot, int x, int y, int z)
    {
        Material blockType = chunkSnapshot.getBlockType(x, y, z);
        Biome biome = chunkSnapshot.getBiome(x, y, z);

        return this.getBiomePlayerBlocks(biome).contains(blockType);
    }

    private Set<Material> getBiomePlayerBlocks(Biome biome)
    {
        return biomePlayerMaterials.computeIfAbsent(biome, newBiome -> RestoreNatureProcessingTask.getPlayerBlocks(this.worldType, newBiome));
    }

    //runs in the main execution thread, where it can safely change claims and save those changes
    private record ExecuteExtendClaimTask(Claim claim, int newY) implements Runnable
    {
        @Override
        public void run()
        {
            GriefPrevention.instance.dataStore.extendClaim(claim, newY);
        }
    }

}
