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

package com.sk89q.worldedit.world.registry;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;

class SimpleStateValue implements StateValue {

    private SimpleState state;
    private Byte data;
    private Vector direction;

    void setState(SimpleState state) {
        this.state = state;
    }

    @Override
    public boolean isSet(BaseBlock block) {
        return data != null && (block.getData() & state.getDataMask()) == data;
    }

    @Override
    public boolean set(BaseBlock block) {
        if (data != null) {
            block.setData((block.getData() & ~state.getDataMask()) | data);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Vector getDirection() {
        return direction;
    }

}
