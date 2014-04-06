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
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.blocks.LazyBlock;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.TreeGenerator.TreeType;
import com.sk89q.worldedit.world.AbstractWorld;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.*;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An adapter to Minecraft worlds for WorldEdit.
 */
public class ForgeWorld extends AbstractWorld {

    private static final Logger logger = Logger.getLogger(ForgeWorld.class.getCanonicalName());
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
        return getWorld().provider.getDimensionName();
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
            previousId = chunk.getBlockID(x & 15, y, z & 15);
        }

        boolean successful = chunk.setBlockIDWithMetadata(x & 15, y, z & 15, block.getId(), block.getData());

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
            world.updateAllLightTypes(x, y, z);
            world.markBlockForUpdate(x, y, z);
            world.notifyBlockChange(x, y, z, previousId);

            Block mcBlock = Block.blocksList[block.getId()];
            if (mcBlock != null && mcBlock.hasComparatorInputOverride()) {
                world.func_96440_m(x, y, z, block.getId());
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
        TileEntity tile = getWorld().getBlockTileEntity(position.getBlockX(), position.getBlockY(), position.getBlockZ());
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
    public BiomeType getBiome(Vector2D position) {
        checkNotNull(position);
        return ForgeBiomeTypes.getFromBaseBiome(getWorld().getBiomeGenForCoords(position.getBlockX(), position.getBlockZ()));
    }

    @Override
    public void setBiome(Vector2D position, BiomeType biome) {
        checkNotNull(position);
        checkNotNull(biome);

        if (getWorld().getChunkProvider().chunkExists(position.getBlockX(), position.getBlockZ())) {
            Chunk chunk = getWorld().getChunkFromBlockCoords(position.getBlockX(), position.getBlockZ());
            if ((chunk != null) && (chunk.isChunkLoaded)) {
                chunk.getBiomeArray()[((position.getBlockZ() & 0xF) << 4 | position.getBlockX() & 0xF)] = (byte) ForgeBiomeTypes.getFromBiomeType(biome).biomeID;
            }
        }
    }

    @Override
    public void dropItem(Vector position, BaseItemStack item) {
        checkNotNull(position);
        checkNotNull(item);

        if ((item == null) || (item.getType() == 0)) {
            return;
        }

        EntityItem entity = new EntityItem(getWorld(), position.getX(), position.getY(), position.getZ(), ForgeUtil.toForgeItemStack(item));
        entity.delayBeforeCanPickup = 10;
        getWorld().spawnEntityInWorld(entity);
    }

    @Override
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public int killMobs(Vector origin, double radius, int flags) {
        boolean killPets = (flags & 0x1) != 0;
        boolean killNPCs = (flags & 0x2) != 0;
        boolean killAnimals = (flags & 0x4) != 0;

        boolean killGolems = (flags & 0x8) != 0;
        boolean killAmbient = (flags & 0x10) != 0;

        int num = 0;
        double radiusSq = radius * radius;

        for (Entity obj : (Iterable<Entity>) getWorld().loadedEntityList) {
            if ((obj instanceof EntityLiving)) {
                EntityLiving ent = (EntityLiving) obj;

                if (!killAnimals && ent instanceof EntityAnimal) {
                    continue;
                }

                if (!killPets && ent instanceof EntityTameable && ((EntityTameable) ent).isTamed()) {
                    continue; // tamed pet
                }

                if (!killGolems && ent instanceof EntityGolem) {
                    continue;
                }

                if (!killNPCs && ent instanceof EntityVillager) {
                    continue;
                }

                if (!killAmbient && ent instanceof EntityAmbientCreature) {
                    continue;
                }

                if ((radius < 0.0D) || (origin.distanceSq(new Vector(ent.posX, ent.posY, ent.posZ)) <= radiusSq)) {
                    ent.isDead = true;
                    num++;
                }
            }
        }

        return num;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int removeEntities(EntityType type, Vector origin, int radius) {
        checkNotNull(type);
        checkNotNull(origin);

        int num = 0;
        double radiusSq = Math.pow(radius, 2.0D);

        for (Entity ent : (Iterable<Entity>) getWorld().loadedEntityList) {
            if ((radius != -1) && (origin.distanceSq(new Vector(ent.posX, ent.posY, ent.posZ)) > radiusSq)) {
                continue;
            }
            if (type == EntityType.ALL) {
                if (((ent instanceof EntityBoat)) || ((ent instanceof EntityItem)) || ((ent instanceof EntityFallingSand)) || ((ent instanceof EntityMinecart)) || ((ent instanceof EntityHanging)) || ((ent instanceof EntityTNTPrimed)) || ((ent instanceof EntityXPOrb)) || ((ent instanceof EntityEnderEye)) || ((ent instanceof IProjectile))) {
                    ent.isDead = true;
                    num++;
                }
            } else if ((type == EntityType.PROJECTILES) || (type == EntityType.ARROWS)) {
                if (((ent instanceof EntityEnderEye)) || ((ent instanceof IProjectile))) {
                    ent.isDead = true;
                    num++;
                }
            } else if (type == EntityType.BOATS) {
                if ((ent instanceof EntityBoat)) {
                    ent.isDead = true;
                    num++;
                }
            } else if (type == EntityType.ITEMS) {
                if ((ent instanceof EntityItem)) {
                    ent.isDead = true;
                    num++;
                }
            } else if (type == EntityType.FALLING_BLOCKS) {
                if ((ent instanceof EntityFallingSand)) {
                    ent.isDead = true;
                    num++;
                }
            } else if (type == EntityType.MINECARTS) {
                if ((ent instanceof EntityMinecart)) {
                    ent.isDead = true;
                    num++;
                }
            } else if (type == EntityType.PAINTINGS) {
                if ((ent instanceof EntityPainting)) {
                    ent.isDead = true;
                    num++;
                }
            } else if (type == EntityType.ITEM_FRAMES) {
                if ((ent instanceof EntityItemFrame)) {
                    ent.isDead = true;
                    num++;
                }
            } else if (type == EntityType.TNT) {
                if ((ent instanceof EntityTNTPrimed)) {
                    ent.isDead = true;
                    num++;
                }
            } else if ((type == EntityType.XP_ORBS) && ((ent instanceof EntityXPOrb))) {
                ent.isDead = true;
                num++;
            }

        }

        return num;
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

    @Override
    public boolean generateTree(TreeType type, EditSession editSession, Vector position) throws MaxChangedBlocksException {
        return false;
    }

    @Override
    public boolean isValidBlockType(int id) {
        return (id == 0) || (net.minecraft.block.Block.blocksList[id] != null);
    }

    @Override
    public BaseBlock getBlock(Vector position) {
        World world = getWorld();
        int id = world.getBlockId(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        int data = world.getBlockMetadata(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        TileEntity tile = getWorld().getBlockTileEntity(position.getBlockX(), position.getBlockY(), position.getBlockZ());

        if (tile != null) {
            return new TileEntityBaseBlock(id, data, tile);
        } else {
            return new BaseBlock(id, data);
        }
    }

    @Override
    public BaseBlock getLazyBlock(Vector position) {
        World world = getWorld();
        int id = world.getBlockId(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        int data = world.getBlockMetadata(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        return new LazyBlock(id, data, this, position);
    }

    @Override
    public int hashCode() {
        return getWorld().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if ((o instanceof ForgeWorld)) {
            ForgeWorld other = ((ForgeWorld) o);
            World otherWorld = other.worldRef.get();
            World thisWorld = other.worldRef.get();
            return otherWorld != null && thisWorld != null && otherWorld.equals(thisWorld);
        } else {
            return false;
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