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

import com.sk89q.worldedit.blocks.BaseBlock;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

class SimpleState implements State {

    private Byte dataMask;
    private Map<String, SimpleStateValue> values;

    @Override
    public Map<String, SimpleStateValue> valueMap() {
        return Collections.unmodifiableMap(values);
    }

    @Nullable
    @Override
    public StateValue getValue(BaseBlock block) {
        for (StateValue value : values.values()) {
            if (value.isSet(block)) {
                return value;
            }
        }

        return null;
    }

    public SimpleStateValue addDirection(String name, SimpleStateValue state) {
        return this.values.put(name, state);
    }

    byte getDataMask() {
        return dataMask != null ? dataMask : 0xF;
    }

    @Override
    public boolean hasDirection() {
        for (SimpleStateValue value : values.values()) {
            if (value.getDirection() != null) {
                return true;
            }
        }

        return false;
    }

    void postDeserialization() {
        for (SimpleStateValue v : values.values()) {
            v.setState(this);
        }
    }

}
