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

package com.sk89q.worldedit.bukkit.adapter.impl.v1_18_R1;

import ca.spottedleaf.dataconverter.minecraft.MCDataConverter;
import ca.spottedleaf.dataconverter.minecraft.datatypes.MCDataType;
import ca.spottedleaf.dataconverter.minecraft.datatypes.MCTypeRegistry;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.world.DataFixer;

import java.util.stream.Collectors;

public class PaperDataConverter implements DataFixer {
    private static int DATA_VERSION;
    private final PaperweightAdapter adapter;

    PaperDataConverter(int dataVersion, PaperweightAdapter adapter) {
        this.adapter = adapter;
        DATA_VERSION = dataVersion;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T fixUp(DataFixer.FixType<T> type, T original, int srcVer) {
        if (type == DataFixer.FixTypes.CHUNK) {
            return (T) fixChunk((CompoundTag) original, srcVer);
        } else if (type == DataFixer.FixTypes.BLOCK_ENTITY) {
            return (T) fixBlockEntity((CompoundTag) original, srcVer);
        } else if (type == DataFixer.FixTypes.ENTITY) {
            return (T) fixEntity((CompoundTag) original, srcVer);
        } else if (type == DataFixer.FixTypes.BLOCK_STATE) {
            return (T) fixBlockState((String) original, srcVer);
        } else if (type == DataFixer.FixTypes.ITEM_TYPE) {
            return (T) fixItemType((String) original, srcVer);
        } else if (type == DataFixer.FixTypes.BIOME) {
            return (T) fixBiome((String) original, srcVer);
        }
        return original;
    }

    private CompoundTag fixChunk(CompoundTag originalChunk, int srcVer) {
        net.minecraft.nbt.CompoundTag tag = (net.minecraft.nbt.CompoundTag) adapter.fromNative(originalChunk);
        net.minecraft.nbt.CompoundTag fixed = convert(MCTypeRegistry.CHUNK, tag, srcVer);
        return (CompoundTag) adapter.toNative(fixed);
    }

    private CompoundTag fixBlockEntity(CompoundTag origTileEnt, int srcVer) {
        net.minecraft.nbt.CompoundTag tag = (net.minecraft.nbt.CompoundTag) adapter.fromNative(origTileEnt);
        net.minecraft.nbt.CompoundTag fixed = convert(MCTypeRegistry.TILE_ENTITY, tag, srcVer);
        return (CompoundTag) adapter.toNative(fixed);
    }

    private CompoundTag fixEntity(CompoundTag origEnt, int srcVer) {
        net.minecraft.nbt.CompoundTag tag = (net.minecraft.nbt.CompoundTag) adapter.fromNative(origEnt);
        net.minecraft.nbt.CompoundTag fixed = convert(MCTypeRegistry.ENTITY, tag, srcVer);
        return (CompoundTag) adapter.toNative(fixed);
    }

    private String fixBlockState(String blockState, int srcVer) {
        net.minecraft.nbt.CompoundTag stateNBT = stateToNBT(blockState);
        net.minecraft.nbt.CompoundTag fixed = MCDataConverter.convertTag(MCTypeRegistry.BLOCK_STATE, stateNBT, srcVer, DATA_VERSION);
        return nbtToState(fixed);
    }

    private String nbtToState(net.minecraft.nbt.CompoundTag tagCompound) {
        StringBuilder sb = new StringBuilder();
        sb.append(tagCompound.getString("Name"));
        if (tagCompound.contains("Properties", 10)) {
            sb.append('[');
            net.minecraft.nbt.CompoundTag props = tagCompound.getCompound("Properties");
            sb.append(props.getAllKeys().stream().map(k -> k + "=" + props.getString(k).replace("\"", "")).collect(Collectors.joining(",")));
            sb.append(']');
        }
        return sb.toString();
    }

    private static net.minecraft.nbt.CompoundTag stateToNBT(String blockState) {
        int propIdx = blockState.indexOf('[');
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        if (propIdx < 0) {
            tag.putString("Name", blockState);
        } else {
            tag.putString("Name", blockState.substring(0, propIdx));
            net.minecraft.nbt.CompoundTag propTag = new net.minecraft.nbt.CompoundTag();
            String props = blockState.substring(propIdx + 1, blockState.length() - 1);
            String[] propArr = props.split(",");
            for (String pair : propArr) {
                final String[] split = pair.split("=");
                propTag.putString(split[0], split[1]);
            }
            tag.put("Properties", propTag);
        }
        return tag;
    }

    private String fixBiome(String key, int srcVer) {
        return (String) MCDataConverter.convert(MCTypeRegistry.BIOME, key, srcVer, DATA_VERSION);
    }

    private String fixItemType(String key, int srcVer) {
        return (String) MCDataConverter.convert(MCTypeRegistry.ITEM_NAME, key, srcVer, DATA_VERSION);
    }

    private net.minecraft.nbt.CompoundTag convert(MCDataType type, net.minecraft.nbt.CompoundTag tag, int srcVer) {
        return MCDataConverter.convertTag(type, tag, srcVer, DATA_VERSION);
    }
}
