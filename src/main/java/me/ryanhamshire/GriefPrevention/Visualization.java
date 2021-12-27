/*
    GriefPrevention Server Plugin for Minecraft
    Copyright (C) 2012 Ryan Hamshire

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.ryanhamshire.GriefPrevention;

import com.griefprevention.visualization.BoundaryVisualization;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

//represents a visualization sent to a player
//FEATURE: to show players visually where claim boundaries are, we send them fake block change packets
//the result is that those players see new blocks, but the world hasn't been changed.  other players can't see the new blocks, either.
@Deprecated(forRemoval = true, since = "16.18")
public class Visualization
{
    @Deprecated(forRemoval = true, since = "16.18")
    public ArrayList<VisualizationElement> elements = new ArrayList<>();

    @Deprecated(forRemoval = true, since = "16.18")
    public Visualization() {}

    /**
     * Send a visualization to a {@link Player}.
     *
     * @deprecated Use {@link BoundaryVisualization#apply(Player)} to apply a {@code Visualization}
     * @param player the {@code Player}
     * @param visualization the {@code Visualization}
     */
    @Deprecated(forRemoval = true, since = "16.18")
    public static void Apply(Player player, Visualization visualization)
    {
        if (player != null) BoundaryVisualization.apply(player, visualization);
    }

    /**
     * Revert the active visualization for a {@link Player}.
     *
     * @deprecated Use {@link BoundaryVisualization#revert(Player)} to revert a {@code Visualization}
     * @param player the {@code Player}
     */
    @Deprecated(forRemoval = true, since = "16.18")
    public static void Revert(Player player)
    {
        if (player != null) BoundaryVisualization.revert(player);
    }

    @Deprecated(forRemoval = true, since = "16.18")
    //convenience method to build a visualization from a claim
    //visualizationType determines the style (gold blocks, silver, red, diamond, etc)
    public static @Nullable Visualization FromClaim(Claim claim, int height, VisualizationType visualizationType, Location locality)
    {
        return BoundaryVisualization.fromClaim(claim, height, visualizationType, locality);
    }

    @Deprecated(forRemoval = true, since = "16.18")
    public static void apply(@NotNull Player player, @Nullable Visualization visualization)
    {
        // If the visualization is null, revert existing visualizations.
        if (visualization == null)
        {
            BoundaryVisualization.revert(player);
            return;
        }

        // If the visualization is a modern visualization, let it apply itself.
        if (visualization instanceof BoundaryVisualization boundaryVisualization)
        {
            boundaryVisualization.apply(player);
        }
        // If the visualization is a legacy visualization, convert and handle.
        else
        {
            BoundaryVisualization boundaryVisualization = BoundaryVisualization.convert(visualization);
            if (boundaryVisualization == null) BoundaryVisualization.revert(player);
            else boundaryVisualization.apply(player);
        }
    }

    //adds a claim's visualization to the current visualization
    //handy for combining several visualizations together, as when visualization a top level claim with several subdivisions inside
    //locality is a performance consideration.  only create visualization blocks for around 100 blocks of the locality

    @Deprecated(forRemoval = true, since = "16.18")
    public void addClaimElements(Claim claim, int height, VisualizationType visualizationType, Location locality)
    {
        BlockData cornerBlockData;
        BlockData accentBlockData;

        if (visualizationType == VisualizationType.Claim)
        {
            cornerBlockData = Material.GLOWSTONE.createBlockData();
            accentBlockData = Material.GOLD_BLOCK.createBlockData();
        }
        else if (visualizationType == VisualizationType.AdminClaim)
        {
            cornerBlockData = Material.GLOWSTONE.createBlockData();
            accentBlockData = Material.PUMPKIN.createBlockData();
        }
        else if (visualizationType == VisualizationType.Subdivision)
        {
            cornerBlockData = Material.IRON_BLOCK.createBlockData();
            accentBlockData = Material.WHITE_WOOL.createBlockData();
        }
        else if (visualizationType == VisualizationType.RestoreNature)
        {
            cornerBlockData = Material.DIAMOND_BLOCK.createBlockData();
            accentBlockData = Material.DIAMOND_BLOCK.createBlockData();
        }
        else
        {
            cornerBlockData = Material.REDSTONE_ORE.createBlockData();
            ((Lightable) cornerBlockData).setLit(true);
            accentBlockData = Material.NETHERRACK.createBlockData();
        }

        addClaimElements(claim.getLesserBoundaryCorner(), claim.getGreaterBoundaryCorner(), locality, height, cornerBlockData, accentBlockData, 10);
    }

    @Deprecated(forRemoval = true, since = "16.18")
    //adds a general claim cuboid (represented by min and max) visualization to the current visualization
    public void addClaimElements(Location min, Location max, Location locality, int height, BlockData cornerBlockData, BlockData accentBlockData, int STEP) {
        World world = min.getWorld();
        boolean waterIsTransparent = locality.getBlock().getType() == Material.WATER;

        int smallx = min.getBlockX();
        int smallz = min.getBlockZ();
        int bigx = max.getBlockX();
        int bigz = max.getBlockZ();

        ArrayList<VisualizationElement> newElements = new ArrayList<>();

        //initialize visualization elements without Y values and real data
        //that will be added later for only the visualization elements within visualization range

        //locality
        int minx = locality.getBlockX() - 75;
        int minz = locality.getBlockZ() - 75;
        int maxx = locality.getBlockX() + 75;
        int maxz = locality.getBlockZ() + 75;

        //top line
        newElements.add(new VisualizationElement(new Location(world, smallx, 0, bigz), cornerBlockData, Material.AIR.createBlockData()));
        newElements.add(new VisualizationElement(new Location(world, smallx + 1, 0, bigz), accentBlockData, Material.AIR.createBlockData()));
        for (int x = smallx + STEP; x < bigx - STEP / 2; x += STEP)
        {
            if (x > minx && x < maxx)
                newElements.add(new VisualizationElement(new Location(world, x, 0, bigz), accentBlockData, Material.AIR.createBlockData()));
        }
        newElements.add(new VisualizationElement(new Location(world, bigx - 1, 0, bigz), accentBlockData, Material.AIR.createBlockData()));

        //bottom line
        newElements.add(new VisualizationElement(new Location(world, smallx + 1, 0, smallz), accentBlockData, Material.AIR.createBlockData()));
        for (int x = smallx + STEP; x < bigx - STEP / 2; x += STEP)
        {
            if (x > minx && x < maxx)
                newElements.add(new VisualizationElement(new Location(world, x, 0, smallz), accentBlockData, Material.AIR.createBlockData()));
        }
        newElements.add(new VisualizationElement(new Location(world, bigx - 1, 0, smallz), accentBlockData, Material.AIR.createBlockData()));

        //left line
        newElements.add(new VisualizationElement(new Location(world, smallx, 0, smallz), cornerBlockData, Material.AIR.createBlockData()));
        newElements.add(new VisualizationElement(new Location(world, smallx, 0, smallz + 1), accentBlockData, Material.AIR.createBlockData()));
        for (int z = smallz + STEP; z < bigz - STEP / 2; z += STEP)
        {
            if (z > minz && z < maxz)
                newElements.add(new VisualizationElement(new Location(world, smallx, 0, z), accentBlockData, Material.AIR.createBlockData()));
        }
        newElements.add(new VisualizationElement(new Location(world, smallx, 0, bigz - 1), accentBlockData, Material.AIR.createBlockData()));

        //right line
        newElements.add(new VisualizationElement(new Location(world, bigx, 0, smallz), cornerBlockData, Material.AIR.createBlockData()));
        newElements.add(new VisualizationElement(new Location(world, bigx, 0, smallz + 1), accentBlockData, Material.AIR.createBlockData()));
        for (int z = smallz + STEP; z < bigz - STEP / 2; z += STEP)
        {
            if (z > minz && z < maxz)
                newElements.add(new VisualizationElement(new Location(world, bigx, 0, z), accentBlockData, Material.AIR.createBlockData()));
        }
        newElements.add(new VisualizationElement(new Location(world, bigx, 0, bigz - 1), accentBlockData, Material.AIR.createBlockData()));
        newElements.add(new VisualizationElement(new Location(world, bigx, 0, bigz), cornerBlockData, Material.AIR.createBlockData()));

        //remove any out of range elements
        this.removeElementsOutOfRange(newElements, minx, minz, maxx, maxz);

        //remove any elements outside the claim
        BoundingBox box = BoundingBox.of(min, max);
        for (int i = 0; i < newElements.size(); i++)
        {
            VisualizationElement element = newElements.get(i);
            if (!containsIncludingIgnoringHeight(box, element.location.toVector()))
            {
                newElements.remove(i--);
            }
        }

        //set Y values and real block information for any remaining visualization blocks
        for (VisualizationElement element : newElements)
        {
            Location tempLocation = element.location;
            element.location = getVisibleLocation(tempLocation.getWorld(), tempLocation.getBlockX(), height, tempLocation.getBlockZ(), waterIsTransparent);
            height = element.location.getBlockY();
            element.realBlock = element.location.getBlock().getBlockData();
        }

        this.elements.addAll(newElements);
    }

    private boolean containsIncludingIgnoringHeight(BoundingBox box, Vector vector) {
        return vector.getBlockX() >= box.getMinX()
                && vector.getBlockX() <= box.getMaxX()
                && vector.getBlockZ() >= box.getMinZ()
                && vector.getBlockZ() <= box.getMaxZ();
    }

    //removes any elements which are out of visualization range
    private void removeElementsOutOfRange(ArrayList<VisualizationElement> elements, int minx, int minz, int maxx, int maxz)
    {
        for (int i = 0; i < elements.size(); i++)
        {
            Location location = elements.get(i).location;
            if (location.getX() < minx || location.getX() > maxx || location.getZ() < minz || location.getZ() > maxz)
            {
                elements.remove(i--);
            }
        }
    }

    //finds a block the player can probably see.  this is how visualizations "cling" to the ground or ceiling
    private static Location getVisibleLocation(World world, int x, int y, int z, boolean waterIsTransparent)
    {
        Block block = world.getBlockAt(x, y, z);
        BlockFace direction = (isTransparent(block, waterIsTransparent)) ? BlockFace.DOWN : BlockFace.UP;

        while (block.getY() >= 1 &&
                block.getY() < world.getMaxHeight() - 1 &&
                (!isTransparent(block.getRelative(BlockFace.UP), waterIsTransparent) || isTransparent(block, waterIsTransparent)))
        {
            block = block.getRelative(direction);
        }

        return block.getLocation();
    }

    //helper method for above.  allows visualization blocks to sit underneath partly transparent blocks like grass and fence
    private static boolean isTransparent(Block block, boolean waterIsTransparent)
    {
        Material blockMaterial = block.getType();

        // Custom per-material definitions.
        switch (blockMaterial)
        {
            case WATER:
                return waterIsTransparent;
            case SNOW:
                return false;
        }

        if (blockMaterial.isAir()
                || Tag.FENCES.isTagged(blockMaterial)
                || Tag.FENCE_GATES.isTagged(blockMaterial)
                || Tag.SIGNS.isTagged(blockMaterial)
                || Tag.WALL_SIGNS.isTagged(blockMaterial))
            return true;

        return block.getType().isTransparent();
    }

    @Deprecated(forRemoval = true, since = "16.18")
    public static Visualization fromClaims(Iterable<Claim> claims, int height, VisualizationType type, Location locality)
    {
        return BoundaryVisualization.fromClaims(claims, height, type, locality);
    }

}
