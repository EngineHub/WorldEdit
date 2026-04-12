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

package com.sk89q.worldedit.coremc.internal;

import com.mojang.brigadier.CommandDispatcher;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.PermissionCondition;
import com.sk89q.worldedit.coremc.CoreMcAdapter;
import com.sk89q.worldedit.coremc.CoreMcPermissionsProvider;
import com.sk89q.worldedit.event.platform.ConfigurationLoadEvent;
import com.sk89q.worldedit.event.platform.PlatformReadyEvent;
import com.sk89q.worldedit.event.platform.PlatformUnreadyEvent;
import com.sk89q.worldedit.event.platform.PlatformsRegisteredEvent;
import com.sk89q.worldedit.event.platform.SessionIdleEvent;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extension.platform.PlatformManager;
import com.sk89q.worldedit.internal.anvil.ChunkDeleter;
import com.sk89q.worldedit.internal.event.InteractionDebouncer;
import com.sk89q.worldedit.registry.CommonRegistries;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeCategory;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.generation.ConfiguredFeatureType;
import com.sk89q.worldedit.world.generation.StructureType;
import com.sk89q.worldedit.world.generation.TreeType;
import com.sk89q.worldedit.world.item.ItemCategory;
import com.sk89q.worldedit.world.item.ItemType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.CoralTreeFeature;
import net.minecraft.world.level.levelgen.feature.FallenTreeFeature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.enginehub.piston.Command;
import org.enginehub.worldeditcui.protocol.CUIPacket;
import org.enginehub.worldeditcui.protocol.CUIPacketHandler;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sk89q.worldedit.internal.anvil.ChunkDeleter.DELCHUNKS_FILE_NAME;

public abstract class CoreMcMod {

    private InteractionDebouncer debouncer;
    private CoreMcPlatform platform;
    private CoreMcConfiguration config;
    private Path workingDir;

    protected abstract String getInternalVersion();

    protected CoreMcPermissionsProvider createPermissionsProvider(CoreMcPlatform platform) {
        return new CoreMcPermissionsProvider.VanillaPermissionsProvider(platform);
    }

    protected final void init(CoreMcPlatform platform, Path gameConfigDir) {
        this.workingDir = gameConfigDir.resolve("worldedit");
        if (!Files.exists(workingDir)) {
            try {
                Files.createDirectory(workingDir);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        this.platform = platform;
        this.debouncer = new InteractionDebouncer(platform);

        WorldEdit.getInstance().getPlatformManager().register(platform);

        this.config = new CoreMcConfiguration(workingDir);

        platform.setPermissionsProvider(createPermissionsProvider(platform));
    }

    protected CoreMcConfiguration getConfig() {
        return this.config;
    }

    protected void setupRegistries(MinecraftServer server) {
        // Blocks
        for (Identifier name : server.registryAccess().lookupOrThrow(Registries.BLOCK).keySet()) {
            String key = name.toString();
            if (BlockType.REGISTRY.get(key) == null) {
                BlockType.REGISTRY.register(key, new BlockType(key,
                    input -> CoreMcAdapter.fromNativeBlockState(CoreMcAdapter.toNativeBlock(input.getBlockType()).defaultBlockState())));
            }
        }
        // Items
        for (Identifier name : server.registryAccess().lookupOrThrow(Registries.ITEM).keySet()) {
            String key = name.toString();
            if (ItemType.REGISTRY.get(key) == null) {
                ItemType.REGISTRY.register(key, new ItemType(key));
            }
        }
        // Entities
        for (Identifier name : server.registryAccess().lookupOrThrow(Registries.ENTITY_TYPE).keySet()) {
            String key = name.toString();
            if (EntityType.REGISTRY.get(key) == null) {
                EntityType.REGISTRY.register(key, new EntityType(key));
            }
        }
        // Biomes
        for (Identifier name : server.registryAccess().lookupOrThrow(Registries.BIOME).keySet()) {
            String key = name.toString();
            if (BiomeType.REGISTRY.get(key) == null) {
                BiomeType.REGISTRY.register(key, new BiomeType(key));
            }
        }
        // Tags
        server.registryAccess().lookupOrThrow(Registries.BLOCK).getTags().map(t -> t.key().location()).forEach(name -> {
            String key = name.toString();
            if (BlockCategory.REGISTRY.get(key) == null) {
                BlockCategory.REGISTRY.register(key, new BlockCategory(key));
            }
        });
        server.registryAccess().lookupOrThrow(Registries.ITEM).getTags().map(t -> t.key().location()).forEach(name -> {
            String key = name.toString();
            if (ItemCategory.REGISTRY.get(key) == null) {
                ItemCategory.REGISTRY.register(key, new ItemCategory(key));
            }
        });
        Registry<Biome> biomeRegistry = server.registryAccess().lookupOrThrow(Registries.BIOME);
        biomeRegistry.getTags().forEach(tag -> {
            String key = tag.key().location().toString();
            if (BiomeCategory.REGISTRY.get(key) == null) {
                BiomeCategory.REGISTRY.register(key, new BiomeCategory(
                    key,
                    () -> biomeRegistry.get(tag.key())
                        .stream()
                        .flatMap(HolderSet.Named::stream)
                        .map(Holder::value)
                        .map(CoreMcAdapter::fromNativeBiome)
                        .collect(Collectors.toSet()))
                );
            }
        });
        // Features
        for (Identifier name : server.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).keySet()) {
            String key = name.toString();
            if (ConfiguredFeatureType.REGISTRY.get(key) == null) {
                ConfiguredFeatureType.REGISTRY.register(key, new ConfiguredFeatureType(key));
            }
        }
        // Structures
        for (Identifier name : server.registryAccess().lookupOrThrow(Registries.STRUCTURE).keySet()) {
            String key = name.toString();
            if (StructureType.REGISTRY.get(key) == null) {
                StructureType.REGISTRY.register(key, new StructureType(key));
            }
        }
        // Trees
        Registry<PlacedFeature> placedFeatureRegistry = server.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE);
        for (Identifier name : placedFeatureRegistry.keySet()) {
            // Do some hackery to make sure this is a tree
            var underlyingFeature = placedFeatureRegistry.get(name).get().value().feature().value().feature();
            if (underlyingFeature instanceof TreeFeature || underlyingFeature instanceof FallenTreeFeature || underlyingFeature instanceof CoralTreeFeature) {
                String key = name.toString();
                if (TreeType.REGISTRY.get(key) == null) {
                    TreeType.REGISTRY.register(key, new TreeType(key));
                }
            }
        }

        // Common registries
        CommonRegistries.init();
    }

    protected void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        WorldEdit.getInstance().getEventBus().post(new PlatformsRegisteredEvent());

        PlatformManager manager = WorldEdit.getInstance().getPlatformManager();
        Platform commandsPlatform = manager.queryCapability(Capability.USER_COMMANDS);
        if (commandsPlatform != platform || !platform.isHookingEvents()) {
            // We're not in control of commands/events -- do not register.
            return;
        }

        List<Command> commands = manager.getPlatformCommandManager().getCommandManager()
            .getAllCommands().toList();
        for (Command command : commands) {
            CoreMcCommandWrapper.register(dispatcher, command);
            Set<String> perms = command.getCondition().as(PermissionCondition.class)
                .map(PermissionCondition::getPermissions)
                .orElseGet(Collections::emptySet);
            if (!perms.isEmpty()) {
                perms.forEach(platform.getPermissionsProvider()::registerPermission);
            }
        }
    }

    protected void serverAboutToStart() {
        final Path delChunks = workingDir.resolve(DELCHUNKS_FILE_NAME);
        if (Files.exists(delChunks)) {
            ChunkDeleter.runFromFile(delChunks, true);
        }
    }

    protected void serverStarted(MinecraftServer server) {
        setupRegistries(server);

        config.load();
        WorldEdit.getInstance().getEventBus().post(new ConfigurationLoadEvent(config));
        WorldEdit.getInstance().getEventBus().post(new PlatformReadyEvent(platform));
    }

    protected void serverStopping() {
        WorldEdit worldEdit = WorldEdit.getInstance();
        worldEdit.getSessionManager().unload();
        WorldEdit.getInstance().getEventBus().post(new PlatformUnreadyEvent(platform));
    }

    private boolean skipEvents() {
        return platform == null || !platform.isHookingEvents();
    }

    private boolean skipInteractionEvent(Player player, InteractionHand hand) {
        return skipEvents() || hand != InteractionHand.MAIN_HAND || player.level().isClientSide() || !(player instanceof ServerPlayer);
    }

    public void onLeftClickAir(ServerPlayer playerEntity, InteractionHand hand) {
        if (skipInteractionEvent(playerEntity, hand)) {
            return;
        }

        WorldEdit we = WorldEdit.getInstance();
        CoreMcPlayer player = CoreMcAdapter.fromNativePlayer(playerEntity);

        Optional<Boolean> previousResult = debouncer.getDuplicateInteractionResult(player);
        if (previousResult.isPresent()) {
            return;
        }

        boolean result = we.handleArmSwing(player);
        debouncer.setLastInteraction(player, result);
    }

    protected boolean onLeftClickBlock(Player playerEntity, InteractionHand hand, BlockPos blockPos, net.minecraft.core.Direction face) {
        if (skipInteractionEvent(playerEntity, hand)) {
            return false;
        }

        ServerPlayer serverPlayer = (ServerPlayer) playerEntity;
        WorldEdit we = WorldEdit.getInstance();
        CoreMcPlayer player = CoreMcAdapter.fromNativePlayer(serverPlayer);
        CoreMcWorld world = (CoreMcWorld) CoreMcAdapter.fromNativeWorld(serverPlayer.level());
        Direction direction = CoreMcAdapter.adaptEnumFacing(face);

        Location pos = new Location(world, blockPos.getX(), blockPos.getY(), blockPos.getZ());

        boolean result = we.handleBlockLeftClick(player, pos, direction) || we.handleArmSwing(player);
        debouncer.setLastInteraction(player, result);

        return result;
    }

    protected boolean onRightClickBlock(Player playerEntity, InteractionHand hand, BlockPos blockPos, net.minecraft.core.Direction face) {
        if (skipInteractionEvent(playerEntity, hand)) {
            return false;
        }

        ServerPlayer serverPlayer = (ServerPlayer) playerEntity;
        WorldEdit we = WorldEdit.getInstance();
        CoreMcPlayer player = CoreMcAdapter.fromNativePlayer(serverPlayer);
        CoreMcWorld world = (CoreMcWorld) CoreMcAdapter.fromNativeWorld(serverPlayer.level());
        Direction direction = CoreMcAdapter.adaptEnumFacing(face);

        Location pos = new Location(world, blockPos.getX(), blockPos.getY(), blockPos.getZ());

        boolean result = we.handleBlockRightClick(player, pos, direction) || we.handleRightClick(player);
        debouncer.setLastInteraction(player, result);

        return result;
    }

    protected Optional<Boolean> onRightClickItem(Player playerEntity, InteractionHand hand) {
        if (skipInteractionEvent(playerEntity, hand)) {
            return Optional.empty();
        }

        ServerPlayer serverPlayer = (ServerPlayer) playerEntity;
        WorldEdit we = WorldEdit.getInstance();
        CoreMcPlayer player = CoreMcAdapter.fromNativePlayer(serverPlayer);

        Optional<Boolean> previousResult = debouncer.getDuplicateInteractionResult(player);
        if (previousResult.isPresent()) {
            return previousResult;
        }

        boolean result = we.handleRightClick(player);
        debouncer.setLastInteraction(player, result);

        return Optional.of(result);
    }

    protected void onPlayerDisconnect(Player playerEntity) {
        if (!(playerEntity instanceof ServerPlayer player)) {
            return;
        }
        debouncer.clearInteraction(CoreMcAdapter.fromNativePlayer(player));

        WorldEdit.getInstance().getEventBus()
            .post(new SessionIdleEvent(new CoreMcPlayer.SessionKeyImpl(player)));
    }

    protected void onCuiPacket(CUIPacket payload, CUIPacketHandler.PacketContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        CoreMcPlayer actor = CoreMcAdapter.fromNativePlayer(player);
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(actor);
        session.handleCUIInitializationMessage(payload.eventType(), payload.args(), actor);
    }
}
