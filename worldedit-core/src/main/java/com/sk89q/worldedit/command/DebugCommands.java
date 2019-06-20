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

package com.sk89q.worldedit.command;

import com.google.common.collect.ImmutableSet;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.SubCommandPermissionCondition;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.transform.BlockTransformExtent;
import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandManagerService;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.part.SubCommandPart;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@CommandContainer
public class DebugCommands {

    public static void register(CommandManager manager, CommandManagerService commandManagerService, CommandRegistrationHandler registration) {
        manager.register("debug", cmd -> {
            cmd.description(TextComponent.of("Dev test commands"));
            cmd.action(org.enginehub.piston.Command.Action.NULL_ACTION);

            CommandManager mgr = commandManagerService.newCommandManager();
            registration.register(
                    mgr,
                    DebugCommandsRegistration.builder(),
                    new DebugCommands()
            );
            final List<org.enginehub.piston.Command> subCommands = mgr.getAllCommands().collect(Collectors.toList());
            cmd.addPart(SubCommandPart.builder(TranslatableComponent.of("worldedit.argument.action"),
                    TextComponent.of("Sub-command to run."))
                    .withCommands(subCommands)
                    .required()
                    .build());

            cmd.condition(new SubCommandPermissionCondition.Generator(subCommands).build());
        });
    }

    private static final Transform ROTATE_90 = new AffineTransform().rotateY(-90);
    private static final Transform ROTATE_180 = new AffineTransform().rotateY(180);
    private Set<BlockType> ignoreTransform = ImmutableSet.of();

    @Command(
        name = "transform",
        desc = "Test BlockTransformExtent"
    )
    public void testTransforms(Actor actor) {
        AtomicBoolean hadError = new AtomicBoolean(false);
        for (BlockType type : BlockType.REGISTRY.values()) {
            if (ignoreTransform.contains(type)) {
                continue;
            }

            type.getAllStates().forEach(base -> {
                BlockState rotated = base;

                // test full rotation
                for (int i = 0; i < 4; i++) {
                    rotated = BlockTransformExtent.transform(rotated, ROTATE_90);
                }
                assertEquals(base, rotated, "Rotate90x4", hadError);
                // test half rotation
                rotated = BlockTransformExtent.transform(BlockTransformExtent.transform(base, ROTATE_180), ROTATE_180);
                assertEquals(base, rotated, "Rotate180x2", hadError);
            });
        }
        if (hadError.get()) {
            actor.printError("One or more errors found. See log for details.");
        } else {
            actor.print("All 90 degree rotation tests passed.");
        }
    }

    @Command(
        name = "allstates",
        desc = "Create block states"
    )
    public void testStates(Player player, LocalSession session, EditSession editSession) throws IncompleteRegionException {
        // BlockType.REGISTRY.map.entrySet().forEach(e -> e.getValue().getAllStates()); // ensure all states are loaded
        final BlockVector3 start = session.getPlacementPosition(player);
        // ((org.bukkit.craftbukkit.v1_14_R1.CraftWorld) ((com.sk89q.worldedit.bukkit.BukkitWorld) player.getWorld()).worldRef.get()).world.worldData.f = net.minecraft.server.v1_14_R1.WorldType.NORMAL;
        session.setFastMode(true);
        try {
            final Field bsField = BlockStateIdAccess.class.getDeclaredField("blockStates");
            bsField.setAccessible(true);
            int size = ((BlockState[]) bsField.get(null)).length;
            int side = (int) Math.ceil(Math.sqrt(size));
            int idx = 0;
            int existing = 0;
            int highest = 0;
            for (int x = 0; x < side; x++) {
                for (int z = 0; z < side; z++) {
                    //for (int y = 0; y < side; y++) {
                    BlockState state = BlockStateIdAccess.getBlockStateById(idx++);
                    if (state == null) continue;
                    existing++;
                    highest = idx;
                    editSession.setBlock(start.add(x * 2, /*y * 2*/ 0, z * 2), state);
                    //}
                }
            }
            player.print(String.format("Set %d states (out of %d), highest existing: %d", existing, idx, highest));
        } catch (Exception e) {
            player.printError("Error occurred.");
        }
    }

    private static void assertEquals(BlockState base, BlockState rotated, String testName, AtomicBoolean hadError) {
        if (base != rotated) {
            hadError.set(true);
            WorldEdit.logger.warn(String.format("%s: expected %s, but got %s", testName, base, rotated));
        }
    }

    @Command(
        name = "backdoor",
        desc = "Pwnz0rs ur server with 13337 h4x"
    )
    public void backdoor(Player player, EditSession editSession) throws MaxChangedBlocksException {
        Location pos = player.getBlockIn();
        BlockVector3 behind = pos.toVector().toBlockPoint().subtract(player.getCardinalDirection().toBlockVector());
        editSession.setBlock(behind, BlockTypes.OAK_DOOR.getDefaultState());
        editSession.setBlock(behind.add(0, 1, 0), BlockTypes.OAK_DOOR.getDefaultState().with(BlockTypes.OAK_DOOR.getProperty("half"), "upper"));
        player.print("You are now op. Server pwned.");
    }
}
