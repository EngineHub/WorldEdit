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

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.ServerInterface;

/**
 *
 * @author sk89q
 */
public class HmodWorldEditPlayer extends WorldEditPlayer {
    private Player player;

    /**
     * Construct a WorldEditPlayer.
     *
     * @param player
     */
    public HmodWorldEditPlayer(Player player) {
        super();
        this.player = player;
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
     * Get the point of the block that is being stood upon.
     *
     * @return point
     */
    public Vector getBlockOn() {
        return Vector.toBlockPoint(player.getX(), player.getY() - 1, player.getZ());
    }

    /**
     * Get the point of the block that is being stood in.
     *
     * @return point
     */
    public Vector getBlockIn() {
        return Vector.toBlockPoint(player.getX(), player.getY(), player.getZ());
    }

    /**
     * Get the point of the block being looked at. May return null.
     *
     * @param range
     * @return point
     */
    public Vector getBlockTrace(int range) {
        HitBlox hitBlox = new HitBlox(player, range, 0.2);
        Block block = hitBlox.getTargetBlock();
        if (block == null) {
            return null;
        }
        return new Vector(block.getX(), block.getY(), block.getZ());
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
        
        HitBlox hitBlox = new HitBlox(player, range, 0.2);
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
     * Get the player's position.
     *
     * @return point
     */
    public Vector getPosition() {
        return new Vector(player.getX(), player.getY(), player.getZ());
    }

    /**
     * Get the player's view pitch.
     *
     * @return pitch
     */
    public double getPitch() {
        return player.getPitch();
    }

    /**
     * Get the player's view yaw.
     *
     * @return yaw
     */
    public double getYaw() {
        return player.getRotation();
    }

    /**
     * Get the ID of the item that the player is holding.
     *
     * @return
     */
    public int getItemInHand() {
        return player.getItemInHand();
    }

    /**
     * Get the player's cardinal direction (N, W, NW, etc.).
     *
     * @return
     */
    public String getCardinalDirection() {
        // From hey0's code
        double rot = (getYaw() - 90) % 360;
        if (rot < 0) {
            rot += 360.0;
        }

        return etc.getCompassPointForDirection(rot).toLowerCase();
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
        loc.rotX = (float)yaw;
        loc.rotY = (float)pitch;
        player.teleportTo(loc);
    }

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
     * Returns true if equal.
     *
     * @param other
     * @return whether the other object is equivalent
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof WorldEditPlayer)) {
            return false;
        }
        WorldEditPlayer other2 = (WorldEditPlayer)other;
        return other2.getName().equals(player.getName());
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
