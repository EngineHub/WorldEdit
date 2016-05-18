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

package com.sk89q.worldedit.sponge.nms;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.LazyBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.sponge.SpongeWorld;
import com.sk89q.worldedit.util.TreeGenerator;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.gen.feature.*;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

@Deprecated
public class SpongeNMSWorld extends SpongeWorld {

    private static final IBlockState JUNGLE_LOG = Blocks.log.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE);
    private static final IBlockState JUNGLE_LEAF = Blocks.leaves.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));
    private static final IBlockState JUNGLE_SHRUB = Blocks.leaves.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));

    /**
     * Construct a new world.
     *
     * @param world the world
     */
    public SpongeNMSWorld(World world) {
        super(world);
    }

    @Override
    protected BlockState getBlockState(BaseBlock block) {
        return (BlockState) Block.getBlockById(block.getId()).getStateFromMeta(block.getData());
    }

    private NBTTagCompound updateForSet(NBTTagCompound tag, Vector position) {
        checkNotNull(tag);
        checkNotNull(position);

        tag.setTag("x", new NBTTagInt(position.getBlockX()));
        tag.setTag("y", new NBTTagInt(position.getBlockY()));
        tag.setTag("z", new NBTTagInt(position.getBlockZ()));

        return tag;
    }

    @Override
    protected void applyTileEntityData(org.spongepowered.api.block.tileentity.TileEntity entity, BaseBlock block) {
        NBTTagCompound tag = NBTConverter.toNative(block.getNbtData());

        Location<World> loc = entity.getLocation();

        updateForSet(tag, new Vector(loc.getX(), loc.getY(), loc.getZ()));
        ((TileEntity) entity).readFromNBT(tag);
    }

    @Override
    protected void applyEntityData(Entity entity, BaseEntity data) {
        NBTTagCompound tag = NBTConverter.toNative(data.getNbtData());
        for (String name : Constants.NO_COPY_ENTITY_NBT_FIELDS) {
            tag.removeTag(name);
        }
        ((net.minecraft.entity.Entity) entity).readFromNBT(tag);
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
