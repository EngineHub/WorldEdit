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

package com.sk89q.worldedit.bukkit.adapter.impl.v1_21_5;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extension.platform.Watchdog;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
import com.sk89q.worldedit.internal.wna.WorldNativeAccess;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.registry.state.BooleanProperty;
import com.sk89q.worldedit.registry.state.DirectionalProperty;
import com.sk89q.worldedit.registry.state.EnumProperty;
import com.sk89q.worldedit.registry.state.IntegerProperty;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.serializer.gson.GsonComponentSerializer;
import com.sk89q.worldedit.util.io.file.SafeFiles;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.RegenOptions;
import com.sk89q.worldedit.world.biome.BiomeCategory;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.entity.EntityTypes;
import com.sk89q.worldedit.world.generation.ConfiguredFeatureType;
import com.sk89q.worldedit.world.generation.StructureType;
import com.sk89q.worldedit.world.item.ItemType;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.Clearable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.generator.ChunkGenerator;
import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.tree.LinByteArrayTag;
import org.enginehub.linbus.tree.LinByteTag;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinDoubleTag;
import org.enginehub.linbus.tree.LinEndTag;
import org.enginehub.linbus.tree.LinFloatTag;
import org.enginehub.linbus.tree.LinIntArrayTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinLongArrayTag;
import org.enginehub.linbus.tree.LinLongTag;
import org.enginehub.linbus.tree.LinShortTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;
import org.spigotmc.SpigotConfig;
import org.spigotmc.WatchdogThread;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public final class PaperweightAdapter implements BukkitImplAdapter {

    private final Logger logger = Logger.getLogger(getClass().getCanonicalName());

    private final Field serverWorldsField;
    private final Method getChunkFutureMethod;
    private final Field chunkProviderExecutorField;
    private final PaperweightDataConverters dataFixer;
    private final Watchdog watchdog;

    private static final RandomSource random = RandomSource.create();

    // ------------------------------------------------------------------------
    // Code that may break between versions of Minecraft
    // ------------------------------------------------------------------------

    public PaperweightAdapter() throws NoSuchFieldException, NoSuchMethodException {
        // A simple test
        CraftServer.class.cast(Bukkit.getServer());

        int dataVersion = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
        if (dataVersion != Constants.DATA_VERSION_MC_1_21_5) {
            throw new UnsupportedClassVersionError("Not 1.21.5!");
        }

        serverWorldsField = CraftServer.class.getDeclaredField("worlds");
        serverWorldsField.setAccessible(true);

        getChunkFutureMethod = ServerChunkCache.class.getDeclaredMethod(
            StaticRefraction.GET_CHUNK_FUTURE_MAIN_THREAD,
            int.class, int.class, ChunkStatus.class, boolean.class
        );
        getChunkFutureMethod.setAccessible(true);

        chunkProviderExecutorField = ServerChunkCache.class.getDeclaredField(
            StaticRefraction.MAIN_THREAD_PROCESSOR
        );
        chunkProviderExecutorField.setAccessible(true);

        this.dataFixer = new PaperweightDataConverters(dataVersion, this);

        Watchdog watchdog;
        try {
            Class.forName("org.spigotmc.WatchdogThread");
            watchdog = new SpigotWatchdog();
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            try {
                watchdog = new MojangWatchdog(((CraftServer) Bukkit.getServer()).getServer());
            } catch (NoSuchFieldException ex) {
                watchdog = null;
            }
        }
        this.watchdog = watchdog;

        try {
            Class.forName("org.spigotmc.SpigotConfig");
            SpigotConfig.config.set("world-settings.worldeditregentempworld.verbose", false);
        } catch (ClassNotFoundException ignored) {
        }
    }

    @Override
    public DataFixer getDataFixer() {
        return this.dataFixer;
    }

    /**
     * Read the given NBT data into the given tile entity.
     *
     * @param tileEntity the tile entity
     * @param tag the tag
     */
    static void readTagIntoTileEntity(net.minecraft.nbt.CompoundTag tag, BlockEntity tileEntity) {
        tileEntity.loadWithComponents(tag, MinecraftServer.getServer().registryAccess());
        tileEntity.setChanged();
    }

    /**
     * Get the ID string of the given entity.
     *
     * @param entity the entity
     * @return the entity ID
     */
    private static String getEntityId(Entity entity) {
        return EntityType.getKey(entity.getType()).toString();
    }

    /**
     * Write the entity's NBT data to the given tag.
     *
     * @param entity the entity
     * @param tag the tag
     */
    private static void readEntityIntoTag(Entity entity, net.minecraft.nbt.CompoundTag tag) {
        entity.save(tag);
    }

    private static Block getBlockFromType(BlockType blockType) {
        return DedicatedServer.getServer().registryAccess().lookupOrThrow(Registries.BLOCK).getValue(ResourceLocation.tryParse(blockType.id()));
    }

    private static Item getItemFromType(ItemType itemType) {
        return DedicatedServer.getServer().registryAccess().lookupOrThrow(Registries.ITEM).getValue(ResourceLocation.tryParse(itemType.id()));
    }

    @Override
    public OptionalInt getInternalBlockStateId(BlockData data) {
        net.minecraft.world.level.block.state.BlockState state = ((CraftBlockData) data).getState();
        int combinedId = Block.getId(state);
        return combinedId == 0 && state.getBlock() != Blocks.AIR ? OptionalInt.empty() : OptionalInt.of(combinedId);
    }

    @Override
    public OptionalInt getInternalBlockStateId(BlockState state) {
        Block mcBlock = getBlockFromType(state.getBlockType());
        net.minecraft.world.level.block.state.BlockState newState = mcBlock.defaultBlockState();
        Map<Property<?>, Object> states = state.getStates();
        newState = applyProperties(mcBlock.getStateDefinition(), newState, states);
        final int combinedId = Block.getId(newState);
        return combinedId == 0 && state.getBlockType() != BlockTypes.AIR ? OptionalInt.empty() : OptionalInt.of(combinedId);
    }

    public BlockState adapt(net.minecraft.world.level.block.state.BlockState blockState) {
        int internalId = Block.getId(blockState);
        BlockState state = BlockStateIdAccess.getBlockStateById(internalId);
        if (state == null) {
            state = BukkitAdapter.adapt(CraftBlockData.createData(blockState));
        }

        return state;
    }

    public BiomeType adapt(Biome biome) {
        var mcBiome = ((CraftServer) Bukkit.getServer()).getServer().registryAccess().lookupOrThrow(Registries.BIOME).getKey(biome);
        if (mcBiome == null) {
            return null;
        }
        return BiomeType.REGISTRY.get(mcBiome.toString());
    }

    public net.minecraft.world.level.block.state.BlockState adapt(BlockState blockState) {
        int internalId = BlockStateIdAccess.getBlockStateId(blockState);
        return Block.stateById(internalId);
    }

    @Override
    public BlockState getBlock(Location location) {
        checkNotNull(location);

        CraftWorld craftWorld = ((CraftWorld) location.getWorld());
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        final ServerLevel handle = craftWorld.getHandle();
        LevelChunk chunk = handle.getChunk(x >> 4, z >> 4);
        final BlockPos blockPos = new BlockPos(x, y, z);
        final net.minecraft.world.level.block.state.BlockState blockData = chunk.getBlockState(blockPos);
        return adapt(blockData);
    }

    @Override
    public BaseBlock getFullBlock(Location location) {
        BlockState state = getBlock(location);

        CraftWorld craftWorld = ((CraftWorld) location.getWorld());
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        final ServerLevel handle = craftWorld.getHandle();
        LevelChunk chunk = handle.getChunk(x >> 4, z >> 4);
        final BlockPos blockPos = new BlockPos(x, y, z);

        // Read the NBT data
        BlockEntity te = chunk.getBlockEntity(blockPos);
        if (te != null) {
            net.minecraft.nbt.CompoundTag tag = te.saveWithId(MinecraftServer.getServer().registryAccess());
            return state.toBaseBlock(LazyReference.from(() -> (LinCompoundTag) toNative(tag)));
        }

        return state.toBaseBlock();
    }

    private static final HashMap<BiomeType, Holder<Biome>> biomeTypeToNMSCache = new HashMap<>();
    private static final HashMap<Holder<Biome>, BiomeType> biomeTypeFromNMSCache = new HashMap<>();

    @Override
    public BiomeType getBiome(Location location) {
        checkNotNull(location);

        CraftWorld craftWorld = ((CraftWorld) location.getWorld());
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        final ServerLevel handle = craftWorld.getHandle();
        LevelChunk chunk = handle.getChunk(x >> 4, z >> 4);

        return biomeTypeFromNMSCache.computeIfAbsent(chunk.getNoiseBiome(x >> 2, y >> 2, z >> 2), b -> BiomeType.REGISTRY.get(b.unwrapKey().orElseThrow().location().toString()));
    }

    @Override
    public void setBiome(Location location, BiomeType biome) {
        checkNotNull(location);
        checkNotNull(biome);

        CraftWorld craftWorld = ((CraftWorld) location.getWorld());
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        final ServerLevel handle = craftWorld.getHandle();
        LevelChunk chunk = handle.getChunk(x >> 4, z >> 4);
        chunk.setBiome(x >> 2, y >> 2, z >> 2, biomeTypeToNMSCache.computeIfAbsent(biome, b -> ((CraftServer) Bukkit.getServer()).getServer().registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(ResourceKey.create(Registries.BIOME, ResourceLocation.parse(b.id())))));
        chunk.markUnsaved();
    }

    @Override
    public WorldNativeAccess<?, ?, ?> createWorldNativeAccess(World world) {
        return new PaperweightWorldNativeAccess(this, new WeakReference<>(((CraftWorld) world).getHandle()));
    }

    private static net.minecraft.core.Direction adapt(Direction face) {
        return switch (face) {
            case NORTH -> net.minecraft.core.Direction.NORTH;
            case SOUTH -> net.minecraft.core.Direction.SOUTH;
            case WEST -> net.minecraft.core.Direction.WEST;
            case EAST -> net.minecraft.core.Direction.EAST;
            case DOWN -> net.minecraft.core.Direction.DOWN;
            default -> net.minecraft.core.Direction.UP;
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private net.minecraft.world.level.block.state.BlockState applyProperties(
        StateDefinition<Block, net.minecraft.world.level.block.state.BlockState> stateContainer,
        net.minecraft.world.level.block.state.BlockState newState,
        Map<Property<?>, Object> states
    ) {
        for (Map.Entry<Property<?>, Object> state : states.entrySet()) {
            net.minecraft.world.level.block.state.properties.Property<?> property =
                stateContainer.getProperty(state.getKey().getName());
            Comparable<?> value = (Comparable) state.getValue();
            // we may need to adapt this value, depending on the source prop
            if (property instanceof net.minecraft.world.level.block.state.properties.EnumProperty) {
                if (property.getValueClass() == net.minecraft.core.Direction.class) {
                    value = adapt((Direction) value);
                } else {
                    String enumName = (String) value;
                    value = ((net.minecraft.world.level.block.state.properties.EnumProperty<?>) property)
                        .getValue(enumName).orElseThrow(() ->
                            new IllegalStateException(
                                "Enum property " + property.getName() + " does not contain " + enumName
                            )
                        );
                }
            }

            newState = newState.setValue(
                (net.minecraft.world.level.block.state.properties.Property) property,
                (Comparable) value
            );
        }
        return newState;
    }

    @Override
    public BaseEntity getEntity(org.bukkit.entity.Entity entity) {
        checkNotNull(entity);

        CraftEntity craftEntity = ((CraftEntity) entity);
        Entity mcEntity = craftEntity.getHandle();

        // Do not allow creating of passenger entity snapshots, passengers are included in the vehicle entity
        if (mcEntity.isPassenger()) {
            return null;
        }

        String id = getEntityId(mcEntity);

        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        readEntityIntoTag(mcEntity, tag);
        return new BaseEntity(
            EntityTypes.get(id),
            LazyReference.from(() -> (LinCompoundTag) toNative(tag))
        );
    }

    @Nullable
    @Override
    public org.bukkit.entity.Entity createEntity(Location location, BaseEntity state) {
        checkNotNull(location);
        checkNotNull(state);

        CraftWorld craftWorld = ((CraftWorld) location.getWorld());
        ServerLevel worldServer = craftWorld.getHandle();

        String entityId = state.getType().id();

        LinCompoundTag nativeTag = state.getNbt();
        net.minecraft.nbt.CompoundTag tag;
        if (nativeTag != null) {
            tag = (net.minecraft.nbt.CompoundTag) fromNative(nativeTag);
            removeUnwantedEntityTagsRecursively(tag);
        } else {
            tag = new net.minecraft.nbt.CompoundTag();
        }

        tag.putString("id", entityId);

        Entity createdEntity = EntityType.loadEntityRecursive(tag, craftWorld.getHandle(), EntitySpawnReason.COMMAND, (loadedEntity) -> {
            loadedEntity.absSnapTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
            return loadedEntity;
        });

        if (createdEntity != null) {
            worldServer.addFreshEntityWithPassengers(createdEntity, SpawnReason.CUSTOM);
            return createdEntity.getBukkitEntity();
        } else {
            return null;
        }
    }

    // This removes all unwanted tags from the main entity and all its passengers
    private void removeUnwantedEntityTagsRecursively(net.minecraft.nbt.CompoundTag tag) {
        for (String name : Constants.NO_COPY_ENTITY_NBT_FIELDS) {
            tag.remove(name);
        }

        // Adapted from net.minecraft.world.entity.EntityType#loadEntityRecursive
        tag.getList("Passengers").ifPresent(nbttaglist -> {
            for (int i = 0; i < nbttaglist.size(); ++i) {
                removeUnwantedEntityTagsRecursively(nbttaglist.getCompoundOrEmpty(i));
            }
        });
    }

    @Override
    public Component getRichBlockName(BlockType blockType) {
        return TranslatableComponent.of(getBlockFromType(blockType).getDescriptionId());
    }

    @Override
    public Component getRichItemName(ItemType itemType) {
        return TranslatableComponent.of(getItemFromType(itemType).getDescriptionId());
    }

    @Override
    public Component getRichItemName(BaseItemStack itemStack) {
        return GsonComponentSerializer.INSTANCE.deserialize(
            net.minecraft.network.chat.Component.Serializer.toJson(
                CraftItemStack.asNMSCopy(BukkitAdapter.adapt(itemStack)).getItemName(),
                ((CraftServer) Bukkit.getServer()).getServer().registryAccess()
            )
        );
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static final LoadingCache<net.minecraft.world.level.block.state.properties.Property, Property<?>> PROPERTY_CACHE = CacheBuilder.newBuilder().build(new CacheLoader<>() {
        @Override
        public Property<?> load(net.minecraft.world.level.block.state.properties.Property state) {
            return switch (state) {
                case net.minecraft.world.level.block.state.properties.BooleanProperty ignored ->
                    new BooleanProperty(state.getName(), ImmutableList.copyOf(state.getPossibleValues()));
                case net.minecraft.world.level.block.state.properties.EnumProperty ignored -> {
                    if (state.getValueClass() == net.minecraft.core.Direction.class) {
                        yield new DirectionalProperty(state.getName(),
                            (List<Direction>) state.getPossibleValues().stream().map(e -> Direction.valueOf(((StringRepresentable) e).getSerializedName().toUpperCase(Locale.ROOT))).toList());
                    }
                    yield new EnumProperty(state.getName(),
                        (List<String>) state.getPossibleValues().stream().map(e -> ((StringRepresentable) e).getSerializedName()).toList());
                }
                case net.minecraft.world.level.block.state.properties.IntegerProperty ignored ->
                    new IntegerProperty(state.getName(), ImmutableList.copyOf(state.getPossibleValues()));
                default ->
                    throw new IllegalArgumentException("WorldEdit needs an update to support " + state.getClass().getSimpleName());
            };
        }
    });

    @SuppressWarnings({ "rawtypes" })
    @Override
    public Map<String, ? extends Property<?>> getProperties(BlockType blockType) {
        Map<String, Property<?>> properties = new TreeMap<>();
        Block block = getBlockFromType(blockType);
        StateDefinition<Block, net.minecraft.world.level.block.state.BlockState> blockStateList =
            block.getStateDefinition();
        for (net.minecraft.world.level.block.state.properties.Property state : blockStateList.getProperties()) {
            Property<?> property = PROPERTY_CACHE.getUnchecked(state);
            properties.put(property.getName(), property);
        }
        return properties;
    }

    @Override
    public void sendFakeNBT(Player player, BlockVector3 pos, LinCompoundTag nbtData) {
        var structureBlock = new StructureBlockEntity(
            new BlockPos(pos.x(), pos.y(), pos.z()),
            Blocks.STRUCTURE_BLOCK.defaultBlockState()
        );
        structureBlock.setLevel(((CraftPlayer) player).getHandle().level());
        ((CraftPlayer) player).getHandle().connection.send(ClientboundBlockEntityDataPacket.create(
            structureBlock,
            (blockEntity, registryAccess) -> (net.minecraft.nbt.CompoundTag) fromNative(nbtData)
        ));
    }

    @Override
    public void sendFakeOP(Player player) {
        ((CraftPlayer) player).getHandle().connection.send(new ClientboundEntityEventPacket(
            ((CraftPlayer) player).getHandle(), (byte) 28
        ));
    }

    /**
     * For serializing and deserializing components.
     */
    private static final Codec<DataComponentPatch> COMPONENTS_CODEC = DataComponentPatch.CODEC.optionalFieldOf(
        "components", DataComponentPatch.EMPTY
    ).codec();

    @Override
    public org.bukkit.inventory.ItemStack adapt(BaseItemStack baseItemStack) {
        var registryAccess = DedicatedServer.getServer().registryAccess();
        ItemStack stack = new ItemStack(
            registryAccess.lookupOrThrow(Registries.ITEM).getOrThrow(ResourceKey.create(
                Registries.ITEM,
                ResourceLocation.tryParse(baseItemStack.getType().id())
            )),
            baseItemStack.getAmount()
        );
        LinCompoundTag nbt = baseItemStack.getNbt();
        if (nbt != null) {
            DataComponentPatch componentPatch = COMPONENTS_CODEC.parse(
                registryAccess.createSerializationContext(NbtOps.INSTANCE),
                fromNative(nbt)
            ).getOrThrow();
            stack.applyComponents(componentPatch);
        }
        return CraftItemStack.asCraftMirror(stack);
    }

    @Override
    public BaseItemStack adapt(org.bukkit.inventory.ItemStack itemStack) {
        var registryAccess = DedicatedServer.getServer().registryAccess();
        final ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        CompoundTag tag = (CompoundTag) COMPONENTS_CODEC.encodeStart(
            registryAccess.createSerializationContext(NbtOps.INSTANCE),
            nmsStack.getComponentsPatch()
        ).getOrThrow();
        return new BaseItemStack(BukkitAdapter.asItemType(itemStack.getType()), LazyReference.from(() -> (LinCompoundTag) toNative(tag)), itemStack.getAmount());
    }

    private final LoadingCache<ServerLevel, PaperweightFakePlayer> fakePlayers
        = CacheBuilder.newBuilder().weakKeys().softValues().build(CacheLoader.from(PaperweightFakePlayer::new));

    @Override
    public boolean simulateItemUse(World world, BlockVector3 position, BaseItem item, Direction face) {
        CraftWorld craftWorld = (CraftWorld) world;
        ServerLevel worldServer = craftWorld.getHandle();
        ItemStack stack = CraftItemStack.asNMSCopy(adapt(
            item instanceof BaseItemStack
                ? ((BaseItemStack) item)
                : new BaseItemStack(item.getType(), item.getNbtReference(), 1)
        ));

        PaperweightFakePlayer fakePlayer;
        try {
            fakePlayer = fakePlayers.get(worldServer);
        } catch (ExecutionException ignored) {
            return false;
        }
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, stack);
        fakePlayer.absSnapTo(position.x(), position.y(), position.z(),
            (float) face.toVector().toYaw(), (float) face.toVector().toPitch());

        final BlockPos blockPos = new BlockPos(position.x(), position.y(), position.z());
        final Vec3 blockVec = Vec3.atLowerCornerOf(blockPos);
        final net.minecraft.core.Direction enumFacing = adapt(face);
        BlockHitResult rayTrace = new BlockHitResult(blockVec, enumFacing, blockPos, false);
        UseOnContext context = new UseOnContext(fakePlayer, InteractionHand.MAIN_HAND, rayTrace);
        InteractionResult result = stack.useOn(context);
        if (result != InteractionResult.SUCCESS) {
            if (worldServer.getBlockState(blockPos).useItemOn(stack, worldServer, fakePlayer, InteractionHand.MAIN_HAND, rayTrace).consumesAction()) {
                result = InteractionResult.SUCCESS;
            } else {
                result = stack.getItem().use(worldServer, fakePlayer, InteractionHand.MAIN_HAND);
            }
        }

        return result == InteractionResult.SUCCESS;
    }

    @Override
    public boolean canPlaceAt(World world, BlockVector3 position, BlockState blockState) {
        int internalId = BlockStateIdAccess.getBlockStateId(blockState);
        net.minecraft.world.level.block.state.BlockState blockData = Block.stateById(internalId);
        return blockData.canSurvive(((CraftWorld) world).getHandle(), new BlockPos(position.x(), position.y(), position.z()));
    }

    @Override
    public boolean regenerate(World bukkitWorld, Region region, Extent extent, RegenOptions options) {
        try {
            doRegen(bukkitWorld, region, extent, options);
        } catch (Exception e) {
            throw new IllegalStateException("Regen failed.", e);
        }

        return true;
    }

    private void doRegen(World bukkitWorld, Region region, Extent extent, RegenOptions options) throws Exception {
        Environment env = bukkitWorld.getEnvironment();
        ChunkGenerator gen = bukkitWorld.getGenerator();

        Path tempDir = Files.createTempDirectory("WorldEditWorldGen");
        LevelStorageSource levelStorage = LevelStorageSource.createDefault(tempDir);
        ResourceKey<LevelStem> worldDimKey = getWorldDimKey(env);
        try (LevelStorageSource.LevelStorageAccess session = levelStorage.createAccess("worldeditregentempworld", worldDimKey)) {
            ServerLevel originalWorld = ((CraftWorld) bukkitWorld).getHandle();
            PrimaryLevelData levelProperties = (PrimaryLevelData) originalWorld.getServer()
                .getWorldData().overworldData();
            WorldOptions originalOpts = levelProperties.worldGenOptions();

            long seed = options.getSeed().orElse(originalWorld.getSeed());
            WorldOptions newOpts = options.getSeed().isPresent()
                ? originalOpts.withSeed(OptionalLong.of(seed))
                : originalOpts;

            LevelSettings newWorldSettings = new LevelSettings(
                "worldeditregentempworld",
                levelProperties.settings.gameType(),
                levelProperties.settings.hardcore(),
                levelProperties.settings.difficulty(),
                levelProperties.settings.allowCommands(),
                levelProperties.settings.gameRules(),
                levelProperties.settings.getDataConfiguration()
            );

            @SuppressWarnings("deprecation")
            PrimaryLevelData.SpecialWorldProperty specialWorldProperty =
                levelProperties.isFlatWorld()
                    ? PrimaryLevelData.SpecialWorldProperty.FLAT
                    : levelProperties.isDebugWorld()
                    ? PrimaryLevelData.SpecialWorldProperty.DEBUG
                    : PrimaryLevelData.SpecialWorldProperty.NONE;

            PrimaryLevelData newWorldData = new PrimaryLevelData(newWorldSettings, newOpts, specialWorldProperty, Lifecycle.stable());

            ServerLevel freshWorld = new ServerLevel(
                originalWorld.getServer(),
                originalWorld.getServer().executor,
                session, newWorldData,
                originalWorld.dimension(),
                new LevelStem(
                    originalWorld.dimensionTypeRegistration(),
                    originalWorld.getChunkSource().getGenerator()
                ),
                new NoOpWorldLoadListener(),
                originalWorld.isDebug(),
                seed,
                ImmutableList.of(),
                false,
                originalWorld.getRandomSequences(),
                env,
                gen,
                bukkitWorld.getBiomeProvider()
            );
            try {
                regenForWorld(region, extent, freshWorld, options);
            } finally {
                freshWorld.getChunkSource().close(false);
            }
        } finally {
            try {
                @SuppressWarnings("unchecked")
                Map<String, World> map = (Map<String, World>) serverWorldsField.get(Bukkit.getServer());
                map.remove("worldeditregentempworld");
            } catch (IllegalAccessException ignored) {
            }
            SafeFiles.tryHardToDeleteDir(tempDir);
        }
    }

    private BiomeType adapt(ServerLevel serverWorld, Biome origBiome) {
        ResourceLocation key = serverWorld.registryAccess().lookupOrThrow(Registries.BIOME).getKey(origBiome);
        if (key == null) {
            return null;
        }
        return BiomeTypes.get(key.toString());
    }

    @SuppressWarnings("unchecked")
    private void regenForWorld(Region region, Extent extent, ServerLevel serverWorld, RegenOptions options) throws WorldEditException {
        List<CompletableFuture<ChunkAccess>> chunkLoadings = submitChunkLoadTasks(region, serverWorld);
        BlockableEventLoop<Runnable> executor;
        try {
            executor = (BlockableEventLoop<Runnable>) chunkProviderExecutorField.get(serverWorld.getChunkSource());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Couldn't get executor for chunk loading.", e);
        }
        executor.managedBlock(() -> {
            // bail out early if a future fails
            if (chunkLoadings.stream().anyMatch(ftr ->
                ftr.isDone() && Futures.getUnchecked(ftr) == null
            )) {
                return false;
            }
            return chunkLoadings.stream().allMatch(CompletableFuture::isDone);
        });
        Map<ChunkPos, ChunkAccess> chunks = new HashMap<>();
        for (CompletableFuture<ChunkAccess> future : chunkLoadings) {
            @Nullable
            ChunkAccess chunk = future.getNow(null);
            checkState(chunk != null, "Failed to generate a chunk, regen failed.");
            chunks.put(chunk.getPos(), chunk);
        }

        for (BlockVector3 vec : region) {
            BlockPos pos = new BlockPos(vec.x(), vec.y(), vec.z());
            ChunkAccess chunk = chunks.get(new ChunkPos(pos));
            final net.minecraft.world.level.block.state.BlockState blockData = chunk.getBlockState(pos);
            int internalId = Block.getId(blockData);
            BlockStateHolder<?> state = BlockStateIdAccess.getBlockStateById(internalId);
            Objects.requireNonNull(state);
            BlockEntity blockEntity = chunk.getBlockEntity(pos);
            if (blockEntity != null) {
                net.minecraft.nbt.CompoundTag tag = blockEntity.saveWithId(serverWorld.registryAccess());
                state = state.toBaseBlock(LazyReference.from(() -> (LinCompoundTag) toNative(tag)));
            }
            extent.setBlock(vec, state.toBaseBlock());
            if (options.shouldRegenBiomes()) {
                Biome origBiome = chunk.getNoiseBiome(vec.x(), vec.y(), vec.z()).value();
                BiomeType adaptedBiome = adapt(serverWorld, origBiome);
                if (adaptedBiome != null) {
                    extent.setBiome(vec, adaptedBiome);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<CompletableFuture<ChunkAccess>> submitChunkLoadTasks(Region region, ServerLevel serverWorld) {
        ServerChunkCache chunkManager = serverWorld.getChunkSource();
        List<CompletableFuture<ChunkAccess>> chunkLoadings = new ArrayList<>();
        // Pre-gen all the chunks
        for (BlockVector2 chunk : region.getChunks()) {
            try {
                //noinspection unchecked
                chunkLoadings.add(
                    ((CompletableFuture<ChunkResult<ChunkAccess>>)
                        getChunkFutureMethod.invoke(chunkManager, chunk.x(), chunk.z(), ChunkStatus.FEATURES, true))
                        .thenApply(either -> either.orElse(null))
                );
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Couldn't load chunk for regen.", e);
            }
        }
        return chunkLoadings;
    }

    private ResourceKey<LevelStem> getWorldDimKey(Environment env) {
        return switch (env) {
            case NETHER -> LevelStem.NETHER;
            case THE_END -> LevelStem.END;
            default -> LevelStem.OVERWORLD;
        };
    }

    private static final Set<SideEffect> SUPPORTED_SIDE_EFFECTS = Sets.immutableEnumSet(
        SideEffect.NEIGHBORS,
        SideEffect.LIGHTING,
        SideEffect.VALIDATION,
        SideEffect.ENTITY_AI,
        SideEffect.EVENTS,
        SideEffect.UPDATE
    );

    @Override
    public Set<SideEffect> getSupportedSideEffects() {
        return SUPPORTED_SIDE_EFFECTS;
    }

    @Override
    public boolean clearContainerBlockContents(World world, BlockVector3 pt) {
        ServerLevel originalWorld = ((CraftWorld) world).getHandle();

        BlockEntity entity = originalWorld.getBlockEntity(new BlockPos(pt.x(), pt.y(), pt.z()));
        if (entity instanceof Clearable) {
            ((Clearable) entity).clearContent();
            return true;
        }
        return false;
    }

    @Override
    public void initializeRegistries() {
        DedicatedServer server = ((CraftServer) Bukkit.getServer()).getServer();
        // Biomes
        for (ResourceLocation name : server.registryAccess().lookupOrThrow(Registries.BIOME).keySet()) {
            if (BiomeType.REGISTRY.get(name.toString()) == null) {
                BiomeType.REGISTRY.register(name.toString(), new BiomeType(name.toString()));
            }
        }

        // Features
        for (ResourceLocation name: server.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).keySet()) {
            if (ConfiguredFeatureType.REGISTRY.get(name.toString()) == null) {
                ConfiguredFeatureType.REGISTRY.register(name.toString(), new ConfiguredFeatureType(name.toString()));
            }
        }

        // Structures
        for (ResourceLocation name : server.registryAccess().lookupOrThrow(Registries.STRUCTURE).keySet()) {
            if (StructureType.REGISTRY.get(name.toString()) == null) {
                StructureType.REGISTRY.register(name.toString(), new StructureType(name.toString()));
            }
        }

        // BiomeCategories
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
                        .map(this::adapt)
                        .collect(Collectors.toSet()))
                );
            }
        });
    }

    public boolean generateFeature(ConfiguredFeatureType type, World world, EditSession session, BlockVector3 pt) {
        ServerLevel originalWorld = ((CraftWorld) world).getHandle();
        ConfiguredFeature<?, ?> feature = originalWorld.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).getValue(ResourceLocation.tryParse(type.id()));
        ServerChunkCache chunkManager = originalWorld.getChunkSource();
        try (PaperweightServerLevelDelegateProxy.LevelAndProxy proxyLevel =
                 PaperweightServerLevelDelegateProxy.newInstance(session, originalWorld, this)) {
            return feature != null && feature.place(proxyLevel.level(), chunkManager.getGenerator(), random, new BlockPos(pt.x(), pt.y(), pt.z()));
        } catch (MaxChangedBlocksException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean generateStructure(StructureType type, World world, EditSession session, BlockVector3 pt) {
        ServerLevel originalWorld = ((CraftWorld) world).getHandle();
        Registry<Structure> structureRegistry = originalWorld.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        Structure structure = structureRegistry.getValue(ResourceLocation.tryParse(type.id()));
        if (structure == null) {
            return false;
        }

        ServerChunkCache chunkManager = originalWorld.getChunkSource();
        try (PaperweightServerLevelDelegateProxy.LevelAndProxy proxyLevel =
                 PaperweightServerLevelDelegateProxy.newInstance(session, originalWorld, this)) {
            ChunkPos chunkPos = new ChunkPos(new BlockPos(pt.x(), pt.y(), pt.z()));
            StructureStart structureStart = structure.generate(
                structureRegistry.wrapAsHolder(structure), originalWorld.dimension(), originalWorld.registryAccess(),
                chunkManager.getGenerator(), chunkManager.getGenerator().getBiomeSource(), chunkManager.randomState(),
                originalWorld.getStructureManager(), originalWorld.getSeed(), chunkPos, 0,
                proxyLevel.level(), biome -> true
            );

            if (!structureStart.isValid()) {
                return false;
            } else {
                BoundingBox boundingBox = structureStart.getBoundingBox();
                ChunkPos min = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.minX()), SectionPos.blockToSectionCoord(boundingBox.minZ()));
                ChunkPos max = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.maxX()), SectionPos.blockToSectionCoord(boundingBox.maxZ()));
                ChunkPos.rangeClosed(min, max).forEach((chunkPosx) ->
                    structureStart.placeInChunk(
                        proxyLevel.level(), originalWorld.structureManager(), chunkManager.getGenerator(),
                        originalWorld.getRandom(),
                        new BoundingBox(
                            chunkPosx.getMinBlockX(), originalWorld.getMinY(), chunkPosx.getMinBlockZ(),
                            chunkPosx.getMaxBlockX(), originalWorld.getMaxY(), chunkPosx.getMaxBlockZ()
                        ), chunkPosx
                    )
                );
                return true;
            }
        } catch (MaxChangedBlocksException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendBiomeUpdates(World world, Iterable<BlockVector2> chunks) {
        ServerLevel originalWorld = ((CraftWorld) world).getHandle();

        List<ChunkAccess> nativeChunks = chunks instanceof Collection<BlockVector2> chunkCollection ? Lists.newArrayListWithCapacity(chunkCollection.size()) : Lists.newArrayList();
        for (BlockVector2 chunk : chunks) {
            nativeChunks.add(originalWorld.getChunk(chunk.x(), chunk.z(), ChunkStatus.BIOMES, false));
        }
        originalWorld.getChunkSource().chunkMap.resendBiomesForChunks(nativeChunks);
    }

    // ------------------------------------------------------------------------
    // Code that is less likely to break
    // ------------------------------------------------------------------------

    /**
     * Converts from a non-native NMS NBT structure to a native WorldEdit NBT
     * structure.
     *
     * @param foreign non-native NMS NBT structure
     * @return native WorldEdit NBT structure
     */
    LinTag<?> toNative(net.minecraft.nbt.Tag foreign) {
        if (foreign == null) {
            return null;
        }
        if (foreign instanceof net.minecraft.nbt.CompoundTag compoundTag) {
            LinCompoundTag.Builder builder = LinCompoundTag.builder();
            for (var entry : compoundTag.keySet()) {
                builder.put(entry, toNative(compoundTag.get(entry)));
            }
            return builder.build();
        } else if (foreign instanceof net.minecraft.nbt.ByteTag byteTag) {
            return LinByteTag.of(byteTag.byteValue());
        } else if (foreign instanceof net.minecraft.nbt.ByteArrayTag byteArrayTag) {
            return LinByteArrayTag.of(byteArrayTag.getAsByteArray());
        } else if (foreign instanceof net.minecraft.nbt.DoubleTag doubleTag) {
            return LinDoubleTag.of(doubleTag.doubleValue());
        } else if (foreign instanceof net.minecraft.nbt.FloatTag floatTag) {
            return LinFloatTag.of(floatTag.floatValue());
        } else if (foreign instanceof net.minecraft.nbt.IntTag intTag) {
            return LinIntTag.of(intTag.intValue());
        } else if (foreign instanceof net.minecraft.nbt.IntArrayTag intArrayTag) {
            return LinIntArrayTag.of(intArrayTag.getAsIntArray());
        } else if (foreign instanceof net.minecraft.nbt.LongArrayTag longArrayTag) {
            return LinLongArrayTag.of(longArrayTag.getAsLongArray());
        } else if (foreign instanceof net.minecraft.nbt.ListTag listTag) {
            try {
                return toNativeList(listTag);
            } catch (Throwable e) {
                logger.log(Level.WARNING, "Failed to convert net.minecraft.nbt.ListTag", e);
                return LinListTag.empty(LinTagType.endTag());
            }
        } else if (foreign instanceof net.minecraft.nbt.LongTag longTag) {
            return LinLongTag.of(longTag.longValue());
        } else if (foreign instanceof net.minecraft.nbt.ShortTag shortTag) {
            return LinShortTag.of(shortTag.shortValue());
        } else if (foreign instanceof net.minecraft.nbt.StringTag stringTag) {
            return LinStringTag.of(stringTag.toString());
        } else if (foreign instanceof net.minecraft.nbt.EndTag) {
            return LinEndTag.instance();
        } else {
            throw new IllegalArgumentException("Don't know how to make native " + foreign.getClass().getCanonicalName());
        }
    }

    private static byte identifyRawElementType(net.minecraft.nbt.ListTag list) {
        byte b = 0;

        for (Tag tag : list) {
            byte c = tag.getId();
            if (b == 0) {
                b = c;
            } else if (b != c) {
                return 10;
            }
        }

        return b;
    }

    private static net.minecraft.nbt.CompoundTag wrapTag(net.minecraft.nbt.Tag tag) {
        if (tag instanceof net.minecraft.nbt.CompoundTag compoundTag) {
            return compoundTag;
        }
        var compoundTag = new net.minecraft.nbt.CompoundTag();
        compoundTag.put("", tag);
        return compoundTag;
    }

    /**
     * Convert a foreign NBT list tag into a native WorldEdit one.
     *
     * @param foreign the foreign tag
     * @return the converted tag
     * @throws SecurityException on error
     * @throws IllegalArgumentException on error
     */
    private LinListTag<?> toNativeList(net.minecraft.nbt.ListTag foreign) throws SecurityException, IllegalArgumentException {
        byte rawType = identifyRawElementType(foreign);
        LinListTag.Builder<LinTag<?>> builder = LinListTag.builder(LinTagType.fromId(
                LinTagId.fromId(rawType)
        ));
        for (net.minecraft.nbt.Tag tag : foreign) {
            if (rawType == LinTagId.COMPOUND.id() && !(tag instanceof net.minecraft.nbt.CompoundTag)) {
                builder.add(toNative(wrapTag(tag)));
            } else {
                builder.add(toNative(tag));
            }
        }
        return builder.build();
    }

    /**
     * Converts a WorldEdit-native NBT structure to a NMS structure.
     *
     * @param foreign structure to convert
     * @return non-native structure
     */
    Tag fromNative(LinTag<?> foreign) {
        if (foreign == null) {
            return null;
        }
        if (foreign instanceof LinCompoundTag compoundTag) {
            net.minecraft.nbt.CompoundTag tag = new CompoundTag();
            for (var entry : compoundTag.value().entrySet()) {
                tag.put(entry.getKey(), fromNative(entry.getValue()));
            }
            return tag;
        } else if (foreign instanceof LinByteTag byteTag) {
            return ByteTag.valueOf(byteTag.valueAsByte());
        } else if (foreign instanceof LinByteArrayTag byteArrayTag) {
            return new ByteArrayTag(byteArrayTag.value());
        } else if (foreign instanceof LinDoubleTag doubleTag) {
            return DoubleTag.valueOf(doubleTag.valueAsDouble());
        } else if (foreign instanceof LinFloatTag floatTag) {
            return FloatTag.valueOf(floatTag.valueAsFloat());
        } else if (foreign instanceof LinIntTag intTag) {
            return IntTag.valueOf(intTag.valueAsInt());
        } else if (foreign instanceof LinIntArrayTag intArrayTag) {
            return new IntArrayTag(intArrayTag.value());
        } else if (foreign instanceof LinLongArrayTag longArrayTag) {
            return new LongArrayTag(longArrayTag.value());
        } else if (foreign instanceof LinListTag<?> listTag) {
            net.minecraft.nbt.ListTag tag = new ListTag();
            for (var t : listTag.value()) {
                tag.add(fromNative(t));
            }
            return tag;
        } else if (foreign instanceof LinLongTag longTag) {
            return LongTag.valueOf(longTag.valueAsLong());
        } else if (foreign instanceof LinShortTag shortTag) {
            return ShortTag.valueOf(shortTag.valueAsShort());
        } else if (foreign instanceof LinStringTag stringTag) {
            return StringTag.valueOf(stringTag.value());
        } else if (foreign instanceof LinEndTag) {
            return EndTag.INSTANCE;
        } else {
            throw new IllegalArgumentException("Don't know how to make NMS " + foreign.getClass().getCanonicalName());
        }
    }

    @Override
    public boolean supportsWatchdog() {
        return watchdog != null;
    }

    @Override
    public void tickWatchdog() {
        watchdog.tick();
    }

    private class SpigotWatchdog implements Watchdog {
        private final Field instanceField;
        private final Field lastTickField;

        SpigotWatchdog() throws NoSuchFieldException {
            Field instanceField = WatchdogThread.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            this.instanceField = instanceField;

            Field lastTickField = WatchdogThread.class.getDeclaredField("lastTick");
            lastTickField.setAccessible(true);
            this.lastTickField = lastTickField;
        }

        @Override
        public void tick() {
            try {
                WatchdogThread instance = (WatchdogThread) this.instanceField.get(null);
                if ((long) lastTickField.get(instance) != 0) {
                    WatchdogThread.tick();
                }
            } catch (IllegalAccessException e) {
                logger.log(Level.WARNING, "Failed to tick watchdog", e);
            }
        }
    }

    private static class MojangWatchdog implements Watchdog {
        private final DedicatedServer server;
        private final Field tickField;

        MojangWatchdog(DedicatedServer server) throws NoSuchFieldException {
            this.server = server;
            Field tickField = MinecraftServer.class.getDeclaredField(StaticRefraction.NEXT_TICK_TIME);
            if (tickField.getType() != long.class) {
                throw new IllegalStateException("nextTickTime is not a long field, mapping is likely incorrect");
            }
            tickField.setAccessible(true);
            this.tickField = tickField;
        }

        @Override
        public void tick() {
            try {
                tickField.set(server, Util.getMillis());
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    private static class NoOpWorldLoadListener implements ChunkProgressListener {
        @Override
        public void updateSpawnPos(ChunkPos spawnPos) {
        }

        @Override
        public void onStatusChange(ChunkPos pos, @org.jetbrains.annotations.Nullable ChunkStatus status) {
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

    }
}
