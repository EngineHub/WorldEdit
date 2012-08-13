/*
 * WorldEdit
 * Copyright (C) 2012 sk89q <http://www.sk89q.com> and contributors
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

package com.sk89q.worldedit.spout;

import com.sk89q.worldedit.LocalEntity;
import com.sk89q.worldedit.Location;
import org.spout.api.entity.Controller;
import org.spout.api.entity.Entity;
import org.spout.api.entity.controller.type.ControllerType;
import org.spout.api.geo.World;
import org.spout.api.geo.discrete.Point;

/**
 * @author zml2008
 */
public class SpoutEntity extends LocalEntity {
    private final ControllerType type;
    private final int entityId;

    public SpoutEntity(Location position, int id, Controller controller) {
        super(position);
        type = controller.getType();
        this.entityId = id;
    }

    public int getEntityId() {
        return entityId;
    }

    @Override
    public boolean spawn(Location loc) {
        World world = ((SpoutWorld) loc.getWorld()).getWorld();
        Point pos = SpoutUtil.toPoint(world, loc.getPosition());
        Controller controller = type.createController();
        if (controller == null) {
            return false;
        }
        Entity e = world.createAndSpawnEntity(pos, controller);

        if (e != null) {
            e.setPitch(loc.getPitch());
            e.setYaw(loc.getYaw());
            // TODO: Copy datatable info
            return true;
        }
        return false;
    }
}
