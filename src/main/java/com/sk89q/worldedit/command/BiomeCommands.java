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

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Logging;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.masks.BiomeTypeMask;
import com.sk89q.worldedit.masks.InvertedMask;
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.regions.FlatRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.sk89q.minecraft.util.commands.Logging.LogMode.REGION;

public class BiomeCommands {

    private WorldEdit we;

    public BiomeCommands(WorldEdit we) {
        this.we = we;
    }

    @Command(
        aliases = { "biomelist", "biomels" },
        usage = "[page]",
        desc = "Gets all biomes available.",
        max = 1
    )
    @CommandPermissions("worldedit.biome.list")
    public void biomeList(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        int page;
        int offset;
        int count = 0;

        if (args.argsLength() == 0 || (page = args.getInteger(0)) < 2) {
            page = 1;
            offset = 0;
        } else {
            offset = (page - 1) * 19;
        }

        List<BiomeType> biomes = we.getServer().getBiomes().all();
        int totalPages = biomes.size() / 19 + 1;
        player.print("Available Biomes (page " + page + "/" + totalPages + ") :");
        for (BiomeType biome : biomes) {
            if (offset > 0) {
                offset--;
            } else {
                player.print(" " + biome.getName());
                if (++count == 19) {
                    break;
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
    public void biomeInfo(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        if (args.hasFlag('t')) {
            Vector blockPosition = player.getBlockTrace(300);
            if (blockPosition == null) {
                player.printError("No block in sight!");
                return;
            }

            BiomeType biome = player.getWorld().getBiome(blockPosition.toVector2D());
            player.print("Biome: " + biome.getName());
        } else if (args.hasFlag('p')) {
            BiomeType biome = player.getWorld().getBiome(player.getPosition().toVector2D());
            player.print("Biome: " + biome.getName());
        } else {
            World world = player.getWorld();
            Region region = session.getSelection(world);
            Set<BiomeType> biomes = new HashSet<BiomeType>();

            if (region instanceof FlatRegion) {
                for (Vector2D pt : ((FlatRegion) region).asFlatRegion()) {
                    biomes.add(world.getBiome(pt));
                }
            } else {
                for (Vector pt : region) {
                    biomes.add(world.getBiome(pt.toVector2D()));
                }
            }

            player.print("Biomes:");
            for (BiomeType biome : biomes) {
                player.print(" " + biome.getName());
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
                            "-p use the block you are currently in",
            min = 1,
            max = 1
    )
    @Logging(REGION)
    @CommandPermissions("worldedit.biome.set")
    public void setBiome(CommandContext args, LocalSession session, LocalPlayer player,
                          EditSession editSession) throws WorldEditException {

        final BiomeType target = we.getServer().getBiomes().get(args.getString(0));
        if (target == null) {
            player.printError("Biome '" + args.getString(0) + "' does not exist!");
            return;
        }

        Mask mask = editSession.getMask();
        BiomeTypeMask biomeMask = null;
        boolean inverted = false;
        if (mask instanceof BiomeTypeMask) {
            biomeMask = (BiomeTypeMask) mask;
        } else if (mask instanceof InvertedMask && ((InvertedMask) mask).getInvertedMask() instanceof BiomeTypeMask) {
            inverted = true;
            biomeMask = (BiomeTypeMask) ((InvertedMask) mask).getInvertedMask();
        }

        if (args.hasFlag('p')) {
            Vector2D pos = player.getPosition().toVector2D();
            if (biomeMask == null || (biomeMask.matches2D(editSession, pos) ^ inverted)) {
                player.getWorld().setBiome(pos, target);
                player.print("Biome changed to " + target.getName() + " at your current location.");
            } else {
                player.print("Your global mask doesn't match this biome. Type //gmask to disable it.");
            }
        } else {
            int affected = 0;
            World world = player.getWorld();
            Region region = session.getSelection(world);

            if (region instanceof FlatRegion) {
                for (Vector2D pt : ((FlatRegion) region).asFlatRegion()) {
                    if (biomeMask == null || (biomeMask.matches2D(editSession, pt) ^ inverted)) {
                        world.setBiome(pt, target);
                        ++affected;
                    }
                }
            } else {
                HashSet<Long> alreadyVisited = new HashSet<Long>();
                for (Vector pt : region) {
                    if (!alreadyVisited.contains((long)pt.getBlockX() << 32 | pt.getBlockZ())) {
                        alreadyVisited.add(((long)pt.getBlockX() << 32 | pt.getBlockZ()));
                        if (biomeMask == null || (biomeMask.matches(editSession, pt) ^ inverted)) {
                            world.setBiome(pt.toVector2D(), target);
                            ++affected;
                        }
                    }
                }
            }

            player.print("Biome changed to " + target.getName() + ". " + affected + " columns affected.");
        }
    }
}
