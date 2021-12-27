package com.griefprevention.visualization;

import com.griefprevention.util.BlockVector;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class BoundaryElement
{

    private final @NotNull BlockVector vector;

    public BoundaryElement(@NotNull BlockVector vector) {
        this.vector = vector;
    }

    public @NotNull BlockVector getVector()
    {
        return vector;
    }

    protected abstract void apply(@NotNull Player player, @NotNull World world);

    protected abstract void revert(@NotNull Player player, @NotNull World world);

    @Override
    public boolean equals(@Nullable Object other)
    {
        if (this == other) return true;
        if (other == null || !getClass().isAssignableFrom(other.getClass())) return false;
        BoundaryElement that = (BoundaryElement) other;
        return vector.equals(that.vector);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(vector);
    }

}
