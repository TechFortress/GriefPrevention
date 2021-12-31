package com.griefprevention.visualization;

import com.griefprevention.util.IntVector;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class BlockElement
{

    private final @NotNull IntVector vector;

    public BlockElement(@NotNull IntVector vector) {
        this.vector = vector;
    }

    public @NotNull IntVector getVector()
    {
        return vector;
    }

    protected abstract void draw(@NotNull Player player, @NotNull World world);

    protected abstract void erase(@NotNull Player player, @NotNull World world);

    @Override
    public boolean equals(@Nullable Object other)
    {
        if (this == other) return true;
        if (other == null || !getClass().isAssignableFrom(other.getClass())) return false;
        BlockElement that = (BlockElement) other;
        return vector.equals(that.vector);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(vector);
    }

}
