import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagEnd;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_5_R2.CraftWorld;

import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.DoubleTag;
import com.sk89q.jnbt.EndTag;
import com.sk89q.jnbt.FloatTag;
import com.sk89q.jnbt.IntArrayTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.LongTag;
import com.sk89q.jnbt.NBTConstants;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.TileEntityBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.NmsBlock;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.foundation.Block;

/**
 * Remapping tools are broken atm.
 */
public class MCPCPlusXNmsBlock_151dv extends NmsBlock {

    private static final Logger logger = WorldEdit.logger;
    private static Field compoundMapField;
    private static final Field nmsBlock_isTileEntityField; // The field is deobfuscated but the method isn't. No idea why.
    private NBTTagCompound nbtData = null;

    static {
        Field field;
        try {
            field = net.minecraft.block.Block.class.getDeclaredField("field_72025_cg");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            // logger.severe("Could not find NMS block tile entity field!");
            field = null;
        }
        nmsBlock_isTileEntityField = field;
    }

    public static boolean verify() {
        try {
            Class.forName("org.bukkit.craftbukkit.v1_5_R2.CraftWorld");
        } catch (Throwable e) {
            return false;
        } 
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
    public MCPCPlusXNmsBlock_151dv(int type, int data, TileEntityBlock tileEntityBlock) {
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
    public MCPCPlusXNmsBlock_151dv(int type, int data, NBTTagCompound nbtData) {
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

        nbtData.func_74782_a("x", new NBTTagInt("x", pt.getBlockX()));
        nbtData.func_74782_a("y", new NBTTagInt("y", pt.getBlockY()));
        nbtData.func_74782_a("z", new NBTTagInt("z", pt.getBlockZ()));

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

        return nbtData.func_74779_i("id");
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
    public static MCPCPlusXNmsBlock_151dv get(World world, Vector position, int type, int data) {
        if (!hasTileEntity(type)) {
            return null;
        }

        TileEntity te = ((CraftWorld) world).getHandle().func_72796_p(
                position.getBlockX(), position.getBlockY(), position.getBlockZ());

        if (te != null) {
            NBTTagCompound tag = new NBTTagCompound();
            te.func_70310_b(tag); // Load data
            return new MCPCPlusXNmsBlock_151dv(type, data, tag);
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

        if (block instanceof MCPCPlusXNmsBlock_151dv) {
            MCPCPlusXNmsBlock_151dv nmsProxyBlock = (MCPCPlusXNmsBlock_151dv) block;
            data = nmsProxyBlock.getNmsData(position);
        } else if (block instanceof TileEntityBlock) {
            MCPCPlusXNmsBlock_151dv nmsProxyBlock = new MCPCPlusXNmsBlock_151dv(
                    block.getId(), block.getData(), block);
            data = nmsProxyBlock.getNmsData(position);
        }

        if (data != null) {
            TileEntity te = ((CraftWorld) world).getHandle().func_72796_p(
                    position.getBlockX(), position.getBlockY(), position.getBlockZ());
            if (te != null) {
                te.func_70307_a(data); // Load data
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

        boolean changed = craftWorld.getHandle().func_72832_d(x, y, z, block.getId(), block.getData(), 0);

        if (block instanceof BaseBlock) {
            world.copyToWorld(position, (BaseBlock) block);
        }

        changed = craftWorld.getHandle().func_72921_c(x, y, z, block.getData(), 0) || changed;
        if (changed && notifyAdjacent) {
            craftWorld.getHandle().func_72845_h(x, y, z);
            craftWorld.getHandle().func_72851_f(x, y, z, block.getId());
        }

        return changed;
    }

    public static boolean hasTileEntity(int type) {
        net.minecraft.block.Block nmsBlock = getNmsBlock(type);
        if (nmsBlock == null) {
            return false;
        }

        try {
            return nmsBlock_isTileEntityField.getBoolean(nmsBlock); // Once we have the field stord, gets are fast
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    public static net.minecraft.block.Block getNmsBlock(int type) {
        if (type < 0 || type >= net.minecraft.block.Block.field_71973_m.length) {
            return null;
        }
        return net.minecraft.block.Block.field_71973_m[type];
    }

    /**
     * Converts from a non-native NMS NBT structure to a native WorldEdit NBT
     * structure.
     *
     * @param foreign non-native NMS NBT structure
     * @return native WorldEdit NBT structure
     */
    @SuppressWarnings("unchecked")
    private static Tag toNative(NBTBase foreign) {
        if (foreign == null) {
            return null;
        }
        if (foreign instanceof NBTTagCompound) {
            Map<String, Tag> values = new HashMap<String, Tag>();
            Collection<Object> foreignValues = null;

            if (compoundMapField == null) {
                try {
                    // Method name may change!
                    foreignValues = ((NBTTagCompound) foreign).func_74758_c();
                } catch (Throwable t) {
                    try {
                        logger.warning("WorldEdit: Couldn't get NBTTagCompound.func_74758_c(), " +
                                "so we're going to try to get at the 'map' field directly from now on");

                        if (compoundMapField == null) {
                            compoundMapField = NBTTagCompound.class.getDeclaredField("field_74784_a");
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
                    foreignValues = ((HashMap<Object, Object>) compoundMapField.get(foreign)).values();
                } catch (Throwable e) {
                    // Can't do much beyond this
                    throw new RuntimeException(e);
                }
            }

            for (Object obj : foreignValues) {
                NBTBase base = (NBTBase) obj;
                values.put(base.func_74740_e(), toNative(base));
            }
            return new CompoundTag(foreign.func_74740_e(), values);
        } else if (foreign instanceof NBTTagByte) {
            return new ByteTag(foreign.func_74740_e(), ((NBTTagByte) foreign).field_74756_a);
        } else if (foreign instanceof NBTTagByteArray) {
            return new ByteArrayTag(foreign.func_74740_e(),
                    ((NBTTagByteArray) foreign).field_74754_a);
        } else if (foreign instanceof NBTTagDouble) {
            return new DoubleTag(foreign.func_74740_e(),
                    ((NBTTagDouble) foreign).field_74755_a);
        } else if (foreign instanceof NBTTagFloat) {
            return new FloatTag(foreign.func_74740_e(), ((NBTTagFloat) foreign).field_74750_a);
        } else if (foreign instanceof NBTTagInt) {
            return new IntTag(foreign.func_74740_e(), ((NBTTagInt) foreign).field_74748_a);
        } else if (foreign instanceof NBTTagIntArray) {
            return new IntArrayTag(foreign.func_74740_e(),
                    ((NBTTagIntArray) foreign).field_74749_a);
        } else if (foreign instanceof NBTTagList) {
            List<Tag> values = new ArrayList<Tag>();
            NBTTagList foreignList = (NBTTagList) foreign;
            int type = NBTConstants.TYPE_BYTE;
            for (int i = 0; i < foreignList.func_74745_c(); i++) {
                NBTBase foreignTag = foreignList.func_74743_b(i);
                values.add(toNative(foreignTag));
                type = foreignTag.func_74732_a();
            }
            Class<? extends Tag> cls = NBTConstants.getClassFromType(type);
            return new ListTag(foreign.func_74740_e(), cls, values);
        } else if (foreign instanceof NBTTagLong) {
            return new LongTag(foreign.func_74740_e(), ((NBTTagLong) foreign).field_74753_a);
        } else if (foreign instanceof NBTTagShort) {
            return new ShortTag(foreign.func_74740_e(), ((NBTTagShort) foreign).field_74752_a);
        } else if (foreign instanceof NBTTagString) {
            return new StringTag(foreign.func_74740_e(),
                    ((NBTTagString) foreign).field_74751_a);
        } else if (foreign instanceof NBTTagEnd) {
            return new EndTag();
        } else {
            throw new IllegalArgumentException("Don't know how to make native "
                    + foreign.getClass().getCanonicalName());
        }
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
            NBTTagCompound tag = new NBTTagCompound(foreign.getName());
            for (Map.Entry<String, Tag> entry : ((CompoundTag) foreign)
                    .getValue().entrySet()) {
                tag.func_74782_a(entry.getKey(), fromNative(entry.getValue()));
            }
            return tag;
        } else if (foreign instanceof ByteTag) {
            return new NBTTagByte(foreign.getName(),
                    ((ByteTag) foreign).getValue());
        } else if (foreign instanceof ByteArrayTag) {
            return new NBTTagByteArray(foreign.getName(),
                    ((ByteArrayTag) foreign).getValue());
        } else if (foreign instanceof DoubleTag) {
            return new NBTTagDouble(foreign.getName(),
                    ((DoubleTag) foreign).getValue());
        } else if (foreign instanceof FloatTag) {
            return new NBTTagFloat(foreign.getName(),
                    ((FloatTag) foreign).getValue());
        } else if (foreign instanceof IntTag) {
            return new NBTTagInt(foreign.getName(),
                    ((IntTag) foreign).getValue());
        } else if (foreign instanceof IntArrayTag) {
            return new NBTTagIntArray(foreign.getName(),
                    ((IntArrayTag) foreign).getValue());
        } else if (foreign instanceof ListTag) {
            NBTTagList tag = new NBTTagList(foreign.getName());
            ListTag foreignList = (ListTag) foreign;
            for (Tag t : foreignList.getValue()) {
                tag.func_74742_a(fromNative(t));
            }
            return tag;
        } else if (foreign instanceof LongTag) {
            return new NBTTagLong(foreign.getName(),
                    ((LongTag) foreign).getValue());
        } else if (foreign instanceof ShortTag) {
            return new NBTTagShort(foreign.getName(),
                    ((ShortTag) foreign).getValue());
        } else if (foreign instanceof StringTag) {
            return new NBTTagString(foreign.getName(),
                    ((StringTag) foreign).getValue());
        } else if (foreign instanceof EndTag) {
            return new NBTTagEnd();
        } else {
            throw new IllegalArgumentException("Don't know how to make NMS "
                    + foreign.getClass().getCanonicalName());
        }
    }

    public static boolean isValidBlockType(int type) throws NoClassDefFoundError {
        return type == 0 || (type >= 1 && type < net.minecraft.block.Block.field_71973_m.length
                && net.minecraft.block.Block.field_71973_m[type] != null);
    }
}
