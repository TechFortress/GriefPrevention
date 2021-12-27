package com.griefprevention.visualization;

import com.griefprevention.visualization.impl.FakeBlockElement;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import me.ryanhamshire.GriefPrevention.Visualization;
import me.ryanhamshire.GriefPrevention.VisualizationType;
import me.ryanhamshire.GriefPrevention.events.VisualizationEvent;
import com.griefprevention.util.BlockVector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class BoundaryVisualization extends Visualization
{

    public static final VisualizationProvider DEFAULT_PROVIDER = new com.griefprevention.visualization.impl.FakeBlockProvider();

    private final Collection<BoundaryElement> boundaries = new HashSet<>();
    private final @NotNull World world;

    protected BoundaryVisualization(@NotNull World world)
    {
        super();
        this.world = world;
    }

    protected void addElement(BoundaryElement element) {
        boundaries.add(element);
    }

    public void apply(@NotNull Player player)
    {
        PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());

        // If they have a visualization active, clear it first.
        if (playerData.currentVisualization != null)
        {
            revert(player);
        }

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
        return player != null && player.isOnline() && !boundaries.isEmpty() && Objects.equals(world, player.getWorld());
    }

    void applyVisuals(@NotNull Player player, @NotNull PlayerData playerData)
    {
        // Apply all visualization elements.
        boundaries.forEach(element -> element.apply(player, world));

        // Remember the visualization so it can be reverted.
        playerData.currentVisualization = this;

        // Schedule automatic reversion.
        scheduleRevert(player, playerData);
    }

    protected void scheduleRevert(@NotNull Player player, @NotNull PlayerData playerData)
    {
        GriefPrevention.instance.getServer().getScheduler().scheduleSyncDelayedTask(
                GriefPrevention.instance,
                () -> revert(player, playerData),
                20L * 60);
    }

    public void revert(@Nullable Player player, @NotNull PlayerData playerData)
    {
        // Player is not visualizing this visualization instance.
        if (playerData.currentVisualization != this) return;

        playerData.currentVisualization = null;

        if (!canVisualize(player))
        {
            return;
        }

        // Revert data as necessary for any sent elements.
        boundaries.forEach(element -> element.revert(player, world));
    }

    public static void apply(@NotNull Player player, @Nullable BoundaryVisualization visualization)
    {
        // If the visualization is null, revert existing visualizations.
        if (visualization == null)
        {
            revert(player);
            return;
        }

        // If the visualization is a modern visualization, apply it.
        visualization.apply(player);
    }

    /**
     * Revert a {@link Player Player's} existing {@code Visualization}, if any.
     *
     * @param player the {@code Player}
     */
    public static void revert(@NotNull Player player)
    {
        PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());

        if (!player.isOnline())
        {
            playerData.currentVisualization = null;
            return;
        }

        Visualization visualization = playerData.currentVisualization;

        if (visualization instanceof BoundaryVisualization boundaryVisualization) boundaryVisualization.revert(player, playerData);

        playerData.currentVisualization = null;
    }

    @Deprecated(forRemoval = true, since = "16.18")
    public static @Nullable BoundaryVisualization convert(@Nullable Visualization visualization)
    {
        if (visualization == null || visualization.elements.isEmpty()) return null;

        World world = visualization.elements.get(0).location.getWorld();
        if (world == null) return null;

        BoundaryVisualization boundaryVisualization = new com.griefprevention.visualization.impl.FakeBlockVisualization(world);
        visualization.elements.forEach(element -> boundaryVisualization.boundaries.add(
                new FakeBlockElement(new BlockVector(element.location), element.realBlock, element.visualizedBlock)));
        return boundaryVisualization;
    }

    public static @Nullable BoundaryVisualization fromClaim(
            @NotNull Claim claim,
            int height,
            @NotNull VisualizationType visualizationType,
            Location location)
    {
        // For individual claim visualization, always visualize top level claim.
        if (claim.parent != null) claim = claim.parent;

        // Create parent visualization in specified mode.
        BoundaryVisualization visualization = fromClaims(Set.of(claim), height, visualizationType, location);

        // Visualization for a single claim should never be null, but in case of a broken claim or odd provider, handle.
        if (visualization == null) return null;

        // Visualize children in subdivision mode
        BoundaryVisualization children = fromClaims(claim.children, height, VisualizationType.Subdivision, location);
        if (children != null) visualization.boundaries.addAll(children.boundaries);

        return visualization;
    }

    public static @Nullable BoundaryVisualization fromClaims(
            @NotNull Iterable<Claim> claims,
            int height,
            @NotNull VisualizationType visualizationType,
            Location location)
    {
        VisualizationProvider provider;
        RegisteredServiceProvider<VisualizationProvider> registration = Bukkit.getServicesManager().getRegistration(VisualizationProvider.class);

        if (registration != null)
        {
            provider = registration.getProvider();
        }
        else
        {
            provider = DEFAULT_PROVIDER;
        }

        return provider.of(claims, height, visualizationType, location);
    }

    /**
     * Helper method for clearing visualization while firing event.
     *
     * @param player the {@link Player} whose visualization is being cleared
     */
    public static void visualizeNothing(@NotNull Player player)
    {
        VisualizationEvent event = new VisualizationEvent(player, null, Set.of());
        visualizeClaims(event);
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
        BoundaryVisualization visualization = BoundaryVisualization.fromClaim(claim, height, type, player.getLocation());
        VisualizationEvent event = new VisualizationEvent(player, visualization, claim);
        visualizeClaims(event);
    }

    public static void visualizeNearbyClaims(
            @NotNull Player player,
            @NotNull Collection<Claim> claims,
            int height)
    {
        BoundaryVisualization visualization = BoundaryVisualization.fromClaims(claims, height, VisualizationType.Claim, player.getLocation());
        VisualizationEvent event = new VisualizationEvent(player, visualization, claims, true);
        visualizeClaims(event);
    }

    public static void visualizeClaims(@NotNull VisualizationEvent event) {
        Bukkit.getPluginManager().callEvent(event);
        BoundaryVisualization.apply(event.getPlayer(), event.getVisualization());
    }

}
