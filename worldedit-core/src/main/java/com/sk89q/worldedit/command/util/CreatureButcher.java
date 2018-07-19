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

package com.sk89q.worldedit.command.util;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.worldedit.entity.metadata.EntityProperties;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.EntityFunction;

/**
 * The implementation of /butcher.
 */
public class CreatureButcher {

    final class Flags {
        @SuppressWarnings("PointlessBitwiseExpression")
        public static final int PETS = 1 << 0;
        public static final int NPCS = 1 << 1;
        public static final int ANIMALS = 1 << 2;
        public static final int GOLEMS = 1 << 3;
        public static final int AMBIENT = 1 << 4;
        public static final int TAGGED = 1 << 5;
        public static final int FRIENDLY = PETS | NPCS | ANIMALS | GOLEMS | AMBIENT | TAGGED;
        public static final int ARMOR_STAND = 1 << 6;
        public static final int WITH_LIGHTNING = 1 << 20;

        private Flags() {
        }
    }

    private final Actor player;
    public int flags = 0;

    public CreatureButcher(Actor player) {
        this.player = player;
    }

    public void or(int flag, boolean on) {
        if (on) flags |= flag;
    }

    public void or(int flag, boolean on, String permission) {
        or(flag, on);

        if ((flags & flag) != 0 && !player.hasPermission(permission)) {
            flags &= ~flag;
        }
    }

    public void fromCommand(CommandContext args) {
        or(Flags.FRIENDLY      , args.hasFlag('f')); // No permission check here. Flags will instead be filtered by the subsequent calls.
        or(Flags.PETS          , args.hasFlag('p'), "worldedit.butcher.pets");
        or(Flags.NPCS          , args.hasFlag('n'), "worldedit.butcher.npcs");
        or(Flags.GOLEMS        , args.hasFlag('g'), "worldedit.butcher.golems");
        or(Flags.ANIMALS       , args.hasFlag('a'), "worldedit.butcher.animals");
        or(Flags.AMBIENT       , args.hasFlag('b'), "worldedit.butcher.ambient");
        or(Flags.TAGGED        , args.hasFlag('t'), "worldedit.butcher.tagged");
        or(Flags.ARMOR_STAND   , args.hasFlag('r'), "worldedit.butcher.armorstands");

        or(Flags.WITH_LIGHTNING, args.hasFlag('l'), "worldedit.butcher.lightning");
    }

    public EntityFunction createFunction() {
        return entity -> {
            boolean killPets = (flags & Flags.PETS) != 0;
            boolean killNPCs = (flags & Flags.NPCS) != 0;
            boolean killAnimals = (flags & Flags.ANIMALS) != 0;
            boolean killGolems = (flags & Flags.GOLEMS) != 0;
            boolean killAmbient = (flags & Flags.AMBIENT) != 0;
            boolean killTagged = (flags & Flags.TAGGED) != 0;
            boolean killArmorStands = (flags & Flags.ARMOR_STAND) != 0;

            EntityProperties type = entity.getFacet(EntityProperties.class);

            if (type == null) {
                return false;
            }

            if (type.isPlayerDerived()) {
                return false;
            }

            if (!type.isLiving()) {
                return false;
            }

            if (!killAnimals && type.isAnimal()) {
                return false;
            }

            if (!killPets && type.isTamed()) {
                return false;
            }

            if (!killGolems && type.isGolem()) {
                return false;
            }

            if (!killNPCs && type.isNPC()) {
                return false;
            }

            if (!killAmbient && type.isAmbient()) {
                return false;
            }

            if (!killTagged && type.isTagged()) {
                return false;
            }

            if (!killArmorStands && type.isArmorStand()) {
                return false;
            }

            entity.remove();
            return true;
        };
    }

}
