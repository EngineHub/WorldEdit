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

package com.sk89q.worldedit.bukkit;
import com.sk89q.jnbt.*;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.TileEntityBlock;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.foundation.Block;
import net.minecraft.server.v1_7_R3.*;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;

/**
 * A blind handler of blocks with TileEntity data that directly access Minecraft's
 * classes through CraftBukkit.
 * </p>
 * Usage of this class may break terribly in the future, and therefore usage should
 * be trapped in a handler for {@link Throwable}.
 */
public class DefaultNmsBlock extends NmsBlock {

    private static final Logger logger = WorldEdit.logger;
    private static Field compoundMapField;
    private static final Field nmsBlock_isTileEntityField; // The field is deobfuscated but the method isn't. No idea why.
    private NBTTagCompound nbtData = null;

    static {
        Field field;
        try {
            field = net.minecraft.server.v1_7_R3.Block.class.getDeclaredField("isTileEntity");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            // logger.severe("Could not find NMS block tile entity field!");
            field = null;
        }
        nmsBlock_isTileEntityField = field;
    }

    public static boolean verify() {
        return nmsBlock_isTileEntityField != null;
    }

    /**
     * Create a new instance with a given type ID, data value, and previous
     * {@link TileEntityBlock}-implementing object.
     *
     * @param type block type ID
     * @param data data value
     * @param tileEntityBlock tile entity block
     */
    public DefaultNmsBlock(int type, int data, TileEntityBlock tileEntityBlock) {
        super(type, data);

        nbtData = (NBTTagCompound) fromNative(tileEntityBlock.getNbtData());
    }

    /**
     * Create a new instance with a given type ID, data value, and raw
     * {@link NBTTagCompound} copy.
     *
     * @param type block type ID
     * @param data data value
     * @param nbtData raw NBT data
     */
    public DefaultNmsBlock(int type, int data, NBTTagCompound nbtData) {
        super(type, data);

        this.nbtData = nbtData;
    }

    /**
     * Build a {@link NBTTagCompound} that has valid coordinates.
     *
     * @param pt coordinates to set
     * @return the tag compound
     */
    private NBTTagCompound getNmsData(Vector pt) {
        if (nbtData == null) {
            return null;
        }

        nbtData.set("x", new NBTTagInt(pt.getBlockX()));
        nbtData.set("y", new NBTTagInt(pt.getBlockY()));
        nbtData.set("z", new NBTTagInt(pt.getBlockZ()));

        return nbtData;
    }

    @Override
    public boolean hasNbtData() {
        return nbtData != null;
    }

    @Override
    public String getNbtId() {
        if (nbtData == null) {
            return "";
        }

        return nbtData.getString("id");
    }

    @Override
    public CompoundTag getNbtData() {
        if (nbtData == null) {
            return new CompoundTag(getNbtId(),
                    new HashMap<String, Tag>());
        }
        return (CompoundTag) toNative(nbtData);
    }

    @Override
    public void setNbtData(CompoundTag tag) throws DataException {
        if (tag == null) {
            this.nbtData = null;
        }
        this.nbtData = (NBTTagCompound) fromNative(tag);
    }

    /**
     * Build an instance from the given information.
     *
     * @param world world to get the block from
     * @param position position to get the block at
     * @param type type ID of block
     * @param data data value of block
     * @return the block, or null
     */
    public static DefaultNmsBlock get(World world, Vector position, int type, int data) {
        if (!hasTileEntity(type)) {
            return null;
        }

        TileEntity te = ((CraftWorld) world).getHandle().getTileEntity(
                position.getBlockX(), position.getBlockY(), position.getBlockZ());

        if (te != null) {
            NBTTagCompound tag = new NBTTagCompound();
            te.b(tag); // Load data
            return new DefaultNmsBlock(type, data, tag);
        }

        return null;
    }

    /**
     * Set an instance or a {@link TileEntityBlock} to the given position.
     *
     * @param world world to set the block in
     * @param position position to set the block at
     * @param block the block to set
     * @return true if tile entity data was copied to the world
     */
    public static boolean set(World world, Vector position, BaseBlock block) {
        NBTTagCompound data = null;
        if (!hasTileEntity(world.getBlockTypeIdAt(position.getBlockX(), position.getBlockY(), position.getBlockZ()))) {
            return false;
        }

        if (block instanceof DefaultNmsBlock) {
            DefaultNmsBlock nmsProxyBlock = (DefaultNmsBlock) block;
            data = nmsProxyBlock.getNmsData(position);
        } else if (block instanceof TileEntityBlock) {
            DefaultNmsBlock nmsProxyBlock = new DefaultNmsBlock(
                    block.getId(), block.getData(), block);
            data = nmsProxyBlock.getNmsData(position);
        }

        if (data != null) {
            TileEntity te = ((CraftWorld) world).getHandle().getTileEntity(
                    position.getBlockX(), position.getBlockY(), position.getBlockZ());
            if (te != null) {
                te.a(data); // Load data
                return true;
            }
        }

        return false;
    }

    /**
     * Tries to set a block 'safely', as in setting the block data to the location, and
     * then triggering physics only at the end.
     *
     * @param world world to set the block in
     * @param position position to set the block at
     * @param block the block to set
     * @param notifyAdjacent true to notify physics and what not
     * @return true if block id or data was changed
     */
    public static boolean setSafely(BukkitWorld world, Vector position,
            Block block, boolean notifyAdjacent) {

        int x = position.getBlockX();
        int y = position.getBlockY();
        int z = position.getBlockZ();

        CraftWorld craftWorld = ((CraftWorld) world.getWorld());
//        TileEntity te = craftWorld.getHandle().getTileEntity(x, y, z);
//        craftWorld.getHandle().tileEntityList.remove(te);

        boolean changed = craftWorld.getHandle().setTypeAndData(x, y, z, getNmsBlock(block.getId()), block.getData(), 0);

        if (block instanceof BaseBlock) {
            world.copyToWorld(position, (BaseBlock) block);
        }

        changed = craftWorld.getHandle().setData(x, y, z, block.getData(), 0) || changed;
        if (changed && notifyAdjacent) {
            craftWorld.getHandle().notify(x, y, z);
            craftWorld.getHandle().update(x, y, z, getNmsBlock(block.getId()));
        }

        return changed;
    }

    public static boolean hasTileEntity(int type) {
        net.minecraft.server.v1_7_R3.Block nmsBlock = getNmsBlock(type);
        if (nmsBlock == null) {
            return false;
        }

        try {
            return nmsBlock_isTileEntityField.getBoolean(nmsBlock); // Once we have the field stord, gets are fast
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    public static net.minecraft.server.v1_7_R3.Block getNmsBlock(int type) {
        return net.minecraft.server.v1_7_R3.Block.e(type);
    }

    /**
     * Converts from a non-native NMS NBT structure to a native WorldEdit NBT
     * structure.
     *
     * @param foreign non-native NMS NBT structure
     * @return native WorldEdit NBT structure
     */
    private static Tag toNative(NBTBase foreign) {
        // temporary fix since mojang removed names from tags
        // our nbt spec will need to be updated to theirs
        return toNative(getTagName(foreign.getTypeId()), foreign);
    }

    // seriously these two methods are hacky - our jnbt spec needs updating
    // copied from NMS 1.7.5- code, since it was removed in 1.7.8
    private static String getTagName(int i) {
        switch (i) {
        case 0:
            return "TAG_End";
        case 1:
            return "TAG_Byte";
        case 2:
            return "TAG_Short";
        case 3:
            return "TAG_Int";
        case 4:
            return "TAG_Long";
        case 5:
            return "TAG_Float";
        case 6:
            return "TAG_Double";
        case 7:
            return "TAG_Byte_Array";
        case 8:
            return "TAG_String";
        case 9:
            return "TAG_List";
        case 10:
            return "TAG_Compound";
        case 11:
            return "TAG_Int_Array";
        case 99:
            return "Any Numeric Tag";
        default:
            return "UNKNOWN";
        }
    }

    /**
     * Converts from a non-native NMS NBT structure to a native WorldEdit NBT
     * structure.
     *
     * @param foreign non-native NMS NBT structure
     * @param name name for the tag, if it has one
     * @return native WorldEdit NBT structure
     */
    @SuppressWarnings("unchecked")
    private static Tag toNative(String name, NBTBase foreign) {
        if (foreign == null) {
            return null;
        }
        if (foreign instanceof NBTTagCompound) {
            Map<String, Tag> values = new HashMap<String, Tag>();
            Collection<Object> foreignKeys = null;

            if (compoundMapField == null) {
                try {
                    // Method name may change!
                    foreignKeys = ((NBTTagCompound) foreign).c();
                } catch (Throwable t) {
                    try {
                        logger.warning("WorldEdit: Couldn't get NBTTagCompound.c(), " +
                                "so we're going to try to get at the 'map' field directly from now on");

                        if (compoundMapField == null) {
                            compoundMapField = NBTTagCompound.class.getDeclaredField("map");
                            compoundMapField.setAccessible(true);
                        }
                    } catch (Throwable e) {
                        // Can't do much beyond this
                        throw new RuntimeException(e);
                    }
                }
            }

            if (compoundMapField != null) {
                try {
                    foreignKeys = ((HashMap<Object, Object>) compoundMapField.get(foreign)).keySet();
                } catch (Throwable e) {
                    // Can't do much beyond this
                    throw new RuntimeException(e);
                }
            }

            for (Object obj : foreignKeys) {
                String key = (String) obj;
                NBTBase base = (NBTBase) ((NBTTagCompound) foreign).get(key);
                values.put(key, toNative(key, base));
            }
            return new CompoundTag(name, values);
        } else if (foreign instanceof NBTTagByte) {
            return new ByteTag(name, ((NBTTagByte) foreign).f()); // getByte
        } else if (foreign instanceof NBTTagByteArray) {
            return new ByteArrayTag(name,
                    ((NBTTagByteArray) foreign).c()); // data
        } else if (foreign instanceof NBTTagDouble) {
            return new DoubleTag(name,
                    ((NBTTagDouble) foreign).g()); // getDouble
        } else if (foreign instanceof NBTTagFloat) {
            return new FloatTag(name, ((NBTTagFloat) foreign).h()); // getFloat
        } else if (foreign instanceof NBTTagInt) {
            return new IntTag(name, ((NBTTagInt) foreign).d()); // getInt
        } else if (foreign instanceof NBTTagIntArray) {
            return new IntArrayTag(name,
                    ((NBTTagIntArray) foreign).c()); // data
        } else if (foreign instanceof NBTTagList) {
            try {
                return transmorgifyList(name, (NBTTagList) foreign);
            } catch (NoSuchFieldException e) {
            } catch (SecurityException e) {
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {}
            return new ListTag(name, ByteTag.class, new ArrayList<ByteTag>());
        } else if (foreign instanceof NBTTagLong) {
            return new LongTag(name, ((NBTTagLong) foreign).c()); // getLong
        } else if (foreign instanceof NBTTagShort) {
            return new ShortTag(name, ((NBTTagShort) foreign).e()); // getShort
        } else if (foreign instanceof NBTTagString) {
            return new StringTag(name,
                    ((NBTTagString) foreign).a_()); // data
        } else if (foreign instanceof NBTTagEnd) {
            return new EndTag();
        } else {
            throw new IllegalArgumentException("Don't know how to make native "
                    + foreign.getClass().getCanonicalName());
        }
    }

    private static ListTag transmorgifyList(String name, NBTTagList foreign)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        List<Tag> values = new ArrayList<Tag>();
        int type = foreign.d();
        Field listField = NBTTagList.class.getDeclaredField("list");
        listField.setAccessible(true);
        List foreignList;
        foreignList = (List) listField.get(foreign);
        for (int i = 0; i < foreign.size(); i++) {
            NBTBase element = (NBTBase) foreignList.get(i);
            values.add(toNative(null, element)); // list elements shouldn't have names
        }
        Class<? extends Tag> cls = NBTConstants.getClassFromType(type);
        return new ListTag(name, cls, values);
    }

    /**
     * Converts a WorldEdit-native NBT structure to a NMS structure.
     *
     * @param foreign structure to convert
     * @return non-native structure
     */
    private static NBTBase fromNative(Tag foreign) {
        if (foreign == null) {
            return null;
        }
        if (foreign instanceof CompoundTag) {
            NBTTagCompound tag = new NBTTagCompound();
            for (Map.Entry<String, Tag> entry : ((CompoundTag) foreign)
                    .getValue().entrySet()) {
                tag.set(entry.getKey(), fromNative(entry.getValue()));
            }
            return tag;
        } else if (foreign instanceof ByteTag) {
            return new NBTTagByte(((ByteTag) foreign).getValue());
        } else if (foreign instanceof ByteArrayTag) {
            return new NBTTagByteArray(((ByteArrayTag) foreign).getValue());
        } else if (foreign instanceof DoubleTag) {
            return new NBTTagDouble(((DoubleTag) foreign).getValue());
        } else if (foreign instanceof FloatTag) {
            return new NBTTagFloat(((FloatTag) foreign).getValue());
        } else if (foreign instanceof IntTag) {
            return new NBTTagInt(((IntTag) foreign).getValue());
        } else if (foreign instanceof IntArrayTag) {
            return new NBTTagIntArray(((IntArrayTag) foreign).getValue());
        } else if (foreign instanceof ListTag) {
            NBTTagList tag = new NBTTagList();
            ListTag foreignList = (ListTag) foreign;
            for (Tag t : foreignList.getValue()) {
                tag.add(fromNative(t));
            }
            return tag;
        } else if (foreign instanceof LongTag) {
            return new NBTTagLong(((LongTag) foreign).getValue());
        } else if (foreign instanceof ShortTag) {
            return new NBTTagShort(((ShortTag) foreign).getValue());
        } else if (foreign instanceof StringTag) {
            return new NBTTagString(((StringTag) foreign).getValue());
        } else if (foreign instanceof EndTag) {
            try {
                Method tagMaker = NBTBase.class.getDeclaredMethod("createTag", byte.class);
                tagMaker.setAccessible(true);
                return (NBTBase) tagMaker.invoke(null, (byte) 0);
            } catch (Exception e) {
                return null;
            }
        } else {
            throw new IllegalArgumentException("Don't know how to make NMS "
                    + foreign.getClass().getCanonicalName());
        }
    }

    public static boolean isValidBlockType(int type) throws NoClassDefFoundError {
        return type == 0 || (type >= 1 && net.minecraft.server.v1_7_R3.Block.e(type) != null);
    }

}
