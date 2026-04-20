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

package com.sk89q.worldedit.coremc;

import com.mojang.serialization.Codec;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.coremc.internal.CoreMcBlockCommandSender;
import com.sk89q.worldedit.coremc.internal.CoreMcCommandSender;
import com.sk89q.worldedit.coremc.internal.CoreMcPlatform;
import com.sk89q.worldedit.coremc.internal.CoreMcPlayer;
import com.sk89q.worldedit.coremc.internal.CoreMcWorld;
import com.sk89q.worldedit.coremc.internal.NBTConverter;
import com.sk89q.worldedit.coremc.mixin.AccessorCommandSourceStack;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.enginehub.linbus.tree.LinCompoundTag;

import java.util.Objects;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Adapter for platforms sharing native Minecraft code.
 *
 * @implNote This is not intended to be implemented by non-platforms. Only extend this in platform-specific code.
 */
public abstract class CoreMcAdapter {

    protected CoreMcAdapter() {
    }

    /**
     * {@return the platform that owns this adapter}
     */
    protected abstract CoreMcPlatform getPlatform();

    public World fromNativeWorld(Level level) {
        return new CoreMcWorld(getPlatform(), (ServerLevel) level);
    }

    public ServerLevel toNativeWorld(World world) {
        checkNotNull(world);
        if (world instanceof CoreMcWorld coreMcWorld) {
            return coreMcWorld.getWorld();
        }
        // TODO introduce a better cross-platform world API to match more easily
        throw new UnsupportedOperationException("Cannot adapt from a " + world.getClass());
    }

    // Available as API, but not used by WorldEdit itself.
    @SuppressWarnings("unused")
    public Biome toNativeBiome(BiomeType biomeType) {
        return getPlatform().serverRegistryAccess().lookupOrThrow(Registries.BIOME)
            .getOptional(Identifier.parse(biomeType.id()))
            .orElseThrow(() -> new IllegalStateException("No biome for " + biomeType.id()));
    }

    public BiomeType fromNativeBiome(Biome biome) {
        Identifier id = getPlatform().serverRegistryAccess().lookupOrThrow(Registries.BIOME).getKey(biome);
        Objects.requireNonNull(id, "biome is not registered");
        return BiomeTypes.get(id.toString());
    }

    public Vector3 adapt(Vec3 vector) {
        return Vector3.at(vector.x, vector.y, vector.z);
    }

    public BlockVector3 adapt(BlockPos pos) {
        return BlockVector3.at(pos.getX(), pos.getY(), pos.getZ());
    }

    public Vec3 toVec3(BlockVector3 vector) {
        return new Vec3(vector.x(), vector.y(), vector.z());
    }

    public net.minecraft.core.Direction adapt(Direction face) {
        return switch (face) {
            case NORTH -> net.minecraft.core.Direction.NORTH;
            case SOUTH -> net.minecraft.core.Direction.SOUTH;
            case WEST -> net.minecraft.core.Direction.WEST;
            case EAST -> net.minecraft.core.Direction.EAST;
            case DOWN -> net.minecraft.core.Direction.DOWN;
            default -> net.minecraft.core.Direction.UP;
        };
    }

    public Direction adaptEnumFacing(@Nullable net.minecraft.core.Direction face) {
        if (face == null) {
            return null;
        }
        return switch (face) {
            case NORTH -> Direction.NORTH;
            case SOUTH -> Direction.SOUTH;
            case WEST -> Direction.WEST;
            case EAST -> Direction.EAST;
            case DOWN -> Direction.DOWN;
            default -> Direction.UP;
        };
    }

    public BlockPos toBlockPos(BlockVector3 vector) {
        return new BlockPos(vector.x(), vector.y(), vector.z());
    }

    public net.minecraft.world.level.block.state.BlockState toNativeBlockState(BlockState blockState) {
        int blockStateId = BlockStateIdAccess.getBlockStateId(blockState);
        if (!BlockStateIdAccess.isValidInternalId(blockStateId)) {
            return getPlatform().getTransmogrifier().transmogToMinecraft(blockState);
        }
        return Block.stateById(blockStateId);
    }

    public BlockState fromNativeBlockState(net.minecraft.world.level.block.state.BlockState blockState) {
        int blockStateId = Block.getId(blockState);
        BlockState worldEdit = BlockStateIdAccess.getBlockStateById(blockStateId);
        if (worldEdit == null) {
            return getPlatform().getTransmogrifier().transmogToWorldEdit(blockState);
        }
        return worldEdit;
    }

    public Block toNativeBlock(BlockType blockType) {
        return getPlatform().serverRegistryAccess().lookupOrThrow(Registries.BLOCK)
            .getValue(Identifier.parse(blockType.id()));
    }

    public BlockType fromNativeBlock(Block block) {
        return BlockTypes.get(
            getPlatform().serverRegistryAccess().lookupOrThrow(Registries.BLOCK).getKey(block).toString()
        );
    }

    public Item toNativeItem(ItemType itemType) {
        return getPlatform().serverRegistryAccess().lookupOrThrow(Registries.ITEM)
            .getValue(Identifier.parse(itemType.id()));
    }

    public ItemType fromNativeItem(Item item) {
        return ItemTypes.get(
            getPlatform().serverRegistryAccess().lookupOrThrow(Registries.ITEM).getKey(item).toString()
        );
    }

    /**
     * For serializing and deserializing components.
     */
    private static final Codec<DataComponentPatch> COMPONENTS_CODEC = DataComponentPatch.CODEC.optionalFieldOf(
        "components", DataComponentPatch.EMPTY
    ).codec();

    public ItemStack toNativeItemStack(BaseItemStack baseItemStack) {
        final ItemStack itemStack = new ItemStack(
            toNativeItem(baseItemStack.getType()),
            baseItemStack.getAmount()
        );
        LinCompoundTag nbt = baseItemStack.getNbt();
        if (nbt != null) {
            DataComponentPatch componentPatch = COMPONENTS_CODEC.parse(
                getPlatform().serverRegistryAccess().createSerializationContext(NbtOps.INSTANCE),
                NBTConverter.toNative(nbt)
            ).getOrThrow();
            itemStack.applyComponents(componentPatch);
        }
        return itemStack;
    }

    public BaseItemStack fromNativeItemStack(ItemStack itemStack) {
        CompoundTag tag = (CompoundTag) COMPONENTS_CODEC.encodeStart(
            getPlatform().serverRegistryAccess().createSerializationContext(NbtOps.INSTANCE),
            itemStack.getComponentsPatch()
        ).getOrThrow();
        return new BaseItemStack(
            fromNativeItem(itemStack.getItem()),
            LazyReference.from(() -> NBTConverter.fromNative(tag)),
            itemStack.getCount()
        );
    }

    /**
     * Get the WorldEdit proxy for the given player.
     *
     * @param player the player
     * @return the WorldEdit player
     */
    public CoreMcPlayer fromNativePlayer(ServerPlayer player) {
        checkNotNull(player);
        return new CoreMcPlayer(getPlatform(), player);
    }

    /**
     * Get the WorldEdit proxy for the given command source.
     *
     * @param commandSourceStack the command source
     * @return the WorldEdit actor
     */
    public Actor adaptCommandSource(CommandSourceStack commandSourceStack) {
        checkNotNull(commandSourceStack);
        if (commandSourceStack.isPlayer()) {
            return fromNativePlayer(commandSourceStack.getPlayer());
        }
        if (getPlatform().getConfiguration().commandBlockSupport
            && ((AccessorCommandSourceStack) commandSourceStack).getSource() instanceof BaseCommandBlock commandBlock) {
            return new CoreMcBlockCommandSender(
                getPlatform(), commandBlock, commandSourceStack.getLevel(), commandSourceStack.getPosition()
            );
        }
        return new CoreMcCommandSender(commandSourceStack);
    }
}
