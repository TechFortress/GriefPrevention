package me.ryanhamshire.GriefPrevention.events;

import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class StartSubdivideClaimCreationEvent extends StartClaimCreationEvent {

    private final @NotNull Claim parent;

    public StartSubdivideClaimCreationEvent(@NotNull Player who, @NotNull Block clickedBlock, @NotNull Claim parent)
    {
        super(who, clickedBlock);
        this.parent = parent;
    }

    public final @NotNull Claim getParent()
    {
        return this.parent;
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
