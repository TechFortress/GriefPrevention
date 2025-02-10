package me.ryanhamshire.GriefPrevention.events;

import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class StartClaimResizeEvent extends PlayerEvent
{

    private final @NotNull Claim claim;
    private final @NotNull Block clickedBlock;

    public StartClaimResizeEvent(@NotNull Player who, @NotNull Claim claim, @NotNull Block clickedBlock)
    {
        super(who);
        this.claim = claim;
        this.clickedBlock = clickedBlock;
    }

    public final @NotNull Claim getClaim()
    {
        return this.claim;
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
