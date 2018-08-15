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

import com.google.common.io.Files;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.TreeGenerator.TreeType;
import com.sk89q.worldedit.world.AbstractWorld;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import com.sk89q.worldedit.world.weather.WeatherType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilSaveHandler;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;
import net.minecraft.world.gen.feature.WorldGenBigTree;
import net.minecraft.world.gen.feature.WorldGenBirchTree;
import net.minecraft.world.gen.feature.WorldGenCanopyTree;
import net.minecraft.world.gen.feature.WorldGenMegaJungle;
import net.minecraft.world.gen.feature.WorldGenMegaPineTree;
import net.minecraft.world.gen.feature.WorldGenSavannaTree;
import net.minecraft.world.gen.feature.WorldGenShrub;
import net.minecraft.world.gen.feature.WorldGenSwamp;
import net.minecraft.world.gen.feature.WorldGenTaiga1;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenTrees;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.DimensionManager;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

/**
 * An adapter to Minecraft worlds for WorldEdit.
 */
public class ForgeWorld extends AbstractWorld {

    private static final Random random = new Random();
    private static final int UPDATE = 1, NOTIFY = 2;

    private static final IBlockState JUNGLE_LOG = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE);
    private static final IBlockState JUNGLE_LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));
    private static final IBlockState JUNGLE_SHRUB = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));
    
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
    public boolean setBlock(Vector position, BlockStateHolder block, boolean notifyAndLight) throws WorldEditException {
        checkNotNull(position);
        checkNotNull(block);

        World world = getWorldChecked();
        int x = position.getBlockX();
        int y = position.getBlockY();
        int z = position.getBlockZ();

        // First set the block
        Chunk chunk = world.getChunkFromChunkCoords(x >> 4, z >> 4);
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState old = chunk.getBlockState(pos);
        IBlockState newState = Block.getBlockById(block.getBlockType().getLegacyId()).getDefaultState(); // TODO .getStateFromMeta(block.getData());
        IBlockState successState = chunk.setBlockState(pos, newState);
        boolean successful = successState != null;

        // Create the TileEntity
        if (successful) {
            if (block instanceof BaseBlock && ((BaseBlock) block).hasNbtData()) {
                // Kill the old TileEntity
                world.removeTileEntity(pos);
                NBTTagCompound nativeTag = NBTConverter.toNative(((BaseBlock) block).getNbtData());
                nativeTag.setString("id", ((BaseBlock) block).getNbtId());
                TileEntityUtils.setTileEntity(world, position, nativeTag);
            }
        }

        if (notifyAndLight) {
            if (!successful) {
                newState = old;
            }
            world.checkLight(pos);
            world.markAndNotifyBlock(pos, chunk, old, newState, UPDATE | NOTIFY);
        }

        return successful;
    }

    @Override
    public int getBlockLightLevel(Vector position) {
        checkNotNull(position);
        return getWorld().getLight(new BlockPos(position.getBlockX(), position.getBlockY(), position.getBlockZ()));
    }

    @Override
    public boolean clearContainerBlockContents(Vector position) {
        checkNotNull(position);
        TileEntity tile = getWorld().getTileEntity(new BlockPos(position.getBlockX(), position.getBlockY(), position.getBlockZ()));
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
    public BaseBiome getBiome(Vector2D position) {
        checkNotNull(position);
        return new BaseBiome(Biome.getIdForBiome(getWorld().getBiomeForCoordsBody(new BlockPos(position.getBlockX(), 0, position.getBlockZ()))));
    }

    @Override
    public boolean setBiome(Vector2D position, BaseBiome biome) {
        checkNotNull(position);
        checkNotNull(biome);

        Chunk chunk = getWorld().getChunkFromBlockCoords(new BlockPos(position.getBlockX(), 0, position.getBlockZ()));
        if (chunk.isLoaded()) {
            chunk.getBiomeArray()[((position.getBlockZ() & 0xF) << 4 | position.getBlockX() & 0xF)] = (byte) biome.getId();
            return true;
        }

        return false;
    }

    @Override
    public boolean useItem(Vector position, BaseItem item, Direction face) {
        Item nativeItem = Item.getByNameOrId(item.getType().getId());
        ItemStack stack = null;
        if (item.getNbtData() == null) {
            stack = new ItemStack(nativeItem, 1, 0);
        } else {
            stack = new ItemStack(nativeItem, 1, 0, NBTConverter.toNative(item.getNbtData()));
        }
        World world = getWorld();
        EnumActionResult used = stack.onItemUse(new WorldEditFakePlayer((WorldServer) world), world, ForgeAdapter.toBlockPos(position),
                EnumHand.MAIN_HAND, ForgeAdapter.adapt(face), 0, 0, 0);
        return used != EnumActionResult.FAIL;
    }

    @Override
    public void dropItem(Vector position, BaseItemStack item) {
        checkNotNull(position);
        checkNotNull(item);

        if (item.getType() == ItemTypes.AIR) {
            return;
        }

        EntityItem entity = new EntityItem(getWorld(), position.getX(), position.getY(), position.getZ(), ForgeWorldEdit.toForgeItemStack(item));
        entity.setPickupDelay(10);
        getWorld().spawnEntity(entity);
    }

    @Override
    public void simulateBlockMine(Vector position) {
        BlockPos pos = ForgeAdapter.toBlockPos(position);
        IBlockState state = getWorld().getBlockState(pos);
        state.getBlock().dropBlockAsItem(getWorld(), pos, state, 0);
        getWorld().setBlockToAir(pos);
    }

    @Override
    public boolean regenerate(Region region, EditSession editSession) {
        // Don't even try to regen if it's going to fail.
        IChunkProvider provider = getWorld().getChunkProvider();
        if (!(provider instanceof ChunkProviderServer)) {
            return false;
        }
        
        File saveFolder = Files.createTempDir();
        // register this just in case something goes wrong
        // normally it should be deleted at the end of this method
        saveFolder.deleteOnExit();

        WorldServer originalWorld = (WorldServer) getWorld();

        MinecraftServer server = originalWorld.getMinecraftServer();
        AnvilSaveHandler saveHandler = new AnvilSaveHandler(saveFolder,
                originalWorld.getSaveHandler().getWorldDirectory().getName(), true, server.getDataFixer());
        World freshWorld = new WorldServer(server, saveHandler, originalWorld.getWorldInfo(),
                originalWorld.provider.getDimension(), originalWorld.profiler).init();

        // Pre-gen all the chunks
        // We need to also pull one more chunk in every direction
        CuboidRegion expandedPreGen = new CuboidRegion(region.getMinimumPoint().subtract(16, 0, 16), region.getMaximumPoint().add(16, 0, 16));
        for (Vector2D chunk : expandedPreGen.getChunks()) {
            freshWorld.getChunkFromChunkCoords(chunk.getBlockX(), chunk.getBlockZ());
        }
        
        ForgeWorld from = new ForgeWorld(freshWorld);
        try {
            for (BlockVector vec : region) {
                editSession.setBlock(vec, from.getFullBlock(vec));
            }
        } catch (MaxChangedBlocksException e) {
            throw new RuntimeException(e);
        } finally {
            saveFolder.delete();
            DimensionManager.setWorld(originalWorld.provider.getDimension(), null, server);
            DimensionManager.setWorld(originalWorld.provider.getDimension(), originalWorld, server);
        }

        return true;
    }

    @Nullable
    private static WorldGenerator createWorldGenerator(TreeType type) {
        switch (type) {
            case TREE: return new WorldGenTrees(true);
            case BIG_TREE: return new WorldGenBigTree(true);
            case REDWOOD: return new WorldGenTaiga2(true);
            case TALL_REDWOOD: return new WorldGenTaiga1();
            case BIRCH: return new WorldGenBirchTree(true, false);
            case JUNGLE: return new WorldGenMegaJungle(true, 10, 20, JUNGLE_LOG, JUNGLE_LEAF);
            case SMALL_JUNGLE: return new WorldGenTrees(true, 4 + random.nextInt(7), JUNGLE_LOG, JUNGLE_LEAF, false);
            case SHORT_JUNGLE: return new WorldGenTrees(true, 4 + random.nextInt(7), JUNGLE_LOG, JUNGLE_LEAF, true);
            case JUNGLE_BUSH: return new WorldGenShrub(JUNGLE_LOG, JUNGLE_SHRUB);
            case RED_MUSHROOM: return new WorldGenBigMushroom(Blocks.BROWN_MUSHROOM_BLOCK);
            case BROWN_MUSHROOM: return new WorldGenBigMushroom(Blocks.RED_MUSHROOM_BLOCK);
            case SWAMP: return new WorldGenSwamp();
            case ACACIA: return new WorldGenSavannaTree(true);
            case DARK_OAK: return new WorldGenCanopyTree(true);
            case MEGA_REDWOOD: return new WorldGenMegaPineTree(false, random.nextBoolean());
            case TALL_BIRCH: return new WorldGenBirchTree(true, true);
            case RANDOM:
            case PINE:
            case RANDOM_REDWOOD:
            default:
                return null;
        }
    }

    @Override
    public boolean generateTree(TreeType type, EditSession editSession, Vector position) throws MaxChangedBlocksException {
        WorldGenerator generator = createWorldGenerator(type);
        return generator != null && generator.generate(getWorld(), random, ForgeAdapter.toBlockPos(position));
    }

    @Override
    public WeatherType getWeather() {
        // TODO Weather implementation
        return null;
    }

    @Override
    public long getRemainingWeatherDuration() {
        return 0;
    }

    @Override
    public void setWeather(WeatherType weatherType) {

    }

    @Override
    public void setWeather(WeatherType weatherType, long duration) {

    }

    @Override
    public BlockState getBlock(Vector position) {
        World world = getWorld();
        BlockPos pos = new BlockPos(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        IBlockState state = world.getBlockState(pos);

        return LegacyMapper.getInstance().getBlockFromLegacy(Block.getIdFromBlock(state.getBlock()), state.getBlock().getMetaFromState(state));
    }

    @Override
    public BaseBlock getFullBlock(Vector position) {
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
            if (region.contains(new Vector(entity.posX, entity.posY, entity.posZ))) {
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
        net.minecraft.entity.Entity createdEntity = EntityList.createEntityByIDFromName(new ResourceLocation(entity.getType().getId()), world);
        if (createdEntity != null) {
            CompoundTag nativeTag = entity.getNbtData();
            if (nativeTag != null) {
                NBTTagCompound tag = NBTConverter.toNative(entity.getNbtData());
                for (String name : Constants.NO_COPY_ENTITY_NBT_FIELDS) {
                    tag.removeTag(name);
                }
                createdEntity.readFromNBT(tag);
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
