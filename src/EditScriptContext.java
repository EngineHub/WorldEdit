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
 * @author sk89q
 */
public class EditScriptContext {
    public EditScriptPlayer player;

    /**
     * Constructs the context. A player object has to be passed.
     * 
     * @param player
     */
    public EditScriptContext(EditScriptPlayer player) {
        this.player = player;
    }

    /**
     * Prints a message to the player.
     * 
     * @param msg
     */
    public void print(String msg) {
        player.print(msg);
    }

    /**
     * Prints an error message to the player.
     *
     * @param msg
     */
    public void error(String msg) {
        player.error(msg);
    }
}
