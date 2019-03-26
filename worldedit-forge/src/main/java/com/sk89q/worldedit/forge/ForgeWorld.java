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

package com.sk89q.worldedit.forge;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.TreeGenerator.TreeType;
import com.sk89q.worldedit.world.AbstractWorld;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldedit.world.weather.WeatherTypes;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.feature.BigBrownMushroomFeature;
import net.minecraft.world.gen.feature.BigRedMushroomFeature;
import net.minecraft.world.gen.feature.BigTreeFeature;
import net.minecraft.world.gen.feature.BirchTreeFeature;
import net.minecraft.world.gen.feature.CanopyTreeFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.JungleTreeFeature;
import net.minecraft.world.gen.feature.MegaJungleFeature;
import net.minecraft.world.gen.feature.MegaPineTree;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.PointyTaigaTreeFeature;
import net.minecraft.world.gen.feature.SavannaTreeFeature;
import net.minecraft.world.gen.feature.ShrubFeature;
import net.minecraft.world.gen.feature.SwampTreeFeature;
import net.minecraft.world.gen.feature.TallTaigaTreeFeature;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.storage.WorldInfo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

/**
 * An adapter to Minecraft worlds for WorldEdit.
 */
public class ForgeWorld extends AbstractWorld {

    private static final Random random = new Random();
    private static final int UPDATE = 1, NOTIFY = 2;

    private static final IBlockState JUNGLE_LOG = Blocks.JUNGLE_LOG.getDefaultState();
    private static final IBlockState JUNGLE_LEAF = Blocks.JUNGLE_LEAVES.getDefaultState().with(BlockLeaves.PERSISTENT, Boolean.TRUE);
    private static final IBlockState JUNGLE_SHRUB = Blocks.OAK_LEAVES.getDefaultState().with(BlockLeaves.PERSISTENT, Boolean.TRUE);
    
    private final WeakReference<World> worldRef;

    /**
     * Construct a new world.
     *
     * @param world the world
     */
    ForgeWorld(World world) {
        checkNotNull(world);
        this.worldRef = new WeakReference<>(world);
    }

    /**
     * Get the underlying handle to the world.
     *
     * @return the world
     * @throws WorldEditException thrown if a reference to the world was lost (i.e. world was unloaded)
     */
    public World getWorldChecked() throws WorldEditException {
        World world = worldRef.get();
        if (world != null) {
            return world;
        } else {
            throw new WorldReferenceLostException("The reference to the world was lost (i.e. the world may have been unloaded)");
        }
    }

    /**
     * Get the underlying handle to the world.
     *
     * @return the world
     * @throws RuntimeException thrown if a reference to the world was lost (i.e. world was unloaded)
     */
    public World getWorld() {
        World world = worldRef.get();
        if (world != null) {
            return world;
        } else {
            throw new RuntimeException("The reference to the world was lost (i.e. the world may have been unloaded)");
        }
    }

    @Override
    public String getName() {
        return getWorld().getWorldInfo().getWorldName();
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, boolean notifyAndLight) throws WorldEditException {
        checkNotNull(position);
        checkNotNull(block);

        World world = getWorldChecked();
        int x = position.getBlockX();
        int y = position.getBlockY();
        int z = position.getBlockZ();

        // First set the block
        Chunk chunk = world.getChunk(x >> 4, z >> 4);
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState old = chunk.getBlockState(pos);
        IBlockState newState = ForgeAdapter.adapt(block.toImmutableState());
        IBlockState successState = chunk.setBlockState(pos, newState, false);
        boolean successful = successState != null;

        // Create the TileEntity
        if (successful || old == newState) {
            if (block instanceof BaseBlock) {
                CompoundTag tag = ((BaseBlock) block).getNbtData();
                if (tag != null) {
                    NBTTagCompound nativeTag = NBTConverter.toNative(tag);
                    nativeTag.putString("id", ((BaseBlock) block).getNbtId());
                    TileEntityUtils.setTileEntity(world, position, nativeTag);
                }
            }
        }

        if (successful && notifyAndLight) {
            //world.checkLight(pos);
            world.markAndNotifyBlock(pos, chunk, old, newState, UPDATE | NOTIFY);
        }

        return successful;
    }

    @Override
    public boolean notifyAndLightBlock(BlockVector3 position, BlockState previousType) throws WorldEditException {
        BlockPos pos = new BlockPos(position.getX(), position.getY(), position.getZ());
        getWorld().notifyBlockUpdate(pos, ForgeAdapter.adapt(previousType), getWorld().getBlockState(pos), 1 | 2);
        return true;
    }

    @Override
    public int getBlockLightLevel(BlockVector3 position) {
        checkNotNull(position);
        return getWorld().getLight(ForgeAdapter.toBlockPos(position));
    }

    @Override
    public boolean clearContainerBlockContents(BlockVector3 position) {
        checkNotNull(position);
        TileEntity tile = getWorld().getTileEntity(ForgeAdapter.toBlockPos(position));
        if ((tile instanceof IInventory)) {
            IInventory inv = (IInventory) tile;
            int size = inv.getSizeInventory();
            for (int i = 0; i < size; i++) {
                inv.setInventorySlotContents(i, ItemStack.EMPTY);
            }
            return true;
        }
        return false;
    }

    @Override
    public BiomeType getBiome(BlockVector2 position) {
        checkNotNull(position);
        return ForgeAdapter.adapt(getWorld().getBiomeBody(new BlockPos(position.getBlockX(), 0, position.getBlockZ())));
    }

    @Override
    public boolean setBiome(BlockVector2 position, BiomeType biome) {
        checkNotNull(position);
        checkNotNull(biome);

        Chunk chunk = getWorld().getChunk(new BlockPos(position.getBlockX(), 0, position.getBlockZ()));
        if (chunk.isLoaded()) {
            chunk.getBiomes()[((position.getBlockZ() & 0xF) << 4 | position.getBlockX() & 0xF)] = ForgeAdapter.adapt(biome);
            return true;
        }

        return false;
    }

    @Override
    public boolean useItem(BlockVector3 position, BaseItem item, Direction face) {
        Item nativeItem = ForgeAdapter.adapt(item.getType());
        ItemStack stack;
        if (item.getNbtData() == null) {
            stack = new ItemStack(nativeItem, 1);
        } else {
            stack = new ItemStack(nativeItem, 1, NBTConverter.toNative(item.getNbtData()));
        }
        World world = getWorld();
        ItemUseContext itemUseContext = new ItemUseContext(
                new WorldEditFakePlayer((WorldServer) world),
                stack,
                ForgeAdapter.toBlockPos(position),
                ForgeAdapter.adapt(face),
                0f,
                0f,
                0f
        );
        EnumActionResult used = stack.onItemUse(itemUseContext);
        return used != EnumActionResult.FAIL;
    }

    @Override
    public void dropItem(Vector3 position, BaseItemStack item) {
        checkNotNull(position);
        checkNotNull(item);

        if (item.getType() == ItemTypes.AIR) {
            return;
        }

        EntityItem entity = new EntityItem(getWorld(), position.getX(), position.getY(), position.getZ(), ForgeAdapter.adapt(item));
        entity.setPickupDelay(10);
        getWorld().spawnEntity(entity);
    }

    @Override
    public void simulateBlockMine(BlockVector3 position) {
        BlockPos pos = ForgeAdapter.toBlockPos(position);
        IBlockState state = getWorld().getBlockState(pos);
        state.dropBlockAsItem(getWorld(), pos, 0);
        getWorld().removeBlock(pos);
    }

    @Override
    public boolean regenerate(Region region, EditSession editSession) {
        // TODO Fix for 1.13
        return false;
//        // Don't even try to regen if it's going to fail.
//        IChunkProvider provider = getWorld().getChunkProvider();
//        if (!(provider instanceof ChunkProviderServer)) {
//            return false;
//        }
//
//        File saveFolder = Files.createTempDir();
//        // register this just in case something goes wrong
//        // normally it should be deleted at the end of this method
//        saveFolder.deleteOnExit();
//
//        WorldServer originalWorld = (WorldServer) getWorld();
//
//        MinecraftServer server = originalWorld.getServer();
//        AnvilSaveHandler saveHandler = new AnvilSaveHandler(saveFolder, originalWorld.getSaveHandler().getWorldDirectory().getName(), server, server.getDataFixer());
//        World freshWorld = new WorldServer(server, saveHandler, originalWorld.getSavedDataStorage(), originalWorld.getWorldInfo(), originalWorld.dimension.getType(), originalWorld.profiler).func_212251_i__();
//
//        // Pre-gen all the chunks
//        // We need to also pull one more chunk in every direction
//        CuboidRegion expandedPreGen = new CuboidRegion(region.getMinimumPoint().subtract(16, 0, 16), region.getMaximumPoint().add(16, 0, 16));
//        for (BlockVector2 chunk : expandedPreGen.getChunks()) {
//            freshWorld.getChunk(chunk.getBlockX(), chunk.getBlockZ());
//        }
//
//        ForgeWorld from = new ForgeWorld(freshWorld);
//        try {
//            for (BlockVector3 vec : region) {
//                editSession.setBlock(vec, from.getFullBlock(vec));
//            }
//        } catch (MaxChangedBlocksException e) {
//            throw new RuntimeException(e);
//        } finally {
//            saveFolder.delete();
//            DimensionManager.setWorld(originalWorld.dimension.getType(), null, server);
//            DimensionManager.setWorld(originalWorld.dimension.getType(), originalWorld, server);
//        }
//
//        return true;
    }

    @Nullable
    private static Feature<NoFeatureConfig> createTreeFeatureGenerator(TreeType type) {
        switch (type) {
            case TREE: return new TreeFeature(true);
            case BIG_TREE: return new BigTreeFeature(true);
            case PINE:
            case REDWOOD: return new PointyTaigaTreeFeature();
            case TALL_REDWOOD: return new TallTaigaTreeFeature(true);
            case BIRCH: return new BirchTreeFeature(true, false);
            case JUNGLE: return new MegaJungleFeature(true, 10, 20, JUNGLE_LOG, JUNGLE_LEAF);
            case SMALL_JUNGLE: return new JungleTreeFeature(true, 4 + random.nextInt(7), JUNGLE_LOG, JUNGLE_LEAF, false);
            case SHORT_JUNGLE: return new JungleTreeFeature(true, 4 + random.nextInt(7), JUNGLE_LOG, JUNGLE_LEAF, true);
            case JUNGLE_BUSH: return new ShrubFeature(JUNGLE_LOG, JUNGLE_SHRUB);
            case RED_MUSHROOM: return new BigBrownMushroomFeature();
            case BROWN_MUSHROOM: return new BigRedMushroomFeature();
            case SWAMP: return new SwampTreeFeature();
            case ACACIA: return new SavannaTreeFeature(true);
            case DARK_OAK: return new CanopyTreeFeature(true);
            case MEGA_REDWOOD: return new MegaPineTree(false, random.nextBoolean());
            case TALL_BIRCH: return new BirchTreeFeature(true, true);
            case RANDOM: return createTreeFeatureGenerator(TreeType.values()[ThreadLocalRandom.current().nextInt(TreeType.values().length)]);
            case RANDOM_REDWOOD:
            default:
                return null;
        }
    }

    @Override
    public boolean generateTree(TreeType type, EditSession editSession, BlockVector3 position) throws MaxChangedBlocksException {
        Feature<NoFeatureConfig> generator = createTreeFeatureGenerator(type);
        return generator != null && generator.place(getWorld(), getWorld().getChunkProvider().getChunkGenerator(), random, ForgeAdapter.toBlockPos(position), new NoFeatureConfig());
    }

    @Override
    public void checkLoadedChunk(BlockVector3 pt) {
        getWorld().getChunk(ForgeAdapter.toBlockPos(pt));
    }

    @Override
    public void fixAfterFastMode(Iterable<BlockVector2> chunks) {
        fixLighting(chunks);
    }

    @Override
    public void fixLighting(Iterable<BlockVector2> chunks) {
        World world = getWorld();
        for (BlockVector2 chunk : chunks) {
            world.getChunk(chunk.getBlockX(), chunk.getBlockZ()).resetRelightChecks();
        }
    }

    @Override
    public boolean playEffect(Vector3 position, int type, int data) {
        getWorld().playEvent(type, ForgeAdapter.toBlockPos(position.toBlockPoint()), data);
        return true;
    }

    @Override
    public WeatherType getWeather() {
        WorldInfo info = getWorld().getWorldInfo();
        if (info.isThundering()) {
            return WeatherTypes.THUNDER_STORM;
        }
        if (info.isRaining()) {
            return WeatherTypes.RAIN;
        }
        return WeatherTypes.CLEAR;
    }

    @Override
    public long getRemainingWeatherDuration() {
        WorldInfo info = getWorld().getWorldInfo();
        if (info.isThundering()) {
            return info.getThunderTime();
        }
        if (info.isRaining()) {
            return info.getRainTime();
        }
        return info.getClearWeatherTime();
    }

    @Override
    public void setWeather(WeatherType weatherType) {
        setWeather(weatherType, 0);
    }

    @Override
    public void setWeather(WeatherType weatherType, long duration) {
        WorldInfo info = getWorld().getWorldInfo();
        if (weatherType == WeatherTypes.THUNDER_STORM) {
            info.setClearWeatherTime(0);
            info.setThundering(true);
            info.setThunderTime((int) duration);
        } else if (weatherType == WeatherTypes.RAIN) {
            info.setClearWeatherTime(0);
            info.setRaining(true);
            info.setRainTime((int) duration);
        } else if (weatherType == WeatherTypes.CLEAR) {
            info.setRaining(false);
            info.setThundering(false);
            info.setClearWeatherTime((int) duration);
        }
    }

    @Override
    public BlockVector3 getSpawnPosition() {
        return ForgeAdapter.adapt(getWorld().getSpawnPoint());
    }

    @Override
    public BlockState getBlock(BlockVector3 position) {
        IBlockState mcState = getWorld().getChunk(position.getBlockX() >> 4, position.getBlockZ() >> 4).getBlockState(
                position.getBlockX(),
                position.getBlockY(),
                position.getBlockZ()
        );

        return ForgeAdapter.adapt(mcState);
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        BlockPos pos = new BlockPos(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        TileEntity tile = getWorld().getTileEntity(pos);

        if (tile != null) {
            return getBlock(position).toBaseBlock(NBTConverter.fromNative(TileEntityUtils.copyNbtData(tile)));
        } else {
            return getBlock(position).toBaseBlock();
        }
    }

    @Override
    public int hashCode() {
        return getWorld().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if ((o instanceof ForgeWorld)) {
            ForgeWorld other = ((ForgeWorld) o);
            World otherWorld = other.worldRef.get();
            World thisWorld = worldRef.get();
            return otherWorld != null && otherWorld.equals(thisWorld);
        } else if (o instanceof com.sk89q.worldedit.world.World) {
            return ((com.sk89q.worldedit.world.World) o).getName().equals(getName());
        } else {
            return false;
        }
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        List<Entity> entities = new ArrayList<>();
        for (net.minecraft.entity.Entity entity : getWorld().loadedEntityList) {
            if (region.contains(BlockVector3.at(entity.posX, entity.posY, entity.posZ))) {
                entities.add(new ForgeEntity(entity));
            }
        }
        return entities;
    }

    @Override
    public List<? extends Entity> getEntities() {
        List<Entity> entities = new ArrayList<>();
        for (net.minecraft.entity.Entity entity : getWorld().loadedEntityList) {
            entities.add(new ForgeEntity(entity));
        }
        return entities;
    }

    @Nullable
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        World world = getWorld();
        net.minecraft.entity.Entity createdEntity = EntityType.create(world, new ResourceLocation(entity.getType().getId()));
        if (createdEntity != null) {
            CompoundTag nativeTag = entity.getNbtData();
            if (nativeTag != null) {
                NBTTagCompound tag = NBTConverter.toNative(entity.getNbtData());
                for (String name : Constants.NO_COPY_ENTITY_NBT_FIELDS) {
                    tag.remove(name);
                }
                createdEntity.read(tag);
            }

            createdEntity.setLocationAndAngles(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

            world.spawnEntity(createdEntity);
            return new ForgeEntity(createdEntity);
        } else {
            return null;
        }
    }

    /**
     * Thrown when the reference to the world is lost.
     */
    @SuppressWarnings("serial")
    private static class WorldReferenceLostException extends WorldEditException {
        private WorldReferenceLostException(String message) {
            super(message);
        }
    }

}
