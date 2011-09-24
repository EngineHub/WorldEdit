// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

package com.sk89q.worldedit.cui;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;

public class SelectionPoint2DEvent implements CUIEvent {

    protected int id;
    protected int blockx;
    protected int blockz;
    protected int area;

    public SelectionPoint2DEvent(int id, Vector2D pos, int area) {
        this.id = id;
        this.blockx = pos.getBlockX();
        this.blockz = pos.getBlockZ();
        this.area = area;
    }

    public SelectionPoint2DEvent(int id, Vector pos, int area) {
        this.id = id;
        this.blockx = pos.getBlockX();
        this.blockz = pos.getBlockZ();
        this.area = area;
    }

    public String getTypeId() {
        return "p2";
    }

    public String[] getParameters() {
        return new String[] {
                    String.valueOf(id),
                    String.valueOf(blockx),
                    String.valueOf(blockz),
                    String.valueOf(area)
                };
    }

}
