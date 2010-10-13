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

/**
 *
 * @author sk89q
 */
public class WorldEditPlayer {
    private Player player;

    /**
     * Construct a WorldEditPlayer.
     * 
     * @param player
     */
    public WorldEditPlayer(Player player) {
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
     * Find a position for the player to stand that is not inside a block.
     * Blocks above the player will be iteratively tested until there is
     * a series of two free blocks. The player will be teleported to
     * that free position.
     */
    public void findFreePosition() {
        int x = (int)Math.floor(player.getX());
        int y = (int)Math.floor(player.getY());
        int origY = y;
        int z = (int)Math.floor(player.getZ());

        byte free = 0;

        while (y <= 129) {
            if (etc.getServer().getBlockIdAt(x, y, z) == 0) {
                free++;
            } else {
                free = 0;
            }

            if (free == 2) {
                if (y - 1 != origY) {
                    Location loc = new Location();
                    loc.x = x + 0.5;
                    loc.y = y - 1;
                    loc.z = z + 0.5;
                    loc.rotX = player.getRotation();
                    loc.rotY = player.getPitch();
                    player.teleportTo(loc);
                    return;
                }
            }

            y++;
        }
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
