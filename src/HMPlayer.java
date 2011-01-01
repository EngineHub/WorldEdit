// $Id$
/*
 * WorldEditLibrary
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

import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditPlayer;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BlockType;

/**
 *
 * @author sk89q
 */
public class HMPlayer extends WorldEditPlayer {
    /**
     * Stores the player.
     */
    private Player player;

    /**
     * Construct the object.
     * 
     * @param player
     */
    public HMPlayer(Player player) {
        super();
        this.player = player;
    }

    /**
     * Move the player.
     *
     * @param pos
     */
    public void setPosition(Vector pos) {
        setPosition(pos, (float)getPitch(), (float)getYaw());
    }

    /**
     * Get the point of the block being looked at. May return null.
     *
     * @param range
     * @return point
     */
    public Vector getBlockTrace(int range) {
        HitBlox hitBlox = new HitBlox(player,range, 0.2);
        Block block = hitBlox.getTargetBlock();
        if (block == null) {
            return null;
        }
        return new Vector(block.getX(), block.getY(), block.getZ());
    }

    /**
     * Get the point of the block being looked at. May return null.
     *
     * @param range
     * @return point
     */
    public Vector getSolidBlockTrace(int range) {
        HitBlox hitBlox = new HitBlox(player,range, 0.2);
        Block block = null;

        while (hitBlox.getNextBlock() != null
                && BlockType.canPassThrough(hitBlox.getCurBlock().getType()));

        block = hitBlox.getCurBlock();

        if (block == null) {
            return null;
        }
        return new Vector(block.getX(), block.getY(), block.getZ());
    }

    /**
     * Get the ID of the item that the player is holding.
     *
     * @return
     */
    /**
     * Get the ID of the item that the player is holding.
     *
     * @return
     */
    public int getItemInHand() {
        return player.getItemInHand();
    }

    /**
     * Get the name of the player.
     *
     * @return String
     */
    public String getName() {
        return player.getName();
    }

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
    public double getPitch() {
        return player.getPitch();
    }

    /**
     * Get the player's position.
     *
     * @return point
     */
    public Vector getPosition() {
        return new Vector(player.getX(), player.getY(), player.getZ());
    }

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
    public double getYaw() {
        return player.getRotation();
    }

    /**
     * Gives the player an item.
     *
     * @param type
     * @param amt
     */
    /**
     * Gives the player an item.
     *
     * @param type
     * @param amt
     */
    public void giveItem(int type, int amt) {
        player.giveItem(type, amt);
    }

    /**
     * Pass through the wall that you are looking at.
     *
     * @param range
     * @return whether the player was pass through
     */
    public boolean passThroughForwardWall(int range) {
        boolean foundNext = false;
        int searchDist = 0;
        HitBlox hitBlox = new HitBlox(player,range, 0.2);
        Block block;
        while ((block = hitBlox.getNextBlock()) != null) {
            searchDist++;
            if (searchDist > 20) {
                return false;
            }
            if (block.getType() == 0) {
                if (foundNext) {
                    Vector v = new Vector(block.getX(), block.getY() - 1, block.getZ());
                    if (server.getBlockType(v) == 0) {
                        setPosition(v.add(0.5, 0, 0.5));
                        return true;
                    }
                }
            } else {
                foundNext = true;
            }
        }
        return false;
    }

    /**
     * Print a message.
     *
     * @param msg
     */
    public void printRaw(String msg) {
        player.sendMessage(msg);
    }

    /**
     * Print a WorldEdit message.
     *
     * @param msg
     */
    public void print(String msg) {
        player.sendMessage(Colors.LightPurple + msg);
    }

    /**
     * Print a WorldEdit error.
     *
     * @param msg
     */
    public void printError(String msg) {
        player.sendMessage(Colors.Rose + msg);
    }

    /**
     * Move the player.
     *
     * @param pos
     * @param pitch
     * @param yaw
     */
    public void setPosition(Vector pos, float pitch, float yaw) {
        Location loc = new Location();
        loc.x = pos.getX();
        loc.y = pos.getY();
        loc.z = pos.getZ();
        loc.rotX = (float) yaw;
        loc.rotY = (float) pitch;
        player.teleportTo(loc);
    }

    /**
     * Get a player's list of groups.
     * 
     * @return
     */
    public String[] getGroups() {
        return player.getGroups();
    }
    
    /**
     * Checks if a player has permission.
     * 
     * @return
     */
    public boolean hasPermission(String perm) {
        return player.canUseCommand("/" + perm);
    }
    
    /**
     * Get this player's block bag.
     */
    public BlockBag getInventoryBlockBag() {
        return new HMPlayerInventoryBlockBag(player);
    }

    /**
     * @return the player
     */
    public Player getPlayerObject() {
        return player;
    }
}
