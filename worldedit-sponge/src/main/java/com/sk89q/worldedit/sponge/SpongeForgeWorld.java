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

package com.sk89q.worldedit.sponge;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.LazyBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.*;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.feature.*;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.entity.SpongeEntitySnapshot;
import org.spongepowered.common.entity.SpongeEntitySnapshotBuilder;
import scala.xml.Null;

import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Level;

import static com.google.common.base.Preconditions.checkNotNull;

public class SpongeForgeWorld extends SpongeWorld {

    private final SpongeBlockSnapshotBuilder blockBuilder = new SpongeBlockSnapshotBuilder();
    private final SpongeEntitySnapshotBuilder entityBuilder = new SpongeEntitySnapshotBuilder();

    private static final IBlockState JUNGLE_LOG = Blocks.log.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE);
    private static final IBlockState JUNGLE_LEAF = Blocks.leaves.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));
    private static final IBlockState JUNGLE_SHRUB = Blocks.leaves.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));

    /**
     * Construct a new world.
     *
     * @param world the world
     */
    public SpongeForgeWorld(World world) {
        super(world);
    }

    @Override
    protected BlockSnapshot createBlockSnapshot(Vector position, BaseBlock block) {
        this.blockBuilder.reset();

        Location<World> location = new Location<>(getWorld(), new Vector3i(position.getX(), position.getY(), position.getZ()));
        IBlockState baseState = Block.getBlockById(block.getId()).getStateFromMeta(block.getData());

        this.blockBuilder.blockState((BlockState) baseState);
        this.blockBuilder.worldId(getWorld().getUniqueId());
        this.blockBuilder.position(location.getBlockPosition());

        if (block.hasNbtData()) {
            NBTTagCompound tag = NBTConverter.toNative(block.getNbtData());
            tag.setString("id", block.getNbtId());

            this.blockBuilder.unsafeNbt(tag);
        }

        return this.blockBuilder.build();
    }

    @Override
    protected EntitySnapshot createEntitySnapshot(com.sk89q.worldedit.util.Location location, BaseEntity entity) {
        this.entityBuilder.reset();

        this.entityBuilder.worldId(getWorld().getUniqueId());
        this.entityBuilder.position(new Vector3d(location.getX(), location.getY(), location.getZ()));
        // TODO Rotation code
        // this.entityBuilder.rotation()
        this.entityBuilder.type(Sponge.getRegistry().getType(EntityType.class, entity.getTypeId()).get());
        if (entity.hasNbtData()) {
            NBTTagCompound tag = NBTConverter.toNative(entity.getNbtData());
            for (String name : Constants.NO_COPY_ENTITY_NBT_FIELDS) {
                tag.removeTag(name);
            }

            this.entityBuilder.unsafeCompound(tag);
        }
        return this.entityBuilder.build();
    }

    @Override
    public boolean clearContainerBlockContents(Vector position) {
        BlockPos pos = new BlockPos(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        TileEntity tile =((net.minecraft.world.World) getWorld()).getTileEntity(pos);
        if (tile instanceof IInventory) {
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
                IChunkProvider provider = ((net.minecraft.world.World) getWorld()).getChunkProvider();
                if (!(provider instanceof ChunkProviderServer)) {
                    return false;
                }
                ChunkProviderServer chunkServer = (ChunkProviderServer) provider;
                IChunkProvider chunkProvider = chunkServer.serverChunkGenerator;

                for (Vector2D coord : chunks) {
                    long pos = ChunkCoordIntPair.chunkXZ2Int(coord.getBlockX(), coord.getBlockZ());
                    Chunk mcChunk;
                    if (chunkServer.chunkExists(coord.getBlockX(), coord.getBlockZ())) {
                        mcChunk = chunkServer.loadChunk(coord.getBlockX(), coord.getBlockZ());
                        mcChunk.onChunkUnload();
                    }
                    chunkServer.droppedChunksSet.remove(pos);
                    chunkServer.id2ChunkMap.remove(pos);
                    mcChunk = chunkProvider.provideChunk(coord.getBlockX(), coord.getBlockZ());
                    chunkServer.id2ChunkMap.add(pos, mcChunk);
                    chunkServer.loadedChunks.add(mcChunk);
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
    private static WorldGenerator createWorldGenerator(TreeGenerator.TreeType type) {
        switch (type) {
            case TREE: return new WorldGenTrees(true);
            case BIG_TREE: return new WorldGenBigTree(true);
            case REDWOOD: return new WorldGenTaiga2(true);
            case TALL_REDWOOD: return new WorldGenTaiga1();
            case BIRCH: return new WorldGenForest(true, false);
            case JUNGLE: return new WorldGenMegaJungle(true, 10, 20, JUNGLE_LOG, JUNGLE_LEAF);
            case SMALL_JUNGLE: return new WorldGenTrees(true, 4 + random.nextInt(7), JUNGLE_LOG, JUNGLE_LEAF, false);
            case SHORT_JUNGLE: return new WorldGenTrees(true, 4 + random.nextInt(7), JUNGLE_LOG, JUNGLE_LEAF, true);
            case JUNGLE_BUSH: return new WorldGenShrub(JUNGLE_LOG, JUNGLE_SHRUB);
            case RED_MUSHROOM: return new WorldGenBigMushroom(Blocks.brown_mushroom_block);
            case BROWN_MUSHROOM: return new WorldGenBigMushroom(Blocks.red_mushroom_block);
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
    public boolean generateTree(TreeGenerator.TreeType type, EditSession editSession, Vector pos) throws MaxChangedBlocksException {
        WorldGenerator generator = createWorldGenerator(type);
        return generator != null && generator.generate((net.minecraft.world.World) getWorld(), random, new BlockPos(pos.getX(), pos.getY(), pos.getZ()));
    }

    @Override
    public BaseBlock getBlock(Vector position) {
        World world = getWorld();
        BlockPos pos = new BlockPos(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        IBlockState state = ((net.minecraft.world.World) world).getBlockState(pos);
        TileEntity tile = ((net.minecraft.world.World) world).getTileEntity(pos);

        if (tile != null) {
            return new TileEntityBaseBlock(Block.getIdFromBlock(state.getBlock()), state.getBlock().getMetaFromState(state), tile);
        } else {
            return new BaseBlock(Block.getIdFromBlock(state.getBlock()), state.getBlock().getMetaFromState(state));
        }
    }

    @Override
    public BaseBlock getLazyBlock(Vector position) {
        World world = getWorld();
        BlockPos pos = new BlockPos(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        IBlockState state = ((net.minecraft.world.World) world).getBlockState(pos);
        return new LazyBlock(Block.getIdFromBlock(state.getBlock()), state.getBlock().getMetaFromState(state), this, position);
    }
}
