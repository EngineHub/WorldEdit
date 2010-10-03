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

/**
 *
 * @author Albert
 */
public class ScriptPlayer {
    private Player player;

    public String name;
    public double pitch;
    public double yaw;
    public double x;
    public double y;
    public double z;
    public int blockX;
    public int blockY;
    public int blockZ;

    /**
     * Constructs the player instance.
     *
     * @param player
     */
    public ScriptPlayer(Player player) {
        this.player = player;
        name = player.getName();
        pitch = player.getPitch();
        yaw = player.getRotation();
        x = player.getX();
        y = player.getY();
        z = player.getZ();
        blockX = (int)Math.floor(player.getX());
        blockY = (int)Math.floor(player.getY());
        blockZ = (int)Math.floor(player.getZ());
    }

    /**
     * Prints a message to the player.
     *
     * @param msg
     */
    public void print(String msg) {
        player.sendMessage(msg);
    }

    /**
     * Prints an error message to the player.
     *
     * @param msg
     */
    public void error(String msg) {
        player.sendMessage(Colors.Rose + msg);
    }

    /**
     * Teleport the player to a position.
     *
     * @param x
     * @param y
     * @param z
     */
    public void teleporTo(double x, double y, double z) {
        Location loc = new Location();
        loc.x = x;
        loc.y = y;
        loc.z = z;
        loc.rotX = player.getRotation();
        loc.rotY = player.getPitch();
        player.teleportTo(loc);
    }

    /**
     * Teleport the player to a position with a certain rotation.
     *
     * @param x
     * @param y
     * @param z
     * @param pitch
     * @param yaw
     */
    public void teleporTo(double x, double y, double z, float pitch, float yaw) {
        Location loc = new Location();
        loc.x = x;
        loc.y = y;
        loc.z = z;
        loc.rotX = yaw;
        loc.rotY = pitch;
        player.teleportTo(loc);
    }
}
