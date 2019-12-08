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

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.Logging;
import com.sk89q.worldedit.command.util.WorldEditAsyncCommandBuilder;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.function.FlatRegionFunction;
import com.sk89q.worldedit.function.FlatRegionMaskingFilter;
import com.sk89q.worldedit.function.biome.BiomeReplace;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Mask2D;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.visitor.FlatRegionVisitor;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.FlatRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.Regions;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.component.PaginationBox;
import com.sk89q.worldedit.util.formatting.component.TextUtils;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeData;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.registry.BiomeRegistry;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.annotation.param.Switch;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sk89q.worldedit.command.util.Logging.LogMode.REGION;

/**
 * Implements biome-related commands such as "/biomelist".
 */
@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class BiomeCommands {

    /**
     * Create a new instance.
     */
    public BiomeCommands() {
    }

    @Command(
        name = "biomelist",
        aliases = { "biomels" },
        desc = "Gets all biomes available."
    )
    @CommandPermissions("worldedit.biome.list")
    public void biomeList(Actor actor,
                          @ArgFlag(name = 'p', desc = "Page number.", def = "1")
                              int page) {
        WorldEditAsyncCommandBuilder.createAndSendMessage(actor, () -> {
            BiomeRegistry biomeRegistry = WorldEdit.getInstance().getPlatformManager()
                    .queryCapability(Capability.GAME_HOOKS).getRegistries().getBiomeRegistry();

            PaginationBox paginationBox = PaginationBox.fromStrings("Available Biomes", "/biomelist -p %page%",
                    BiomeType.REGISTRY.values().stream()
                            .map(biomeType -> {
                                String id = biomeType.getId();
                                final BiomeData data = biomeRegistry.getData(biomeType);
                                if (data != null) {
                                    String name = data.getName();
                                    return id + " (" + name + ")";
                                } else {
                                    return id;
                                }
                            })
                            .collect(Collectors.toList()));
             return paginationBox.create(page);
        }, (Component) null);
    }

    @Command(
        name = "biomeinfo",
        desc = "Get the biome of the targeted block.",
        descFooter = "By default, uses all blocks in your selection."
    )
    @CommandPermissions("worldedit.biome.info")
    public void biomeInfo(Player player, LocalSession session,
                          @Switch(name = 't', desc = "Use the block you are looking at.")
                              boolean useLineOfSight,
                          @Switch(name = 'p', desc = "Use the block you are currently in.")
                              boolean usePosition) throws WorldEditException {
        BiomeRegistry biomeRegistry = WorldEdit.getInstance().getPlatformManager()
                .queryCapability(Capability.GAME_HOOKS).getRegistries().getBiomeRegistry();
        Set<BiomeType> biomes = new HashSet<>();
        String messageKey;

        if (useLineOfSight) {
            Location blockPosition = player.getBlockTrace(300);
            if (blockPosition == null) {
                player.printError(TranslatableComponent.of("worldedit.raytrace.noblock"));
                return;
            }

            BiomeType biome = player.getWorld().getBiome(blockPosition.toVector().toBlockPoint().toBlockVector2());
            biomes.add(biome);

            messageKey = "worldedit.biomeinfo.lineofsight";
        } else if (usePosition) {
            BiomeType biome = player.getWorld().getBiome(player.getLocation().toVector().toBlockPoint().toBlockVector2());
            biomes.add(biome);

            messageKey = "worldedit.biomeinfo.position";
        } else {
            World world = player.getWorld();
            Region region = session.getSelection(world);

            if (region instanceof FlatRegion) {
                for (BlockVector2 pt : ((FlatRegion) region).asFlatRegion()) {
                    biomes.add(world.getBiome(pt));
                }
            } else {
                for (BlockVector3 pt : region) {
                    biomes.add(world.getBiome(pt.toBlockVector2()));
                }
            }

            messageKey = "worldedit.biomeinfo.selection";
        }

        List<Component> components = biomes.stream().map(biome -> {
            BiomeData data = biomeRegistry.getData(biome);
            if (data != null) {
                return TextComponent.of(data.getName()).hoverEvent(HoverEvent.showText(TextComponent.of(biome.getId())));
            } else {
                return TextComponent.of(biome.getId());
            }
        }).collect(Collectors.toList());
        player.printInfo(TranslatableComponent.of(messageKey, TextUtils.join(components, TextComponent.of(", "))));
    }

    @Command(
        name = "/setbiome",
        desc = "Sets the biome of your current block or region.",
        descFooter = "By default, uses all the blocks in your selection"
    )
    @Logging(REGION)
    @CommandPermissions("worldedit.biome.set")
    public void setBiome(Player player, LocalSession session, EditSession editSession,
                         @Arg(desc = "Biome type.") BiomeType target,
                         @Switch(name = 'p', desc = "Use your current position")
                             boolean atPosition) throws WorldEditException {
        World world = player.getWorld();
        Region region;
        Mask mask = editSession.getMask();
        Mask2D mask2d = mask != null ? mask.toMask2D() : null;

        if (atPosition) {
            final BlockVector3 pos = player.getLocation().toVector().toBlockPoint();
            region = new CuboidRegion(pos, pos);
        } else {
            region = session.getSelection(world);
        }

        FlatRegionFunction replace = new BiomeReplace(editSession, target);
        if (mask2d != null) {
            replace = new FlatRegionMaskingFilter(mask2d, replace);
        }
        FlatRegionVisitor visitor = new FlatRegionVisitor(Regions.asFlatRegion(region), replace);
        Operations.completeLegacy(visitor);

        player.printInfo(TranslatableComponent.of(
                "worldedit.setbiome.changed",
                TextComponent.of(visitor.getAffected())
        ));
    }

}
