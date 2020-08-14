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

package com.sk89q.worldedit.command.tool;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.registry.LegacyMapper;

import javax.annotation.Nullable;

/**
 * Looks up information about a block.
 */
public class QueryTool implements BlockTool {

    @Override
    public boolean canUse(Actor player) {
        return player.hasPermission("worldedit.tool.info");
    }

    @Override
    public boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session, Location clicked, @Nullable Direction face) {

        World world = (World) clicked.getExtent();
        EditSession editSession = session.createEditSession(player);
        BlockVector3 blockPoint = clicked.toVector().toBlockPoint();
        BaseBlock block = editSession.getFullBlock(blockPoint);

        TextComponent.Builder builder = TextComponent.builder();
        builder.append(TextComponent.of("@" + clicked.toVector().toBlockPoint() + ": ", TextColor.BLUE));
        builder.append(block.getBlockType().getRichName().color(TextColor.YELLOW));
        builder.append(TextComponent.of(" (" + block + ") ", TextColor.GRAY)
            .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TranslatableComponent.of("worldedit.tool.info.blockstate.hover"))));
        final int internalId = BlockStateIdAccess.getBlockStateId(block.toImmutableState());
        if (BlockStateIdAccess.isValidInternalId(internalId)) {
            builder.append(TextComponent.of(" (" + internalId + ") ", TextColor.DARK_GRAY)
                .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TranslatableComponent.of("worldedit.tool.info.internalid.hover"))));
        }
        final int[] legacy = LegacyMapper.getInstance().getLegacyFromBlock(block.toImmutableState());
        if (legacy != null) {
            builder.append(TextComponent.of(" (" + legacy[0] + ":" + legacy[1] + ") ", TextColor.DARK_GRAY)
                .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TranslatableComponent.of("worldedit.tool.info.legacy.hover"))));
        }

        builder.append(TextComponent.of(" (" + world.getBlockLightLevel(blockPoint) + "/"
            + world.getBlockLightLevel(blockPoint.add(0, 1, 0)) + ")", TextColor.WHITE)
            .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TranslatableComponent.of("worldedit.tool.info.light.hover"))));

        player.print(builder.build());

        return true;
    }

}
