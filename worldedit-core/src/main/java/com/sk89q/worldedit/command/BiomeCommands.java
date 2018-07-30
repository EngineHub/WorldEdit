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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.minecraft.util.commands.Logging.LogMode.REGION;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Logging;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.function.FlatRegionFunction;
import com.sk89q.worldedit.function.FlatRegionMaskingFilter;
import com.sk89q.worldedit.function.biome.BiomeReplace;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Mask2D;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.visitor.FlatRegionVisitor;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.FlatRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.Regions;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.command.binding.Switch;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.biome.BiomeData;
import com.sk89q.worldedit.world.registry.BiomeRegistry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implements biome-related commands such as "/biomelist".
 */
public class BiomeCommands {

    private final WorldEdit worldEdit;

    /**
     * Create a new instance.
     *
     * @param worldEdit reference to WorldEdit
     */
    public BiomeCommands(WorldEdit worldEdit) {
        checkNotNull(worldEdit);
        this.worldEdit = worldEdit;
    }

    @Command(
        aliases = { "biomelist", "biomels" },
        usage = "[page]",
        desc = "Gets all biomes available.",
        max = 1
    )
    @CommandPermissions("worldedit.biome.list")
    public void biomeList(Player player, CommandContext args) throws WorldEditException {
        int page;
        int offset;
        int count = 0;

        if (args.argsLength() == 0 || (page = args.getInteger(0)) < 2) {
            page = 1;
            offset = 0;
        } else {
            offset = (page - 1) * 19;
        }

        BiomeRegistry biomeRegistry = WorldEdit.getInstance().getPlatformManager()
                .queryCapability(Capability.GAME_HOOKS).getRegistries().getBiomeRegistry();
        List<BaseBiome> biomes = biomeRegistry.getBiomes();
        int totalPages = biomes.size() / 19 + 1;
        player.print("Available Biomes (page " + page + "/" + totalPages + ") :");
        for (BaseBiome biome : biomes) {
            if (offset > 0) {
                offset--;
            } else {
                BiomeData data = biomeRegistry.getData(biome);
                if (data != null) {
                    player.print(" " + data.getName());
                    if (++count == 19) {
                        break;
                    }
                } else {
                    player.print(" <unknown #" + biome.getId() + ">");
                }
            }
        }
    }

    @Command(
        aliases = { "biomeinfo" },
        flags = "pt",
        desc = "Get the biome of the targeted block.",
        help =
            "Get the biome of the block.\n" +
            "By default use all the blocks contained in your selection.\n" +
            "-t use the block you are looking at.\n" +
            "-p use the block you are currently in",
        max = 0
    )
    @CommandPermissions("worldedit.biome.info")
    public void biomeInfo(Player player, LocalSession session, CommandContext args) throws WorldEditException {
        BiomeRegistry biomeRegistry = WorldEdit.getInstance().getPlatformManager()
                .queryCapability(Capability.GAME_HOOKS).getRegistries().getBiomeRegistry();
        Set<BaseBiome> biomes = new HashSet<>();
        String qualifier;

        if (args.hasFlag('t')) {
            Location blockPosition = player.getBlockTrace(300);
            if (blockPosition == null) {
                player.printError("No block in sight!");
                return;
            }

            BaseBiome biome = player.getWorld().getBiome(blockPosition.toVector().toVector2D());
            biomes.add(biome);

            qualifier = "at line of sight point";
        } else if (args.hasFlag('p')) {
            BaseBiome biome = player.getWorld().getBiome(player.getLocation().toVector().toVector2D());
            biomes.add(biome);

            qualifier = "at your position";
        } else {
            World world = player.getWorld();
            Region region = session.getSelection(world);

            if (region instanceof FlatRegion) {
                for (Vector2D pt : ((FlatRegion) region).asFlatRegion()) {
                    biomes.add(world.getBiome(pt));
                }
            } else {
                for (Vector pt : region) {
                    biomes.add(world.getBiome(pt.toVector2D()));
                }
            }

            qualifier = "in your selection";
        }

        player.print(biomes.size() != 1 ? "Biomes " + qualifier + ":" : "Biome " + qualifier + ":");
        for (BaseBiome biome : biomes) {
            BiomeData data = biomeRegistry.getData(biome);
            if (data != null) {
                player.print(" " + data.getName());
            } else {
                player.print(" <unknown #" + biome.getId() + ">");
            }
        }
    }

    @Command(
            aliases = { "/setbiome" },
            usage = "<biome>",
            flags = "p",
            desc = "Sets the biome of the player's current block or region.",
            help =
                    "Set the biome of the region.\n" +
                    "By default use all the blocks contained in your selection.\n" +
                    "-p use the block you are currently in"
    )
    @Logging(REGION)
    @CommandPermissions("worldedit.biome.set")
    public void setBiome(Player player, LocalSession session, EditSession editSession, BaseBiome target, @Switch('p') boolean atPosition) throws WorldEditException {
        World world = player.getWorld();
        Region region;
        Mask mask = editSession.getMask();
        Mask2D mask2d = mask != null ? mask.toMask2D() : null;

        if (atPosition) {
            region = new CuboidRegion(player.getLocation().toVector(), player.getLocation().toVector());
        } else {
            region = session.getSelection(world);
        }

        FlatRegionFunction replace = new BiomeReplace(editSession, target);
        if (mask2d != null) {
            replace = new FlatRegionMaskingFilter(mask2d, replace);
        }
        FlatRegionVisitor visitor = new FlatRegionVisitor(Regions.asFlatRegion(region), replace);
        Operations.completeLegacy(visitor);

        player.print("Biomes were changed in " + visitor.getAffected() + " columns. You may have to rejoin your game (or close and reopen your world) to see a change.");
    }

}
