/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.extension.platform;

import com.sk89q.worldedit.NotABlockException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.TargetBlock;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;

import java.io.File;

/**
 * An abstract implementation of both a {@link Actor} and a {@link Player}
 * that is intended for implementations of WorldEdit to use to wrap
 * players that make use of WorldEdit.
 */
public abstract class AbstractPlayerActor implements Actor, Player, Cloneable {

    @Override
    public final Extent getExtent() {
        return getWorld();
    }

    /**
     * Returns direction according to rotation. May return null.
     *
     * @param rot yaw
     * @return the direction
     */
    private static Direction getDirection(double rot) {
        if (0 <= rot && rot < 22.5) {
            return Direction.SOUTH;
        } else if (22.5 <= rot && rot < 67.5) {
            return Direction.SOUTHWEST;
        } else if (67.5 <= rot && rot < 112.5) {
            return Direction.WEST;
        } else if (112.5 <= rot && rot < 157.5) {
            return Direction.NORTHWEST;
        } else if (157.5 <= rot && rot < 202.5) {
            return Direction.NORTH;
        } else if (202.5 <= rot && rot < 247.5) {
            return Direction.NORTHEAST;
        } else if (247.5 <= rot && rot < 292.5) {
            return Direction.EAST;
        } else if (292.5 <= rot && rot < 337.5) {
            return Direction.SOUTHEAST;
        } else if (337.5 <= rot && rot < 360.0) {
            return Direction.SOUTH;
        } else {
            return null;
        }
    }

    @Override
    public boolean isHoldingPickAxe() {
        ItemType item = getItemInHand(HandSide.MAIN_HAND).getType();
        return item == ItemTypes.IRON_PICKAXE
                || item == ItemTypes.WOODEN_PICKAXE
                || item == ItemTypes.STONE_PICKAXE
                || item == ItemTypes.DIAMOND_PICKAXE
                || item == ItemTypes.GOLDEN_PICKAXE;
    }

    @Override
    public void findFreePosition(Location searchPos) {
        Extent world = searchPos.getExtent();
        int x = searchPos.getBlockX();
        int y = Math.max(0, searchPos.getBlockY());
        int origY = y;
        int z = searchPos.getBlockZ();

        byte free = 0;

        while (y <= world.getMaximumPoint().getBlockY() + 2) {
            if (!world.getBlock(BlockVector3.at(x, y, z)).getBlockType().getMaterial().isMovementBlocker()) {
                ++free;
            } else {
                free = 0;
            }

            if (free == 2) {
                if (y - 1 != origY) {
                    setPosition(Vector3.at(x + 0.5, y - 2 + 1, z + 0.5));
                }

                return;
            }

            ++y;
        }
    }

    @Override
    public void setOnGround(Location searchPos) {
        Extent world = searchPos.getExtent();
        int x = searchPos.getBlockX();
        int y = Math.max(0, searchPos.getBlockY());
        int z = searchPos.getBlockZ();

        while (y >= 0) {
            final BlockVector3 pos = BlockVector3.at(x, y, z);
            final BlockState id = world.getBlock(pos);
            if (id.getBlockType().getMaterial().isMovementBlocker()) {
                setPosition(Vector3.at(x + 0.5, y + 1, z + 0.5));
                return;
            }

            --y;
        }
    }

    @Override
    public void findFreePosition() {
        findFreePosition(getBlockIn());
    }

    @Override
    public boolean ascendLevel() {
        final Location pos = getBlockIn();
        final int x = pos.getBlockX();
        int y = Math.max(0, pos.getBlockY());
        final int z = pos.getBlockZ();
        final Extent world = pos.getExtent();

        byte free = 0;
        byte spots = 0;

        while (y <= world.getMaximumPoint().getY() + 2) {
            if (!world.getBlock(BlockVector3.at(x, y, z)).getBlockType().getMaterial().isMovementBlocker()) {
                ++free;
            } else {
                free = 0;
            }

            if (free == 2) {
                ++spots;
                if (spots == 2) {
                    final BlockVector3 platform = BlockVector3.at(x, y - 2, z);
                    final BlockState block = world.getBlock(platform);
                    final com.sk89q.worldedit.world.block.BlockType type = block.getBlockType();

                    // Don't get put in lava!
                    if (type == BlockTypes.LAVA) {
                        return false;
                    }

                    setPosition(platform.toVector3().add(0.5, 1, 0.5));
                    return true;
                }
            }

            ++y;
        }

        return false;
    }

    @Override
    public boolean descendLevel() {
        final Location pos = getBlockIn();
        final int x = pos.getBlockX();
        int y = Math.max(0, pos.getBlockY() - 1);
        final int z = pos.getBlockZ();
        final Extent world = pos.getExtent();

        byte free = 0;

        while (y >= 1) {
            if (!world.getBlock(BlockVector3.at(x, y, z)).getBlockType().getMaterial().isMovementBlocker()) {
                ++free;
            } else {
                free = 0;
            }

            if (free == 2) {
                // So we've found a spot, but we have to drop the player
                // lightly and also check to see if there's something to
                // stand upon
                while (y >= 0) {
                    final BlockVector3 platform = BlockVector3.at(x, y, z);
                    final BlockState block = world.getBlock(platform);
                    final BlockType type = block.getBlockType();

                    // Don't want to end up in lava
                    if (!type.getMaterial().isAir() && type != BlockTypes.LAVA) {
                        // Found a block!
                        setPosition(platform.toVector3().add(0.5, 1, 0.5));
                        return true;
                    }

                    --y;
                }

                return false;
            }

            --y;
        }

        return false;
    }

    @Override
    public boolean ascendToCeiling(int clearance) {
        return ascendToCeiling(clearance, true);
    }

    @Override
    public boolean ascendToCeiling(int clearance, boolean alwaysGlass) {
        Location pos = getBlockIn();
        int x = pos.getBlockX();
        int initialY = Math.max(0, pos.getBlockY());
        int y = Math.max(0, pos.getBlockY() + 2);
        int z = pos.getBlockZ();
        Extent world = getLocation().getExtent();

        // No free space above
        if (!world.getBlock(BlockVector3.at(x, y, z)).getBlockType().getMaterial().isAir()) {
            return false;
        }

        while (y <= world.getMaximumPoint().getY()) {
            // Found a ceiling!
            if (world.getBlock(BlockVector3.at(x, y, z)).getBlockType().getMaterial().isMovementBlocker()) {
                int platformY = Math.max(initialY, y - 3 - clearance);
                floatAt(x, platformY + 1, z, alwaysGlass);
                return true;
            }

            ++y;
        }

        return false;
    }

    @Override
    public boolean ascendUpwards(int distance) {
        return ascendUpwards(distance, true);
    }

    @Override
    public boolean ascendUpwards(int distance, boolean alwaysGlass) {
        final Location pos = getBlockIn();
        final int x = pos.getBlockX();
        final int initialY = Math.max(0, pos.getBlockY());
        int y = Math.max(0, pos.getBlockY() + 1);
        final int z = pos.getBlockZ();
        final int maxY = Math.min(getWorld().getMaxY() + 1, initialY + distance);
        final Extent world = getLocation().getExtent();

        while (y <= world.getMaximumPoint().getY() + 2) {
            if (world.getBlock(BlockVector3.at(x, y, z)).getBlockType().getMaterial().isMovementBlocker()) {
                break; // Hit something
            } else if (y > maxY + 1) {
                break;
            } else if (y == maxY + 1) {
                floatAt(x, y - 1, z, alwaysGlass);
                return true;
            }

            ++y;
        }

        return false;
    }

    @Override
    public void floatAt(int x, int y, int z, boolean alwaysGlass) {
        try {
            BlockVector3 spot = BlockVector3.at(x, y - 1, z);
            if (!getLocation().getExtent().getBlock(spot).getBlockType().getMaterial().isMovementBlocker()) {
                getLocation().getExtent().setBlock(spot, BlockTypes.GLASS.getDefaultState());
            }
        } catch (WorldEditException e) {
            e.printStackTrace();
        }
        setPosition(Vector3.at(x + 0.5, y, z + 0.5));
    }

    @Override
    public Location getBlockIn() {
        return getLocation().setPosition(getLocation().toVector().floor());
    }

    @Override
    public Location getBlockOn() {
        return getLocation().setPosition(getLocation().setY(getLocation().getY() - 1).toVector().floor());
    }

    @Override
    public Location getBlockTrace(int range, boolean useLastBlock) {
        TargetBlock tb = new TargetBlock(this, range, 0.2);
        return (useLastBlock ? tb.getAnyTargetBlock() : tb.getTargetBlock());
    }

    @Override
    public Location getBlockTraceFace(int range, boolean useLastBlock) {
        TargetBlock tb = new TargetBlock(this, range, 0.2);
        return (useLastBlock ? tb.getAnyTargetBlockFace() : tb.getTargetBlockFace());
    }

    @Override
    public Location getBlockTrace(int range) {
        return getBlockTrace(range, false);
    }

    @Override
    public Location getSolidBlockTrace(int range) {
        TargetBlock tb = new TargetBlock(this, range, 0.2);
        return tb.getSolidTargetBlock();
    }

    @Override
    public Direction getCardinalDirection() {
        return getCardinalDirection(0);
    }

    @Override
    public Direction getCardinalDirection(int yawOffset) {
        if (getLocation().getPitch() > 67.5) {
            return Direction.DOWN;
        }
        if (getLocation().getPitch() < -67.5) {
            return Direction.UP;
        }

        // From hey0's code
        double rot = (getLocation().getYaw() + yawOffset) % 360; //let's use real yaw now
        if (rot < 0) {
            rot += 360.0;
        }
        return getDirection(rot);
    }

    @Override
    public BaseBlock getBlockInHand(HandSide handSide) throws WorldEditException {
        final ItemType typeId = getItemInHand(handSide).getType();
        if (typeId.hasBlockType()) {
            return typeId.getBlockType().getDefaultState().toBaseBlock();
        } else {
            throw new NotABlockException();
        }
    }

    /**
     * Get the player's view yaw.
     *
     * @return yaw
     */

    @Override
    public boolean passThroughForwardWall(int range) {
        int searchDist = 0;
        TargetBlock hitBlox = new TargetBlock(this, range, 0.2);
        Extent world = getLocation().getExtent();
        Location block;
        boolean firstBlock = true;
        int freeToFind = 2;
        boolean inFree = false;

        while ((block = hitBlox.getNextBlock()) != null) {
            boolean free = !world.getBlock(block.toVector().toBlockPoint()).getBlockType().getMaterial().isMovementBlocker();

            if (firstBlock) {
                firstBlock = false;

                if (!free) {
                    --freeToFind;
                    continue;
                }
            }

            ++searchDist;
            if (searchDist > 20) {
                return false;
            }

            if (inFree != free) {
                if (free) {
                    --freeToFind;
                }
            }

            if (freeToFind == 0) {
                setOnGround(block);
                return true;
            }

            inFree = free;
        }

        return false;
    }

    @Override
    public void setPosition(Vector3 pos) {
        setPosition(pos, getLocation().getPitch(), getLocation().getYaw());
    }

    @Override
    public File openFileOpenDialog(String[] extensions) {
        printError("File dialogs are not supported in your environment.");
        return null;
    }

    @Override
    public File openFileSaveDialog(String[] extensions) {
        printError("File dialogs are not supported in your environment.");
        return null;
    }

    @Override
    public boolean canDestroyBedrock() {
        return hasPermission("worldedit.override.bedrock");
    }

    @Override
    public void dispatchCUIEvent(CUIEvent event) {
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Player)) {
            return false;
        }
        Player other2 = (Player) other;
        return other2.getName().equals(getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public void checkPermission(String permission) throws AuthorizationException {
        if (!hasPermission(permission)) {
            throw new AuthorizationException();
        }
    }

    @Override
    public boolean isPlayer() {
        return true;
    }

    @Override
    public GameMode getGameMode() {
        return GameModes.SURVIVAL;
    }

    @Override
    public void setGameMode(GameMode gameMode) {

    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Not supported");
    }

    @Override
    public boolean remove() {
        return false;
    }

    @Override
    public <B extends BlockStateHolder<B>> void sendFakeBlock(BlockVector3 pos, B block) {

    }
}
