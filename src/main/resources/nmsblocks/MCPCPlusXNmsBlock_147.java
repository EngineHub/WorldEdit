import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_4_R1.CraftWorld;

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
public class MCPCPlusXNmsBlock_147 extends NmsBlock {

    private static final Logger logger = WorldEdit.logger;
    private static Field compoundMapField;
    private static final Field nmsBlock_isTileEntityField; // The field is deobfuscated but the method isn't. No idea why.
    private bq nbtData = null;

    static {
        Field field;
        try {
            field = amq.class.getDeclaredField("cs");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            // logger.severe("Could not find NMS block tile entity field!");
            field = null;
        }
        nmsBlock_isTileEntityField = field;
    }

    public static boolean verify() {
        try {
            Class.forName("org.bukkit.craftbukkit.v1_4_R1.CraftWorld");
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
    public MCPCPlusXNmsBlock_147(int type, int data, TileEntityBlock tileEntityBlock) {
        super(type, data);

        nbtData = (bq) fromNative(tileEntityBlock.getNbtData());
    }

    /**
     * Create a new instance with a given type ID, data value, and raw
     * {@link bq} copy.
     *
     * @param type block type ID
     * @param data data value
     * @param nbtData raw NBT data
     */
    public MCPCPlusXNmsBlock_147(int type, int data, bq nbtData) {
        super(type, data);

        this.nbtData = nbtData;
    }

    /**
     * Build a {@link bq} that has valid coordinates.
     *
     * @param pt coordinates to set
     * @return the tag compound
     */
    private bq getNmsData(Vector pt) {
        if (nbtData == null) {
            return null;
        }

        nbtData.a("x", new bx("x", pt.getBlockX()));
        nbtData.a("y", new bx("y", pt.getBlockY()));
        nbtData.a("z", new bx("z", pt.getBlockZ()));

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

        return nbtData.i("id");
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
        this.nbtData = (bq) fromNative(tag);
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
    public static MCPCPlusXNmsBlock_147 get(World world, Vector position, int type, int data) {
        if (!hasTileEntity(type)) {
            return null;
        }

        any te = ((CraftWorld) world).getHandle().q(position.getBlockX(), position.getBlockY(), position.getBlockZ());

        if (te != null) {
            bq tag = new bq();
            te.b(tag); // Load data
            return new MCPCPlusXNmsBlock_147(type, data, tag);
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
        bq data = null;
        if (!hasTileEntity(world.getBlockTypeIdAt(position.getBlockX(), position.getBlockY(), position.getBlockZ()))) {
            return false;
        }

        if (block instanceof MCPCPlusXNmsBlock_147) {
            MCPCPlusXNmsBlock_147 nmsProxyBlock = (MCPCPlusXNmsBlock_147) block;
            data = nmsProxyBlock.getNmsData(position);
        } else if (block instanceof TileEntityBlock) {
            MCPCPlusXNmsBlock_147 nmsProxyBlock = new MCPCPlusXNmsBlock_147(
                    block.getId(), block.getData(), block);
            data = nmsProxyBlock.getNmsData(position);
        }

        if (data != null) {
            any te = ((CraftWorld) world).getHandle().q(
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

        boolean changed = craftWorld.getHandle().c(x, y, z, block.getId(), block.getData());

        if (block instanceof BaseBlock) {
            world.copyToWorld(position, (BaseBlock) block);
        }

        if (changed) {
            if (notifyAdjacent) {
                craftWorld.getHandle().f(x, y, z, block.getId());
            } else {
                craftWorld.getHandle().i(x, y, z);
            }
        }

        return changed;
    }

    public static boolean hasTileEntity(int type) {
        amq nmsBlock = getNmsBlock(type);
        if (nmsBlock == null) {
            return false;
        }

        try {
            return nmsBlock_isTileEntityField.getBoolean(nmsBlock); // Once we have the field stord, gets are fast
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    public static amq getNmsBlock(int type) {
        if (type < 0 || type >= amq.p.length) {
            return null;
        }
        return amq.p[type];
    }

    /**
     * Converts from a non-native NMS NBT structure to a native WorldEdit NBT
     * structure.
     *
     * @param foreign non-native NMS NBT structure
     * @return native WorldEdit NBT structure
     */
    @SuppressWarnings("unchecked")
    private static Tag toNative(cd foreign) {
        if (foreign == null) {
            return null;
        }
        if (foreign instanceof bq) {
            Map<String, Tag> values = new HashMap<String, Tag>();
            Collection<Object> foreignValues = null;

            if (compoundMapField == null) {
                try {
                    // Method name may change!
                    foreignValues = ((bq) foreign).c();
                } catch (Throwable t) {
                    try {
                        logger.warning("WorldEdit: Couldn't get bq.c(), " +
                                "so we're going to try to get at the 'map' field directly from now on");

                        if (compoundMapField == null) {
                            compoundMapField = bq.class.getDeclaredField("map");
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
                cd base = (cd) obj;
                values.put(base.e(), toNative(base));
            }
            return new CompoundTag(foreign.e(), values);
        } else if (foreign instanceof bp) {
            return new ByteTag(foreign.e(), ((bp) foreign).a);
        } else if (foreign instanceof bo) {
            return new ByteArrayTag(foreign.e(),
                    ((bo) foreign).a);
        } else if (foreign instanceof bt) {
            return new DoubleTag(foreign.e(),
                    ((bt) foreign).a);
        } else if (foreign instanceof bv) {
            return new FloatTag(foreign.e(), ((bv) foreign).a);
        } else if (foreign instanceof bx) {
            return new IntTag(foreign.e(), ((bx) foreign).a);
        } else if (foreign instanceof bw) {
            return new IntArrayTag(foreign.e(),
                    ((bw) foreign).a);
        } else if (foreign instanceof by) {
            List<Tag> values = new ArrayList<Tag>();
            by foreignList = (by) foreign;
            int type = NBTConstants.TYPE_BYTE;
            for (int i = 0; i < foreignList.c(); i++) {
                cd foreignTag = foreignList.b(i);
                values.add(toNative(foreignTag));
                type = foreignTag.a();
            }
            Class<? extends Tag> cls = NBTConstants.getClassFromType(type);
            return new ListTag(foreign.e(), cls, values);
        } else if (foreign instanceof bz) {
            return new LongTag(foreign.e(), ((bz) foreign).a);
        } else if (foreign instanceof cb) {
            return new ShortTag(foreign.e(), ((cb) foreign).a);
        } else if (foreign instanceof cc) {
            return new StringTag(foreign.e(),
                    ((cc) foreign).a);
        } else if (foreign instanceof bu) {
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
    private static cd fromNative(Tag foreign) {
        if (foreign == null) {
            return null;
        }
        if (foreign instanceof CompoundTag) {
            bq tag = new bq(foreign.getName());
            for (Map.Entry<String, Tag> entry : ((CompoundTag) foreign)
                    .getValue().entrySet()) {
                tag.a(entry.getKey(), fromNative(entry.getValue()));
            }
            return tag;
        } else if (foreign instanceof ByteTag) {
            return new bp(foreign.getName(),
                    ((ByteTag) foreign).getValue());
        } else if (foreign instanceof ByteArrayTag) {
            return new bo(foreign.getName(),
                    ((ByteArrayTag) foreign).getValue());
        } else if (foreign instanceof DoubleTag) {
            return new bt(foreign.getName(),
                    ((DoubleTag) foreign).getValue());
        } else if (foreign instanceof FloatTag) {
            return new bv(foreign.getName(),
                    ((FloatTag) foreign).getValue());
        } else if (foreign instanceof IntTag) {
            return new bx(foreign.getName(),
                    ((IntTag) foreign).getValue());
        } else if (foreign instanceof IntArrayTag) {
            return new bw(foreign.getName(),
                    ((IntArrayTag) foreign).getValue());
        } else if (foreign instanceof ListTag) {
            by tag = new by(foreign.getName());
            ListTag foreignList = (ListTag) foreign;
            for (Tag t : foreignList.getValue()) {
                tag.a(fromNative(t));
            }
            return tag;
        } else if (foreign instanceof LongTag) {
            return new bz(foreign.getName(),
                    ((LongTag) foreign).getValue());
        } else if (foreign instanceof ShortTag) {
            return new cb(foreign.getName(),
                    ((ShortTag) foreign).getValue());
        } else if (foreign instanceof StringTag) {
            return new cc(foreign.getName(),
                    ((StringTag) foreign).getValue());
        } else if (foreign instanceof EndTag) {
            return new bu();
        } else {
            throw new IllegalArgumentException("Don't know how to make NMS "
                    + foreign.getClass().getCanonicalName());
        }
    }

    public static boolean isValidBlockType(int type) throws NoClassDefFoundError {
        return type == 0 || (type >= 1 && type < amq.p.length
                && amq.p[type] != null);
    }
}
