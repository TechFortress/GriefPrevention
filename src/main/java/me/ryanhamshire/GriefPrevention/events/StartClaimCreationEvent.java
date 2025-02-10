package me.ryanhamshire.GriefPrevention.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class StartClaimCreationEvent extends PlayerEvent
{

    private final @NotNull Block clickedBlock;

    public StartClaimCreationEvent(@NotNull Player who, @NotNull Block clickedBlock)
    {
        super(who);
        this.clickedBlock = clickedBlock;
    }

    public final @NotNull Block getClickedBlock()
    {
        return this.clickedBlock;
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
