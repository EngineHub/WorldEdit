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

package com.sk89q.worldedit.spout;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.LocalEntity;
import com.sk89q.worldedit.Location;
import org.spout.api.component.Component;
import org.spout.api.datatable.ManagedHashMap;
import org.spout.api.entity.Entity;
import org.spout.api.geo.LoadOption;
import org.spout.api.geo.World;
import org.spout.api.geo.discrete.Point;
import org.spout.api.math.QuaternionMath;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * @author zml2008
 */
public class SpoutEntity extends LocalEntity {
    private final byte[] datatableBytes;
    private final List<Class<? extends Component>> components;
    private final int entityId;

    public SpoutEntity(Location position, int id, Collection<Class<? extends Component>> components, ManagedHashMap datatable) {
        super(position);
        this.components = Lists.newArrayList(components);
        this.datatableBytes = datatable.serialize();
        this.entityId = id;
    }

    public int getEntityId() {
        return entityId;
    }

    @Override
    public boolean spawn(Location loc) {
        World world = ((SpoutWorld) loc.getWorld()).getWorld();
        Point pos = SpoutUtil.toPoint(world, loc.getPosition());
        Class<? extends Component> mainComponent = null;
        if (components.size() > 0) {
            mainComponent = components.get(0);
        }
        if (mainComponent == null) {
            return false;
        }
        Entity e = world.createAndSpawnEntity(pos, mainComponent, LoadOption.LOAD_ONLY); // Blocks should already be pasted by time entitieos are brought in

        if (e != null) {
            e.getScene().setRotation(QuaternionMath.rotation(loc.getPitch(), loc.getYaw(), 0));
            for (Class<? extends Component> clazz : Iterables.skip(components, 1)) {
                e.add(clazz);
            }
            try {
                e.getData().deserialize(datatableBytes, true);
            } catch (IOException e1) {
                return false;
            }
            return true;
        }
        return false;
    }
}
