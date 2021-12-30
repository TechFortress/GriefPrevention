package me.ryanhamshire.GriefPrevention.events;

import com.griefprevention.visualization.BoundaryDefinition;
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

    private final @NotNull Collection<BoundaryDefinition> boundaries;
    private @NotNull VisualizationProvider provider;

    public BoundaryVisualizationEvent(
            @NotNull Player player,
            @NotNull Collection<BoundaryDefinition> boundaries
    ) {
        this(player, boundaries, BoundaryVisualization.DEFAULT_PROVIDER);
    }

    public BoundaryVisualizationEvent(
            @NotNull Player player,
            @NotNull Collection<BoundaryDefinition> boundaries,
            @NotNull VisualizationProvider provider
    ) {
        super(player);
        this.boundaries = new ArrayList<>(boundaries);
        this.provider = provider;
    }

    public @NotNull Collection<BoundaryDefinition> getBoundaries()
    {
        return boundaries;
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
