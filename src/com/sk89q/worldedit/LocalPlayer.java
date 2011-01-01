// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit;

import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BlockType;

/**
 *
 * @author sk89q
 */
public abstract class LocalPlayer {    
    /**
     * Directions.
     */
    public enum DIRECTION {
        NORTH,
        NORTH_EAST,
        EAST,
        SOUTH_EAST,
        SOUTH,
        SOUTH_WEST,
        WEST,
        NORTH_WEST
    };

    /**
     * Server.
     */
    protected ServerInterface server;
    
    /**
     * Construct the object.
     * 
     * @param server
     */
    protected LocalPlayer(ServerInterface server) {
        this.server = server;
    }
    
    /**
     * Returns true if the player is holding a pick axe.
     *
     * @return whether a pick axe is held
     */
    public boolean isHoldingPickAxe() {
        int item = getItemInHand();
        return item == 257 || item == 270 || item == 274 || item == 278
                || item == 285;
    }

    /**
     * Find a position for the player to stand that is not inside a block.
     * Blocks above the player will be iteratively tested until there is
     * a series of two free blocks. The player will be teleported to
     * that free position.
     *
     * @param searchPos search position
     */
    public void findFreePosition(LocalWorld world, Vector searchPos) {
        int x = searchPos.getBlockX();
        int y = Math.max(0, searchPos.getBlockY());
        int origY = y;
        int z = searchPos.getBlockZ();

        byte free = 0;

        while (y <= 129) {
            if (BlockType.canPassThrough(server.getBlockType(world,
                    new Vector(x, y, z)))) {
                free++;
            } else {
                free = 0;
            }

            if (free == 2) {
                if (y - 1 != origY) {
                    setPosition(new Vector(x + 0.5, y - 1, z + 0.5));
                }

                return;
            }

            y++;
        }
    }

    /**
     * Find a position for the player to stand that is not inside a block.
     * Blocks above the player will be iteratively tested until there is
     * a series of two free blocks. The player will be teleported to
     * that free position.
     */
    public void findFreePosition() {
        findFreePosition(getPosition().getWorld(), getBlockIn());
    }

    /**
     * Go up one level to the next free space above.
     *
     * @return true if a spot was found
     */
    public boolean ascendLevel() {
        Vector pos = getBlockIn();
        int x = pos.getBlockX();
        int y = Math.max(0, pos.getBlockY());
        int z = pos.getBlockZ();
        LocalWorld world = getPosition().getWorld();

        byte free = 0;
        byte spots = 0;

        while (y <= 129) {
            if (BlockType.canPassThrough(server.getBlockType(world, new Vector(x, y, z)))) {
                free++;
            } else {
                free = 0;
            }

            if (free == 2) {
                spots++;
                if (spots == 2) {
                    int type = server.getBlockType(world, new Vector(x, y - 2, z));
                    
                    // Don't get put in lava!
                    if (type == 10 || type == 11) {
                        return false;
                    }

                    setPosition(new Vector(x + 0.5, y - 1, z + 0.5));
                    return true;
                }
            }

            y++;
        }

        return false;
    }

    /**
     * Go up one level to the next free space above.
     *
     * @return true if a spot was found
     */
    public boolean descendLevel() {
        Vector pos = getBlockIn();
        int x = pos.getBlockX();
        int y = Math.max(0, pos.getBlockY() - 1);
        int z = pos.getBlockZ();
        LocalWorld world = getPosition().getWorld();

        byte free = 0;

        while (y >= 1) {
            if (BlockType.canPassThrough(server.getBlockType(world, new Vector(x, y, z)))) {
                free++;
            } else {
                free = 0;
            }

            if (free == 2) {
                // So we've found a spot, but we have to drop the player
                // lightly and also check to see if there's something to
                // stand upon
                while (y >= 0) {
                    int type = server.getBlockType(world, new Vector(x, y, z));

                    // Don't want to end up in lava
                    if (type != 0 && type != 10 && type != 11) {
                        // Found a block!
                        setPosition(new Vector(x + 0.5, y + 1, z + 0.5));
                        return true;
                    }
                    
                    y--;
                }

                return false;
            }

            y--;
        }

        return false;
    }

    /**
     * Ascend to the ceiling above.
     * 
     * @param clearance
     * @return whether the player was moved
     */
    public boolean ascendToCeiling(int clearance) {
        Vector pos = getBlockIn();
        int x = pos.getBlockX();
        int initialY = Math.max(0, pos.getBlockY());
        int y = Math.max(0, pos.getBlockY() + 2);
        int z = pos.getBlockZ();
        LocalWorld world = getPosition().getWorld();
        
        // No free space above
        if (server.getBlockType(world, new Vector(x, y, z)) != 0) {
            return false;
        }

        while (y <= 127) {
            // Found a ceiling!
            if (!BlockType.canPassThrough(server.getBlockType(world, new Vector(x, y, z)))) {
                int platformY = Math.max(initialY, y - 3 - clearance);
                server.setBlockType(world, new Vector(x, platformY, z),
                        BlockType.GLASS.getID());
                setPosition(new Vector(x + 0.5, platformY + 1, z + 0.5));
                return true;
            }

            y++;
        }

        return false;
    }

    /**
     * Just go up.
     *
     * @param distance
     * @return whether the player was moved
     */
    public boolean ascendUpwards(int distance) {
        Vector pos = getBlockIn();
        int x = pos.getBlockX();
        int initialY = Math.max(0, pos.getBlockY());
        int y = Math.max(0, pos.getBlockY() + 1);
        int z = pos.getBlockZ();
        int maxY = Math.min(128, initialY + distance);
        LocalWorld world = getPosition().getWorld();

        while (y <= 129) {
            if (!BlockType.canPassThrough(server.getBlockType(world, new Vector(x, y, z)))) {
                break; // Hit something
            } else if (y > maxY + 1) {
                break;
            } else if (y == maxY + 1) {
                server.setBlockType(world, new Vector(x, y - 2, z),
                        BlockType.GLASS.getID());
                setPosition(new Vector(x + 0.5, y - 1, z + 0.5));
                return true;
            }

            y++;
        }

        return false;
    }

    /**
     * Get the point of the block that is being stood in.
     *
     * @return point
     */
    public WorldVector getBlockIn() {
        return getPosition();
    }

    /**
     * Get the point of the block that is being stood upon.
     *
     * @return point
     */
    public WorldVector getBlockOn() {
        WorldVector pos = getPosition();
        return new WorldVector(pos.getWorld(), pos.subtract(0, 1, 0));
    }

    /**
     * Get the point of the block being looked at. May return null.
     *
     * @param range
     * @return point
     */
    public abstract WorldVector getBlockTrace(int range);

    /**
     * Get the point of the block being looked at. May return null.
     *
     * @param range
     * @return point
     */
    public abstract WorldVector getSolidBlockTrace(int range);

    /**
     * Get the player's cardinal direction (N, W, NW, etc.). May return null.
     *
     * @return
     */
    public LocalPlayer.DIRECTION getCardinalDirection() {
        // From hey0's code
        double rot = (getYaw() - 90) % 360;
        if (rot < 0) {
            rot += 360.0;
        }
        return getDirection(rot);
    }

    /**
     * Returns direction according to rotation. May return null.
     * 
     * @param rot
     * @return
     */
    private static LocalPlayer.DIRECTION getDirection(double rot) {
        if (0 <= rot && rot < 22.5) {
            return LocalPlayer.DIRECTION.NORTH;
        } else if (22.5 <= rot && rot < 67.5) {
            return LocalPlayer.DIRECTION.NORTH_EAST;
        } else if (67.5 <= rot && rot < 112.5) {
            return LocalPlayer.DIRECTION.EAST;
        } else if (112.5 <= rot && rot < 157.5) {
            return LocalPlayer.DIRECTION.SOUTH_EAST;
        } else if (157.5 <= rot && rot < 202.5) {
            return LocalPlayer.DIRECTION.SOUTH;
        } else if (202.5 <= rot && rot < 247.5) {
            return LocalPlayer.DIRECTION.SOUTH_WEST;
        } else if (247.5 <= rot && rot < 292.5) {
            return LocalPlayer.DIRECTION.WEST;
        } else if (292.5 <= rot && rot < 337.5) {
            return LocalPlayer.DIRECTION.NORTH_WEST;
        } else if (337.5 <= rot && rot < 360.0) {
            return LocalPlayer.DIRECTION.NORTH;
        } else {
            return null;
        }
    }

    /**
     * Get the ID of the item that the player is holding.
     *
     * @return
     */
    public abstract int getItemInHand();

    /**
     * Get the name of the player.
     *
     * @return String
     */
    public abstract String getName();

    /**
     * Get the player's position.
     *
     * @return point
     */
    public abstract WorldVector getPosition();

    /**
     * Get the player's world.
     *
     * @return point
     */
    public abstract LocalWorld getWorld();

    /**
     * Get the player's view pitch.
     *
     * @return pitch
     */
    /**
     * Get the player's view pitch.
     *
     * @return pitch
     */
    public abstract double getPitch();

    /**
     * Get the player's view yaw.
     *
     * @return yaw
     */
    /**
     * Get the player's view yaw.
     *
     * @return yaw
     */
    public abstract double getYaw();

    /**
     * Gives the player an item.
     *
     * @param type
     * @param amt
     */
    public abstract void giveItem(int type, int amt);

    /**
     * Pass through the wall that you are looking at.
     *
     * @param range
     * @return whether the player was pass through
     */
    public abstract boolean passThroughForwardWall(int range);

    /**
     * Print a message.
     *
     * @param msg
     */
    public abstract void printRaw(String msg);

    /**
     * Print a WorldEdit message.
     *
     * @param msg
     */
    public abstract void print(String msg);

    /**
     * Print a WorldEdit error.
     *
     * @param msg
     */
    public abstract void printError(String msg);

    /**
     * Move the player.
     *
     * @param pos
     * @param pitch
     * @param yaw
     */
    public abstract void setPosition(Vector pos, float pitch, float yaw);

    /**
     * Move the player.
     *
     * @param pos
     */
    public void setPosition(Vector pos) {
        setPosition(pos, (float)getPitch(), (float)getYaw());
    }

    /**
     * Get a player's list of groups.
     * 
     * @return
     */
    public abstract String[] getGroups();
    
    /**
     * Get this player's block bag.
     * 
     * @return
     */
    public abstract BlockBag getInventoryBlockBag();
    
    /**
     * Checks if a player has permission.
     * 
     * @param perm
     * @return
     */
    public abstract boolean hasPermission(String perm);

    /**
     * Returns true if equal.
     *
     * @param other
     * @return whether the other object is equivalent
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof LocalPlayer)) {
            return false;
        }
        LocalPlayer other2 = (LocalPlayer)other;
        return other2.getName().equals(getName());
    }

    /**
     * Gets the hash code.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
