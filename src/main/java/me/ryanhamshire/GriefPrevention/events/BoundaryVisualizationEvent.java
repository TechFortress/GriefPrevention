package me.ryanhamshire.GriefPrevention.events;

import com.griefprevention.util.IntVector;
import com.griefprevention.visualization.Boundary;
import com.griefprevention.visualization.BoundaryVisualization;
import com.griefprevention.visualization.VisualizationProvider;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class BoundaryVisualizationEvent extends PlayerEvent
{

    private final @NotNull Collection<Boundary> boundaries;
    private final int height;
    private @NotNull VisualizationProvider provider;

    public BoundaryVisualizationEvent(
            @NotNull Player player,
            @NotNull Collection<Boundary> boundaries,
            int height
    ) {
        this(player, boundaries, height, BoundaryVisualization.DEFAULT_PROVIDER);
    }

    public BoundaryVisualizationEvent(
            @NotNull Player player,
            @NotNull Collection<Boundary> boundaries,
            int height,
            @NotNull VisualizationProvider provider
    ) {
        super(player);
        this.boundaries = new ArrayList<>(boundaries);
        this.height = height;
        this.provider = provider;
    }

    public @NotNull Collection<Boundary> getBoundaries()
    {
        return boundaries;
    }

    public @NotNull IntVector getCenter()
    {
        return new IntVector(player.getLocation());
    }

    public int getHeight()
    {
        return height;
    }

    public @NotNull VisualizationProvider getProvider()
    {
        return provider;
    }

    public void setProvider(@NotNull VisualizationProvider provider)
    {
        this.provider = provider;
    }

    // Listenable event requirements
    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return HANDLERS;
    }

}
