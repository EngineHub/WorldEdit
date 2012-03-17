package com.sk89q.worldedit.commands;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.BiomeType;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.regions.FlatRegion;
import com.sk89q.worldedit.regions.Region;

public class BiomeCommands {

    private WorldEdit we;

    public BiomeCommands(WorldEdit we) {
        this.we = we;
    }

    @Command(
        aliases = { "biomelist", "biomels" },
        usage = "[page]",
        desc = "Gets all biomes available.",
        min = 0,
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
        usage = "",
        flags = "ts",
        desc = "Get the biome of the targeted block.",
        help =
            "Get the biome of the block.\n" +
            "By default use the block you are currently in.\n" +
            "-t use the block you are looking at.\n" +
            "-s use all the blocks contained in your selection",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.biome.info")
    public void biomeInfo(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        if (args.hasFlag('s')) {
            LocalWorld world = player.getWorld();
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
        } else {
            Vector2D pos;
            if (args.hasFlag('t')) {
                Vector blockPosition = player.getBlockTrace(300);
                if (blockPosition == null) {
                    player.printError("No block in sight!");
                    return;
                }
                pos = blockPosition.toVector2D();
            } else {
                pos = player.getPosition().toVector2D();
            }
            BiomeType biome = player.getWorld().getBiome(pos);
            player.print("Biome: " + biome.getName());
        }
    }
}
