/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.internal.expression;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

public class SlotTable {

    private final Map<String, LocalSlot> slots = new HashMap<>();

    public Set<String> keySet() {
        return slots.keySet();
    }

    public void putSlot(String name, LocalSlot slot) {
        slots.put(name, slot);
    }

    public boolean containsSlot(String name) {
        return slots.containsKey(name);
    }

    public Optional<LocalSlot.Variable> initVariable(String name) {
        slots.computeIfAbsent(name, n -> new LocalSlot.Variable(0));
        return getVariable(name);
    }

    public Optional<LocalSlot> getSlot(String name) {
        return Optional.ofNullable(slots.get(name));
    }

    public Optional<LocalSlot.Variable> getVariable(String name) {
        return getSlot(name)
            .filter(LocalSlot.Variable.class::isInstance)
            .map(LocalSlot.Variable.class::cast);
    }

    public OptionalDouble getSlotValue(String name) {
        LocalSlot slot = slots.get(name);
        return slot == null ? OptionalDouble.empty() : OptionalDouble.of(slot.getValue());
    }

}
