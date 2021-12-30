package com.griefprevention.visualization;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import me.ryanhamshire.GriefPrevention.events.BoundaryVisualizationEvent;
import com.griefprevention.util.BlockVector;
import me.ryanhamshire.GriefPrevention.util.BoundingBox;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BoundaryVisualization
{

    public static final VisualizationProvider DEFAULT_PROVIDER = new com.griefprevention.visualization.impl.FakeBlockProvider();

    private final Collection<BoundaryElement> elements = new HashSet<>();
    protected final @NotNull World world;

    protected BoundaryVisualization(@NotNull World world)
    {
        super();
        this.world = world;
    }

    protected final void addElement(BoundaryElement element) {
        elements.add(element);
    }

    protected abstract void addElements(@NotNull BoundingBox bounds, @NotNull BlockVector center, int height, @NotNull VisualizationType type);

    protected void apply(@NotNull Player player)
    {
        PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());

        // If they have a visualization active, clear it first.
        playerData.setVisibleBoundaries(null);

        // If they are online and in the same world as the visualization, display the visualization next tick.
        if (canVisualize(player))
        {
            GriefPrevention.instance.getServer().getScheduler().scheduleSyncDelayedTask(
                    GriefPrevention.instance,
                    () -> applyVisuals(player, playerData),
                    1L);
        }
    }

    @Contract("null -> false")
    private boolean canVisualize(@Nullable Player player) {
        return player != null && player.isOnline() && !elements.isEmpty() && Objects.equals(world, player.getWorld());
    }

    void applyVisuals(@NotNull Player player, @NotNull PlayerData playerData)
    {
        // Apply all visualization elements.
        elements.forEach(element -> element.apply(player, world));

        // Remember the visualization so it can be reverted.
        playerData.setVisibleBoundaries(this);

        // Schedule automatic reversion.
        scheduleRevert(player, playerData);
    }

    protected void scheduleRevert(@NotNull Player player, @NotNull PlayerData playerData)
    {
        GriefPrevention.instance.getServer().getScheduler().scheduleSyncDelayedTask(
                GriefPrevention.instance,
                () -> {
                    if (playerData.getVisibleBoundaries() == this) revert(player);
                },
                20L * 60);
    }

    public void revert(@Nullable Player player)
    {
        // If the player cannot visualize the blocks, they should already be effectively reverted.
        if (!canVisualize(player))
        {
            return;
        }

        // Revert data as necessary for any sent elements.
        elements.forEach(element -> element.revert(player, world));
    }

    public static void visualizeClaim(@NotNull Player player, @NotNull Claim claim, @NotNull VisualizationType type)
    {
        visualizeClaim(player, claim, player.getEyeLocation().getBlockY(), type);
    }

    public static void visualizeClaim(@NotNull Player player, @NotNull Claim claim, @NotNull Block block, @NotNull VisualizationType type)
    {
        visualizeClaim(player, claim, block.getY(), type);
    }

    private static void visualizeClaim(@NotNull Player player, @NotNull Claim claim, int height, @NotNull VisualizationType type)
    {
        BoundaryVisualizationEvent event = new BoundaryVisualizationEvent(player, defineBoundaries(claim, type), DEFAULT_PROVIDER);
        visualizeClaims(event, new BlockVector(player.getLocation()), height);
    }

    private static Collection<BoundaryDefinition> defineBoundaries(Claim claim, VisualizationType type)
    {
        if (type == VisualizationType.CLAIM && claim.isAdminClaim()) type = VisualizationType.ADMIN_CLAIM;

        return Stream.concat(
                Stream.of(new BoundaryDefinition(claim, type)),
                claim.children.stream().map(child -> new BoundaryDefinition(child, VisualizationType.SUBDIVISION)))
                .collect(Collectors.toSet());
    }

    public static void visualizeNearbyClaims(
            @NotNull Player player,
            @NotNull Collection<Claim> claims,
            int height)
    {
        BlockVector blockVector = new BlockVector(player.getLocation());
        BoundaryVisualizationEvent event = new BoundaryVisualizationEvent(
                player,
                claims.stream().map(claim -> new BoundaryDefinition(
                        claim,
                        claim.isAdminClaim() ? VisualizationType.ADMIN_CLAIM :  VisualizationType.CLAIM))
                        .collect(Collectors.toSet()));
        visualizeClaims(event, blockVector, height);
    }

    public static void visualizeClaims(@NotNull BoundaryVisualizationEvent event, BlockVector center, int height) {
        Bukkit.getPluginManager().callEvent(event);
        BoundaryVisualization visualization = event.getProvider().create(event.getPlayer().getWorld());
        for (BoundaryDefinition boundary : event.getBoundaries())
        {
            visualization.addElements(boundary.bounds(), center, height, boundary.type());
        }
        visualization.apply(event.getPlayer());
    }

}
