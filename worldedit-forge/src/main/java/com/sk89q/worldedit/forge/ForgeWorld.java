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

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.blocks.LazyBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.TreeGenerator.TreeType;
import com.sk89q.worldedit.world.AbstractWorld;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.registry.WorldData;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;
import net.minecraft.world.gen.feature.WorldGenBigTree;
import net.minecraft.world.gen.feature.WorldGenCanopyTree;
import net.minecraft.world.gen.feature.WorldGenForest;
import net.minecraft.world.gen.feature.WorldGenMegaJungle;
import net.minecraft.world.gen.feature.WorldGenMegaPineTree;
import net.minecraft.world.gen.feature.WorldGenSavannaTree;
import net.minecraft.world.gen.feature.WorldGenShrub;
import net.minecraft.world.gen.feature.WorldGenSwamp;
import net.minecraft.world.gen.feature.WorldGenTaiga1;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenTrees;
import net.minecraft.world.gen.feature.WorldGenerator;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An adapter to Minecraft worlds for WorldEdit.
 */
public class ForgeWorld extends AbstractWorld {

    private static final Logger logger = Logger.getLogger(ForgeWorld.class.getCanonicalName());
    private static final Random random = new Random();
    private final WeakReference<World> worldRef;

    /**
     * Construct a new world.
     *
     * @param world the world
     */
    ForgeWorld(World world) {
        checkNotNull(world);
        this.worldRef = new WeakReference<World>(world);
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
    public boolean useItem(Vector position, BaseItem item, Direction face) {
        Item nativeItem = Item.getItemById(item.getType());
        ItemStack stack = new ItemStack(nativeItem, 1, item.getData());
        World world = getWorld();
        return stack.tryPlaceItemIntoWorld(new WorldEditFakePlayer((WorldServer) world), world, position.getBlockX(), position.getBlockY(), position.getBlockZ(), ForgeAdapter.adapt(face), 0, 0, 0);
    }

    @Override
    public boolean setBlock(Vector position, BaseBlock block, boolean notifyAndLight) throws WorldEditException {
        checkNotNull(position);
        checkNotNull(block);

        World world = getWorldChecked();
        int x = position.getBlockX();
        int y = position.getBlockY();
        int z = position.getBlockZ();

        // First set the block
        Chunk chunk = world.getChunkFromChunkCoords(x >> 4, z >> 4);
        int previousId = 0;

        if (notifyAndLight) {
            previousId = Block.getIdFromBlock(chunk.getBlock(x & 15, y, z & 15));
        }

        boolean successful = chunk.func_150807_a(x & 15, y, z & 15, Block.getBlockById(block.getId()), block.getData());

        // Create the TileEntity
        if (successful) {
            CompoundTag tag = block.getNbtData();
            if (tag != null) {
                NBTTagCompound nativeTag = NBTConverter.toNative(tag);
                nativeTag.setString("id", block.getNbtId());
                TileEntityUtils.setTileEntity(getWorld(), position, nativeTag);
            }
        }

        if (notifyAndLight) {
            world.func_147451_t(x, y, z);
            world.markBlockForUpdate(x, y, z);
            world.notifyBlockChange(x, y, z, Block.getBlockById(previousId));

            Block mcBlock = Block.getBlockById(previousId);
            if (mcBlock != null && mcBlock.hasComparatorInputOverride()) {
                world.func_147453_f(x, y, z, Block.getBlockById(block.getId()));
            }
        }

        return successful;
    }

    @Override
    public int getBlockLightLevel(Vector position) {
        checkNotNull(position);
        return getWorld().getBlockLightValue(position.getBlockX(), position.getBlockY(), position.getBlockZ());
    }

    @Override
    public boolean clearContainerBlockContents(Vector position) {
        checkNotNull(position);
        TileEntity tile = getWorld().getTileEntity(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        if ((tile instanceof IInventory)) {
            IInventory inv = (IInventory) tile;
            int size = inv.getSizeInventory();
            for (int i = 0; i < size; i++) {
                inv.setInventorySlotContents(i, null);
            }
            return true;
        }
        return false;
    }

    @Override
    public BaseBiome getBiome(Vector2D position) {
        checkNotNull(position);
        return new BaseBiome(getWorld().getBiomeGenForCoords(position.getBlockX(), position.getBlockZ()).biomeID);
    }

    @Override
    public boolean setBiome(Vector2D position, BaseBiome biome) {
        checkNotNull(position);
        checkNotNull(biome);

        Chunk chunk = getWorld().getChunkFromBlockCoords(position.getBlockX(), position.getBlockZ());
        if ((chunk != null) && (chunk.isChunkLoaded)) {
            chunk.getBiomeArray()[((position.getBlockZ() & 0xF) << 4 | position.getBlockX() & 0xF)] = (byte) biome.getId();
            return true;
        }

        return false;
    }

    @Override
    public void dropItem(Vector position, BaseItemStack item) {
        checkNotNull(position);
        checkNotNull(item);

        if (item.getType() == 0) {
            return;
        }

        EntityItem entity = new EntityItem(getWorld(), position.getX(), position.getY(), position.getZ(), ForgeWorldEdit.toForgeItemStack(item));
        entity.delayBeforeCanPickup = 10;
        getWorld().spawnEntityInWorld(entity);
    }

    @Override
    public boolean regenerate(Region region, EditSession editSession) {
        BaseBlock[] history = new BaseBlock[256 * (getMaxY() + 1)];

        for (Vector2D chunk : region.getChunks()) {
            Vector min = new Vector(chunk.getBlockX() * 16, 0, chunk.getBlockZ() * 16);

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < getMaxY() + 1; y++) {
                    for (int z = 0; z < 16; z++) {
                        Vector pt = min.add(x, y, z);
                        int index = y * 16 * 16 + z * 16 + x;
                        history[index] = editSession.getBlock(pt);
                    }
                }
            }
            try {
                Set<Vector2D> chunks = region.getChunks();
                IChunkProvider provider = getWorld().getChunkProvider();
                if (!(provider instanceof ChunkProviderServer)) {
                    return false;
                }
                ChunkProviderServer chunkServer = (ChunkProviderServer) provider;
                Field u;
                try {
                    u = ChunkProviderServer.class.getDeclaredField("field_73248_b"); // chunksToUnload
                } catch(NoSuchFieldException e) {
                    u = ChunkProviderServer.class.getDeclaredField("chunksToUnload");
                }
                u.setAccessible(true);
                Set<?> unloadQueue = (Set<?>) u.get(chunkServer);
                Field m;
                try {
                    m = ChunkProviderServer.class.getDeclaredField("field_73244_f"); // loadedChunkHashMap
                } catch(NoSuchFieldException e) {
                    m = ChunkProviderServer.class.getDeclaredField("loadedChunkHashMap");
                }
                m.setAccessible(true);
                LongHashMap loadedMap = (LongHashMap) m.get(chunkServer);
                Field lc;
                try {
                    lc = ChunkProviderServer.class.getDeclaredField("field_73245_g"); // loadedChunkHashMap
                } catch(NoSuchFieldException e) {
                    lc = ChunkProviderServer.class.getDeclaredField("loadedChunks");
                }
                lc.setAccessible(true);
                @SuppressWarnings("unchecked") List<Chunk> loaded = (List<Chunk>) lc.get(chunkServer);
                Field p;
                try {
                    p = ChunkProviderServer.class.getDeclaredField("field_73246_d"); // currentChunkProvider
                } catch(NoSuchFieldException e) {
                    p = ChunkProviderServer.class.getDeclaredField("currentChunkProvider");
                }
                p.setAccessible(true);
                IChunkProvider chunkProvider = (IChunkProvider) p.get(chunkServer);

                for (Vector2D coord : chunks) {
                    long pos = ChunkCoordIntPair.chunkXZ2Int(coord.getBlockX(), coord.getBlockZ());
                    Chunk mcChunk;
                    if (chunkServer.chunkExists(coord.getBlockX(), coord.getBlockZ())) {
                        mcChunk = chunkServer.loadChunk(coord.getBlockX(), coord.getBlockZ());
                        mcChunk.onChunkUnload();
                    }
                    unloadQueue.remove(pos);
                    loadedMap.remove(pos);
                    mcChunk = chunkProvider.provideChunk(coord.getBlockX(), coord.getBlockZ());
                    loadedMap.add(pos, mcChunk);
                    loaded.add(mcChunk);
                    if (mcChunk != null) {
                        mcChunk.onChunkLoad();
                        mcChunk.populateChunk(chunkProvider, chunkProvider, coord.getBlockX(), coord.getBlockZ());
                    }
                }
            } catch (Throwable t) {
                logger.log(Level.WARNING, "Failed to generate chunk", t);
                return false;
            }

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < getMaxY() + 1; y++) {
                    for (int z = 0; z < 16; z++) {
                        Vector pt = min.add(x, y, z);
                        int index = y * 16 * 16 + z * 16 + x;

                        if (!region.contains(pt))
                            editSession.smartSetBlock(pt, history[index]);
                        else {
                            editSession.rememberChange(pt, history[index], editSession.rawGetBlock(pt));
                        }
                    }
                }
            }
        }

        return false;
    }

    @Nullable
    private static WorldGenerator createWorldGenerator(TreeType type) {
        switch (type) {
            case TREE: return new WorldGenTrees(true);
            case BIG_TREE: return new WorldGenBigTree(true);
            case REDWOOD: return new WorldGenTaiga2(true);
            case TALL_REDWOOD: return new WorldGenTaiga1();
            case BIRCH: return new WorldGenForest(true, false);
            case JUNGLE: return new WorldGenMegaJungle(true, 10, 20, 3, 3);
            case SMALL_JUNGLE: return new WorldGenTrees(true, 4 + random.nextInt(7), 3, 3, false);
            case SHORT_JUNGLE: return new WorldGenTrees(true, 4 + random.nextInt(7), 3, 3, true);
            case JUNGLE_BUSH: return new WorldGenShrub(3, 0);
            case RED_MUSHROOM: return new WorldGenBigMushroom(1);
            case BROWN_MUSHROOM: return new WorldGenBigMushroom(0);
            case SWAMP: return new WorldGenSwamp();
            case ACACIA: return new WorldGenSavannaTree(true);
            case DARK_OAK: return new WorldGenCanopyTree(true);
            case MEGA_REDWOOD: return new WorldGenMegaPineTree(false, random.nextBoolean());
            case TALL_BIRCH: return new WorldGenForest(true, true);
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
        return generator != null ? generator.generate(getWorld(), random, position.getBlockX(), position.getBlockY(), position.getBlockZ()) : false;
    }

    @Override
    public WorldData getWorldData() {
        return ForgeWorldData.getInstance();
    }

    @Override
    public boolean isValidBlockType(int id) {
        return (id == 0) || (net.minecraft.block.Block.getBlockById(id) != null);
    }

    @Override
    public BaseBlock getBlock(Vector position) {
        World world = getWorld();
        int id = Block.getIdFromBlock(world.getBlock(position.getBlockX(), position.getBlockY(), position.getBlockZ()));
        int data = world.getBlockMetadata(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        TileEntity tile = getWorld().getTileEntity(position.getBlockX(), position.getBlockY(), position.getBlockZ());

        if (tile != null) {
            return new TileEntityBaseBlock(id, data, tile);
        } else {
            return new BaseBlock(id, data);
        }
    }

    @Override
    public BaseBlock getLazyBlock(Vector position) {
        World world = getWorld();
        int id = Block.getIdFromBlock(world.getBlock(position.getBlockX(), position.getBlockY(), position.getBlockZ()));
        int data = world.getBlockMetadata(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        return new LazyBlock(id, data, this, position);
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
            return otherWorld != null && thisWorld != null && otherWorld.equals(thisWorld);
        } else if (o instanceof com.sk89q.worldedit.world.World) {
            return ((com.sk89q.worldedit.world.World) o).getName().equals(getName());
        } else {
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<? extends Entity> getEntities(Region region) {
        List<Entity> entities = new ArrayList<Entity>();
        World world = getWorld();
        List<net.minecraft.entity.Entity> ents = world.loadedEntityList;
        for (net.minecraft.entity.Entity entity : ents) {
            if (region.contains(new Vector(entity.posX, entity.posY, entity.posZ))) {
                entities.add(new ForgeEntity(entity));
            }
        }
        return entities;
    }

    @Override
    public List<? extends Entity> getEntities() {
        List<Entity> entities = new ArrayList<Entity>();
        for (Object entity : getWorld().loadedEntityList) {
            entities.add(new ForgeEntity((net.minecraft.entity.Entity) entity));
        }
        return entities;
    }

    @Nullable
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        World world = getWorld();
        net.minecraft.entity.Entity createdEntity = EntityList.createEntityByName(entity.getTypeId(), world);
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

            world.spawnEntityInWorld(createdEntity);
            return new ForgeEntity(createdEntity);
        } else {
            return null;
        }
    }

    /**
     * Thrown when the reference to the world is lost.
     */
    private static class WorldReferenceLostException extends WorldEditException {
        private WorldReferenceLostException(String message) {
            super(message);
        }
    }

}
