package com.sk89q.worldedit.forge;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.item.EntityFallingSand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;

import com.sk89q.worldedit.BiomeType;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EntityType;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.blocks.MobSpawnerBlock;
import com.sk89q.worldedit.blocks.NoteBlock;
import com.sk89q.worldedit.blocks.SignBlock;
import com.sk89q.worldedit.blocks.SkullBlock;
import com.sk89q.worldedit.blocks.TileEntityBlock;
import com.sk89q.worldedit.foundation.Block;
import com.sk89q.worldedit.regions.Region;

public class ForgeWorld extends LocalWorld {
    private WeakReference<World> world;
    
    public ForgeWorld(World world) {
        this.world = new WeakReference<World>(world);
    }

    public String getName() {
        return this.world.get().provider.getDimensionName();
    }

    public boolean setBlockType(Vector pt, int type) {
        return this.world.get().setBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), type, 0, 3);
    }

    public boolean setBlockTypeFast(Vector pt, int type) {
        return this.world.get().setBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), type);
    }

    public boolean setTypeIdAndData(Vector pt, int type, int data) {
        return this.world.get().setBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), type, data, 3);
    }

    public boolean setTypeIdAndDataFast(Vector pt, int type, int data) {
        return this.world.get().setBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), type, data, 3);
    }

    public int getBlockType(Vector pt) {
        return this.world.get().getBlockId(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
    }

    public void setBlockData(Vector pt, int data) {
        this.world.get().setBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), data, 0, 3);
    }

    public void setBlockDataFast(Vector pt, int data) {
        this.world.get().setBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), data, 0, 3);
    }

    public int getBlockData(Vector pt) {
        return this.world.get().getBlockMetadata(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
    }

    public int getBlockLightLevel(Vector pt) {
        return this.world.get().getBlockLightValue(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
    }

    public boolean isValidBlockType(int id) {
        return (id == 0) || (net.minecraft.block.Block.blocksList[id] != null);
    }

    public BiomeType getBiome(Vector2D pt) {
        return ForgeBiomeTypes.getFromBaseBiome(this.world.get().getBiomeGenForCoords(pt.getBlockX(), pt.getBlockZ()));
    }

    public void setBiome(Vector2D pt, BiomeType biome) {
        if (this.world.get().getChunkProvider().chunkExists(pt.getBlockX(), pt.getBlockZ())) {
            Chunk chunk = this.world.get().getChunkFromBlockCoords(pt.getBlockX(), pt.getBlockZ());
            if ((chunk != null) && (chunk.isChunkLoaded)) {
                byte[] biomevals = chunk.getBiomeArray();
                biomevals[((pt.getBlockZ() & 0xF) << 4 | pt.getBlockX() & 0xF)] = (byte) ForgeBiomeTypes.getFromBiomeType(biome).biomeID;
            }
        }
    }

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
                IChunkProvider provider = this.world.get().getChunkProvider();
                if (!(provider instanceof ChunkProviderServer)) {
                    return false;
                }
                ChunkProviderServer chunkServer = (ChunkProviderServer) provider;
                Field u = null;
                try {
                    u = ChunkProviderServer.class.getDeclaredField("field_73248_b"); // chunksToUnload
                } catch(NoSuchFieldException e) {
                    u = ChunkProviderServer.class.getDeclaredField("chunksToUnload");
                }
                u.setAccessible(true);
                Set<?> unloadQueue = (Set<?>) u.get(chunkServer);
                Field m = null;
                try {
                    m = ChunkProviderServer.class.getDeclaredField("field_73244_f"); // loadedChunkHashMap
                } catch(NoSuchFieldException e) {
                    m = ChunkProviderServer.class.getDeclaredField("loadedChunkHashMap");
                }
                m.setAccessible(true);
                LongHashMap loadedMap = (LongHashMap) m.get(chunkServer);
                Field lc = null;
                try {
                    lc = ChunkProviderServer.class.getDeclaredField("field_73245_g"); // loadedChunkHashMap
                } catch(NoSuchFieldException e) {
                    lc = ChunkProviderServer.class.getDeclaredField("loadedChunks");
                }
                lc.setAccessible(true);
                List<Chunk> loaded = (List<Chunk>) lc.get(chunkServer);
                Field p = null;
                try {
                    p = ChunkProviderServer.class.getDeclaredField("field_73246_d"); // currentChunkProvider
                } catch(NoSuchFieldException e) {
                    p = ChunkProviderServer.class.getDeclaredField("currentChunkProvider");
                }
                p.setAccessible(true);
                IChunkProvider chunkProvider = (IChunkProvider) p.get(chunkServer);

                for (Vector2D coord : chunks) {
                    long pos = ChunkCoordIntPair.chunkXZ2Int(coord.getBlockX(), coord.getBlockZ());
                    Chunk mcChunk = null;
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
                    }
                    mcChunk.populateChunk(chunkProvider, chunkProvider, coord.getBlockX(), coord.getBlockZ());
                }
            } catch (Throwable t) {
                t.printStackTrace();
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

    public boolean setBlock(Vector pt, Block block, boolean notify) {
        if (!(block instanceof BaseBlock)) {
            return false;
        }

        boolean successful = this.world.get().setBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), block.getId());

        if (successful) {
            if (block instanceof TileEntityBlock) {
                copyToWorld(pt, (BaseBlock) block);
            }
        }
        this.world.get().setBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), block.getId(), block.getData(), 3);
        return successful;
    }

    public BaseBlock getBlock(Vector pt) {
        int type = getBlockType(pt);
        int data = getBlockData(pt);
        TileEntity tile = this.world.get().getBlockTileEntity(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (tile != null) {
            TileEntityBaseBlock block = new TileEntityBaseBlock(type, data, tile);
            copyFromWorld(pt, block);

            return block;
        }
        return new BaseBlock(type, data);
    }

    public boolean copyToWorld(Vector pt, BaseBlock block) {
        return copyToWorld(pt, block, true);
    }

    public boolean copyToWorld(Vector pt, BaseBlock block, boolean hardcopy) {
        if (!(block instanceof TileEntityBlock)) {
            return false;
        }
        if (block instanceof SignBlock) {
            // Signs
            TileEntitySign sign = new TileEntitySign();
            sign.signText = ((SignBlock) block).getText();
            world.get().setBlockTileEntity(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), sign);
            return true;
        }

        if (block instanceof MobSpawnerBlock) {
            // Mob spawners
            TileEntityMobSpawner spawner = new TileEntityMobSpawner();
            spawner.getSpawnerLogic().setMobID(((MobSpawnerBlock) block).getMobType());
            world.get().setBlockTileEntity(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), spawner);
            return true;
        }

        if (block instanceof NoteBlock) {
            // Note block
            TileEntityNote note = new TileEntityNote();
            note.note = ((NoteBlock) block).getNote();
            world.get().setBlockTileEntity(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), note);
            return true;
        }

        if (block instanceof SkullBlock) {
            // Skull block
            TileEntitySkull skull = new TileEntitySkull();
            skull.setSkullType(((SkullBlock) block).getSkullType(), ((SkullBlock) block).getOwner());
            skull.setSkullRotation(((SkullBlock) block).getRot());
            world.get().setBlockTileEntity(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), skull);
            return true;
        }

        if (block instanceof TileEntityBaseBlock) {
            TileEntityBaseBlock.set(this.world.get(), pt, (TileEntityBaseBlock) block, hardcopy);
            return true;
        }
        return false;
    }

    public boolean copyFromWorld(Vector pt, BaseBlock block) {
        if (!(block instanceof TileEntityBaseBlock)) {
            return false;
        }
        ((TileEntityBaseBlock) block).setTile(this.world.get().getBlockTileEntity(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()));
        return true;
    }

    public boolean clearContainerBlockContents(Vector pt) {
        TileEntity tile = this.world.get().getBlockTileEntity(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
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

    public void dropItem(Vector pt, BaseItemStack item) {
        if ((item == null) || (item.getType() == 0)) {
            return;
        }
        EntityItem entity = new EntityItem(this.world.get(), pt.getX(), pt.getY(), pt.getZ(), ForgeUtil.toForgeItemStack(item));
        entity.delayBeforeCanPickup = 10;
        this.world.get().spawnEntityInWorld(entity);
    }

    public int removeEntities(EntityType type, Vector origin, int radius) {
        int num = 0;
        double radiusSq = Math.pow(radius, 2.0D);

        for (Iterator<Entity> it = this.world.get().loadedEntityList.iterator(); it.hasNext();) {
            Entity ent = it.next();
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

    public int killMobs(Vector origin, double radius, int flags) {
        boolean killPets = (flags & 0x1) != 0;
        boolean killNPCs = (flags & 0x2) != 0;
        boolean killAnimals = (flags & 0x4) != 0;

        boolean killGolems = (flags & 0x8) != 0;
        boolean killAmbient = (flags & 0x10) != 0;

        int num = 0;
        double radiusSq = radius * radius;

        for (Iterator<Entity> it = this.world.get().loadedEntityList.iterator(); it.hasNext();) {
            Entity obj = it.next();
            if (!(obj instanceof EntityLiving)) {
                continue;
            }
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

        return num;
    }

    public World getWorld() {
        return world.get();
    }

    public boolean equals(Object other) {
        if ((other instanceof ForgeWorld)) {
            return ((ForgeWorld) other).world.get().equals(this.world.get());
        }
        return false;
    }

    public int hashCode() {
        return this.world.get().hashCode();
    }
}