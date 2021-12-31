package com.griefprevention.visualization;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import me.ryanhamshire.GriefPrevention.events.BoundaryVisualizationEvent;
import com.griefprevention.util.IntVector;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BoundaryVisualization
{

    public static final VisualizationProvider DEFAULT_PROVIDER = new com.griefprevention.visualization.impl.FakeBlockProvider();

    // TODO -> Collection<Boundary> and -> BoundaryDisplaySomething? Not all visualizations will need per-block, CUI etc.
    private final Collection<BoundaryElement> elements = new HashSet<>();
    protected final @NotNull World world;
    protected final @NotNull IntVector visualizeFrom;
    protected final int height;

    protected BoundaryVisualization(@NotNull World world, @NotNull IntVector visualizeFrom, int height)
    {
        this.world = world;
        this.visualizeFrom = visualizeFrom;
        this.height = height;
    }

    protected final void addElement(BoundaryElement element) {
        elements.add(element);
    }

    protected abstract void addElements(@NotNull BoundingBox bounds, @NotNull VisualizationType type);

    protected void scheduleApply(@NotNull Player player)
    {
        PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());

        // If they have a visualization active, clear it first.
        playerData.setVisibleBoundaries(null);

        // If they are online and in the same world as the visualization, display the visualization next tick.
        if (canVisualize(player))
        {
            GriefPrevention.instance.getServer().getScheduler().scheduleSyncDelayedTask(
                    GriefPrevention.instance,
                    () -> apply(player, playerData),
                    1L);
        }
    }

    @Contract("null -> false")
    protected boolean canVisualize(@Nullable Player player) {
        return player != null && player.isOnline() && !elements.isEmpty() && Objects.equals(world, player.getWorld());
    }

    protected void apply(@NotNull Player player, @NotNull PlayerData playerData)
    {
        // Remember the visualization so it can be reverted.
        playerData.setVisibleBoundaries(this);

        // Apply all visualization elements.
        elements.forEach(element -> element.apply(player, world));

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

    public static void visualizeArea(
            @NotNull Player player,
            @NotNull BoundingBox boundingBox,
            @NotNull VisualizationType type) {
        BoundaryVisualizationEvent event = new BoundaryVisualizationEvent(player,
                Set.of(new Boundary(boundingBox, type)),
                player.getEyeLocation().getBlockY());
        BoundaryVisualization.callAndVisualize(event);
    }

    public static void visualizeClaim(
            @NotNull Player player,
            @NotNull Claim claim,
            @NotNull VisualizationType type)
    {
        visualizeClaim(player, claim, type, player.getEyeLocation().getBlockY());
    }

    public static void visualizeClaim(
            @NotNull Player player,
            @NotNull Claim claim,
            @NotNull VisualizationType type,
            @NotNull Block block)
    {
        visualizeClaim(player, claim, type, block.getY());
    }

    private static void visualizeClaim(
            @NotNull Player player,
            @NotNull Claim claim,
            @NotNull VisualizationType type,
            int height)
    {
        BoundaryVisualizationEvent event = new BoundaryVisualizationEvent(player, defineBoundaries(claim, type), height);
        callAndVisualize(event);
    }

    private static Collection<Boundary> defineBoundaries(Claim claim, VisualizationType type)
    {
        if (type == VisualizationType.CLAIM && claim.isAdminClaim()) type = VisualizationType.ADMIN_CLAIM;

        return Stream.concat(
                Stream.of(new Boundary(claim, type)),
                claim.children.stream().map(child -> new Boundary(child, VisualizationType.SUBDIVISION)))
                .collect(Collectors.toSet());
    }

    public static void visualizeNearbyClaims(
            @NotNull Player player,
            @NotNull Collection<Claim> claims,
            int height)
    {
        BoundaryVisualizationEvent event = new BoundaryVisualizationEvent(
                player,
                claims.stream().map(claim -> new Boundary(
                        claim,
                        claim.isAdminClaim() ? VisualizationType.ADMIN_CLAIM :  VisualizationType.CLAIM))
                        .collect(Collectors.toSet()),
                height);
        callAndVisualize(event);
    }

    public static void callAndVisualize(@NotNull BoundaryVisualizationEvent event) {
        Bukkit.getPluginManager().callEvent(event);
        BoundaryVisualization visualization = event.getProvider().create(event.getPlayer().getWorld(), event.getCenter(), event.getHeight());
        for (Boundary boundary : event.getBoundaries())
        {
            visualization.addElements(boundary.bounds(), boundary.type());
        }
        visualization.scheduleApply(event.getPlayer());
    }

}
