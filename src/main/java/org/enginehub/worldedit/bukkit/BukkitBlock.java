package org.enginehub.worldedit.bukkit;

import org.bukkit.World;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.foundation.Block;

public class BukkitBlock extends BaseBlock {

    protected BukkitBlock(int type) {
        super(type);
    }

    protected BukkitBlock(int type, int data) {
        super(type, data);
    }

    public BukkitBlock(org.bukkit.block.Block block) {
        super(block.getTypeId(), block.getData());
        
        // @TOOD: Really need to load NBT data
    }

    public static boolean verify() {
        return false;
    }

    public static BukkitBlock get(World world, Vector vector, int type, int data) {
        return null;
    }

    public static boolean set(World world, Vector vector, Block block) {
        return false;
    }

    public static boolean setSafely(World world, Vector vector, Block block, boolean notify) {
        return false;
    }

    public static boolean hasTileEntity(int type) {
        return false;
    }

    public static boolean isValidBlockType(int type) {
        return false;
    }
}