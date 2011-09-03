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

import java.io.File;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldedit.cui.CUIEvent;
import com.sk89q.worldedit.util.TargetBlock;

/**
 *
 * @author sk89q
 */
public abstract class LocalPlayer {    
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
        return item == ItemType.IRON_PICK.getID()
                || item == ItemType.WOOD_PICKAXE.getID()
                || item == ItemType.STONE_PICKAXE.getID()
                || item == ItemType.DIAMOND_PICKAXE.getID()
                || item == ItemType.GOLD_PICKAXE.getID();
    }

    /**
     * Find a position for the player to stand that is not inside a block.
     * Blocks above the player will be iteratively tested until there is
     * a series of two free blocks. The player will be teleported to
     * that free position.
     *
     * @param searchPos search position
     */
    public void findFreePosition(WorldVector searchPos) {
        LocalWorld world = searchPos.getWorld();
        int x = searchPos.getBlockX();
        int y = Math.max(0, searchPos.getBlockY());
        int origY = y;
        int z = searchPos.getBlockZ();

        byte free = 0;

        while (y <= 129) {
            if (BlockType.canPassThrough(world.getBlockType(new Vector(x, y, z)))) {
                ++free;
            } else {
                free = 0;
            }

            if (free == 2) {
                if (y - 1 != origY) {
                    setPosition(new Vector(x + 0.5, y - 1, z + 0.5));
                }

                return;
            }

            ++y;
        }
    }
    
    /**
     * Set the player on the ground.
     * 
     * @param searchPos
     */
    public void setOnGround(WorldVector searchPos) {
        LocalWorld world = searchPos.getWorld();
        int x = searchPos.getBlockX();
        int y = Math.max(0, searchPos.getBlockY());
        int z = searchPos.getBlockZ();

        while (y >= 0) {
            if (!BlockType.canPassThrough(world.getBlockType(new Vector(x, y, z)))) {
                setPosition(new Vector(x + 0.5, y + 1, z + 0.5));
                return;
            }

            --y;
        }
    }

    /**
     * Find a position for the player to stand that is not inside a block.
     * Blocks above the player will be iteratively tested until there is
     * a series of two free blocks. The player will be teleported to
     * that free position.
     */
    public void findFreePosition() {
        findFreePosition(getBlockIn());
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
            if (BlockType.canPassThrough(world.getBlockType(new Vector(x, y, z)))) {
                ++free;
            } else {
                free = 0;
            }

            if (free == 2) {
                ++spots;
                if (spots == 2) {
                    int type = world.getBlockType(new Vector(x, y - 2, z));
                    
                    // Don't get put in lava!
                    if (type == BlockID.LAVA || type == BlockID.STATIONARY_LAVA) {
                        return false;
                    }

                    setPosition(new Vector(x + 0.5, y - 1, z + 0.5));
                    return true;
                }
            }

            ++y;
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
            if (BlockType.canPassThrough(world.getBlockType(new Vector(x, y, z)))) {
                ++free;
            } else {
                free = 0;
            }

            if (free == 2) {
                // So we've found a spot, but we have to drop the player
                // lightly and also check to see if there's something to
                // stand upon
                while (y >= 0) {
                    int type = world.getBlockType(new Vector(x, y, z));

                    // Don't want to end up in lava
                    if (type != BlockID.AIR && type != BlockID.LAVA && type != BlockID.STATIONARY_LAVA) {
                        // Found a block!
                        setPosition(new Vector(x + 0.5, y + 1, z + 0.5));
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
        if (world.getBlockType(new Vector(x, y, z)) != 0) {
            return false;
        }

        while (y <= 127) {
            // Found a ceiling!
            if (!BlockType.canPassThrough(world.getBlockType(new Vector(x, y, z)))) {
                int platformY = Math.max(initialY, y - 3 - clearance);
                world.setBlockType(new Vector(x, platformY, z),
                        BlockID.GLASS);
                setPosition(new Vector(x + 0.5, platformY + 1, z + 0.5));
                return true;
            }

            ++y;
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
            if (!BlockType.canPassThrough(world.getBlockType(new Vector(x, y, z)))) {
                break; // Hit something
            } else if (y > maxY + 1) {
                break;
            } else if (y == maxY + 1) {
                world.setBlockType(new Vector(x, y - 2, z),
                        BlockID.GLASS);
                setPosition(new Vector(x + 0.5, y - 1, z + 0.5));
                return true;
            }

            ++y;
        }

        return false;
    }

    /**
     * Get the point of the block that is being stood in.
     *
     * @return point
     */
    public WorldVector getBlockIn() {
        WorldVector pos = getPosition();
        return WorldVector.toBlockPoint(pos.getWorld(), pos.getX(),
                pos.getY(), pos.getZ());
    }

    /**
     * Get the point of the block that is being stood upon.
     *
     * @return point
     */
    public WorldVector getBlockOn() {
        WorldVector pos = getPosition();
        return WorldVector.toBlockPoint(pos.getWorld(), pos.getX(),
                pos.getY() - 1, pos.getZ());
    }

    /**
     * Get the point of the block being looked at. May return null.
     * Will return the farthest away air block if useLastBlock is true and no other block is found.
     * 
     * @param range
     * @param useLastBlock
     * @return point
     */
    public WorldVector getBlockTrace(int range, boolean useLastBlock) {
        TargetBlock tb = new TargetBlock(this, range, 0.2);
        return (useLastBlock ? tb.getAnyTargetBlock() : tb.getTargetBlock());
    }
    
    /**
     * Get the point of the block being looked at. May return null.
     *
     * @param range
     * @return point
     */
    public WorldVector getBlockTrace(int range) {
        return getBlockTrace(range, false);
    }

    /**
     * Get the point of the block being looked at. May return null.
     *
     * @param range
     * @return point
     */
    public WorldVector getSolidBlockTrace(int range) {
        TargetBlock tb = new TargetBlock(this, range, 0.2);
        return tb.getSolidTargetBlock();
    }

    /**
     * Get the player's cardinal direction (N, W, NW, etc.). May return null.
     *
     * @return
     */
    public PlayerDirection getCardinalDirection() {
        if (getPitch() > 67.5)
            return PlayerDirection.DOWN;
        if (getPitch() < -67.5)
            return PlayerDirection.UP;

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
    private static PlayerDirection getDirection(double rot) {
        if (0 <= rot && rot < 22.5) {
            return PlayerDirection.NORTH;
        } else if (22.5 <= rot && rot < 67.5) {
            return PlayerDirection.NORTH_EAST;
        } else if (67.5 <= rot && rot < 112.5) {
            return PlayerDirection.EAST;
        } else if (112.5 <= rot && rot < 157.5) {
            return PlayerDirection.SOUTH_EAST;
        } else if (157.5 <= rot && rot < 202.5) {
            return PlayerDirection.SOUTH;
        } else if (202.5 <= rot && rot < 247.5) {
            return PlayerDirection.SOUTH_WEST;
        } else if (247.5 <= rot && rot < 292.5) {
            return PlayerDirection.WEST;
        } else if (292.5 <= rot && rot < 337.5) {
            return PlayerDirection.NORTH_WEST;
        } else if (337.5 <= rot && rot < 360.0) {
            return PlayerDirection.NORTH;
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
    public boolean passThroughForwardWall(int range) {
        int searchDist = 0;
        TargetBlock hitBlox = new TargetBlock(this, range, 0.2);
        LocalWorld world = getPosition().getWorld();
        BlockWorldVector block;
        boolean firstBlock = true;
        int freeToFind = 2;
        boolean inFree = false;
        
        while ((block = hitBlox.getNextBlock()) != null) {
            boolean free = BlockType.canPassThrough(world.getBlockType(block));
            
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
    public abstract void printDebug(String msg);

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
     * Open a file open dialog.
     * 
     * @param extensions null to allow all
     * @return
     */
    public File openFileOpenDialog(String[] extensions) {
        printError("File dialogs are not supported in your environment.");
        return null;
    }
    
    /**
     * Open a file save dialog.
     * 
     * @param extensions null to allow all
     * @return
     */
    public File openFileSaveDialog(String[] extensions) {
        printError("File dialogs are not supported in your environment.");
        return null;
    }
    
    /**
     * Returns true if the player can destroy bedrock.
     * 
     * @return
     */
    public boolean canDestroyBedrock() {
        return hasPermission("worldedit.override.bedrock");
    }
    
    /**
     * Send a CUI event.
     * 
     * @param event
     */
    public void dispatchCUIEvent(CUIEvent event) {
    }
    
    /**
     * Send the CUI handshake.
     */
    public void dispatchCUIHandshake() {
    }

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

    public void checkPermission(String permission) throws WorldEditPermissionException {
       if (!hasPermission(permission)) {
           throw new WorldEditPermissionException();
       }
    }
}
