package com.sk89q.worldedit.bukkit;

import java.util.ArrayList;
import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * @author toi Thanks to Raphfrk for optimization of this class.
 */
public class TargetBlock {

    private Location loc;
    private double viewHeight;
    private int maxDistance;
    private int[] blockToIgnore;
    private double checkDistance, curDistance;
    private double xRotation, yRotation;
    private Vector targetPos = new Vector();
    private Vector targetPosDouble = new Vector();
    private Vector prevPos = new Vector();
    private Vector offset = new Vector();

    /**
     * Constructor requiring a player, uses default values
     * 
     * @param player
     *            Player to work with
     */
    public TargetBlock(Player player) {
        this.setValues(player.getLocation(), 300, 1.65, 0.2, null);
    }

    /**
     * Constructor requiring a location, uses default values
     * 
     * @param loc
     *            Location to work with
     */
    public TargetBlock(Location loc) {
        this.setValues(loc, 300, 0, 0.2, null);
    }

    /**
     * Constructor requiring a player, max distance and a checking distance
     * 
     * @param player
     *            Player to work with
     * @param maxDistance
     *            How far it checks for blocks
     * @param checkDistance
     *            How often to check for blocks, the smaller the more precise
     */
    public TargetBlock(Player player, int maxDistance, double checkDistance) {
        this.setValues(player.getLocation(), maxDistance, 1.65, checkDistance,
                null);
    }

    /**
     * Constructor requiring a location, max distance and a checking distance
     * 
     * @param loc
     *            What location to work with
     * @param maxDistance
     *            How far it checks for blocks
     * @param checkDistance
     *            How often to check for blocks, the smaller the more precise
     */
    public TargetBlock(Location loc, int maxDistance, double checkDistance) {
        this.setValues(loc, maxDistance, 0, checkDistance, null);
    }

    /**
     * Constructor requiring a player, max distance, checking distance and an
     * array of blocks to ignore
     * 
     * @param player
     *            What player to work with
     * @param maxDistance
     *            How far it checks for blocks
     * @param checkDistance
     *            How often to check for blocks, the smaller the more precise
     * @param blocksToIgnore
     *            Integer array of what block ids to ignore while checking for
     *            viable targets
     */
    public TargetBlock(Player player, int maxDistance, double checkDistance,
            int[] blocksToIgnore) {
        this.setValues(player.getLocation(), maxDistance, 1.65, checkDistance,
                blocksToIgnore);
    }

    /**
     * Constructor requiring a location, max distance, checking distance and an
     * array of blocks to ignore
     * 
     * @param loc
     *            What location to work with
     * @param maxDistance
     *            How far it checks for blocks
     * @param checkDistance
     *            How often to check for blocks, the smaller the more precise
     * @param blocksToIgnore
     *            Array of what block ids to ignore while checking for viable
     *            targets
     */
    public TargetBlock(Location loc, int maxDistance, double checkDistance,
            int[] blocksToIgnore) {
        this.setValues(loc, maxDistance, 0, checkDistance, blocksToIgnore);
    }

    /**
     * Constructor requiring a player, max distance, checking distance and an
     * array of blocks to ignore
     * 
     * @param player
     *            What player to work with
     * @param maxDistance
     *            How far it checks for blocks
     * @param checkDistance
     *            How often to check for blocks, the smaller the more precise
     * @param blocksToIgnore
     *            String ArrayList of what block ids to ignore while checking
     *            for viable targets
     */
    public TargetBlock(Player player, int maxDistance, double checkDistance,
            ArrayList<String> blocksToIgnore) {
        int[] bti = this.convertStringArraytoIntArray(blocksToIgnore);
        this.setValues(player.getLocation(), maxDistance, 1.65, checkDistance,
                bti);
    }

    /**
     * Constructor requiring a location, max distance, checking distance and an
     * array of blocks to ignore
     * 
     * @param loc
     *            What location to work with
     * @param maxDistance
     *            How far it checks for blocks
     * @param checkDistance
     *            How often to check for blocks, the smaller the more precise
     * @param blocksToIgnore
     *            String ArrayList of what block ids to ignore while checking
     *            for viable targets
     */
    public TargetBlock(Location loc, int maxDistance, double checkDistance,
            ArrayList<String> blocksToIgnore) {
        int[] bti = this.convertStringArraytoIntArray(blocksToIgnore);
        this.setValues(loc, maxDistance, 0, checkDistance, bti);
    }

    /**
     * Set the values, all constructors uses this function
     * 
     * @param loc
     *            Location of the view
     * @param maxDistance
     *            How far it checks for blocks
     * @param viewPos
     *            Where the view is positioned in y-axis
     * @param checkDistance
     *            How often to check for blocks, the smaller the more precise
     * @param blocksToIgnore
     *            Ids of blocks to ignore while checking for viable targets
     */
    private void setValues(Location loc, int maxDistance, double viewHeight,
            double checkDistance, int[] blocksToIgnore) {
        this.loc = loc;
        this.maxDistance = maxDistance;
        this.viewHeight = viewHeight;
        this.checkDistance = checkDistance;
        this.blockToIgnore = blocksToIgnore;
        this.curDistance = 0;
        xRotation = (loc.getYaw() + 90) % 360;
        yRotation = loc.getPitch() * -1;

        double h = (checkDistance * Math.cos(Math.toRadians(yRotation)));
        offset.setY((checkDistance * Math.sin(Math.toRadians(yRotation))));
        offset.setX((h * Math.cos(Math.toRadians(xRotation))));
        offset.setZ((h * Math.sin(Math.toRadians(xRotation))));

        targetPosDouble = new Vector(loc.getX(), loc.getY() + viewHeight,
                loc.getZ());
        targetPos = new Vector(targetPosDouble.getBlockX(),
                targetPosDouble.getBlockY(), targetPosDouble.getBlockZ());
        prevPos = targetPos.clone();
    }

    /**
     * Call this to reset checking position to allow you to check for a new
     * target with the same TargetBlock instance.
     */
    public void reset() {
        targetPosDouble = new Vector(loc.getX(), loc.getY() + viewHeight,
                loc.getZ());
        targetPos = new Vector(targetPosDouble.getBlockX(),
                targetPosDouble.getBlockY(), targetPosDouble.getBlockZ());
        prevPos = targetPos.clone();
        this.curDistance = 0;
    }

    /**
     * Gets the distance to a block. Measures from the block underneath the
     * player to the targetblock Should only be used when passing player as an
     * constructor parameter
     * 
     * @return double
     */
    public double getDistanceToBlock() {
        Vector blockUnderPlayer = new Vector(
                (int) Math.floor(loc.getX() + 0.5),
                (int) Math.floor(loc.getY() - 0.5),
                (int) Math.floor(loc.getZ() + 0.5));

        Block blk = getTargetBlock();
        double x = blk.getX() - blockUnderPlayer.getBlockX();
        double y = blk.getY() - blockUnderPlayer.getBlockY();
        double z = blk.getZ() - blockUnderPlayer.getBlockZ();

        return Math.sqrt((Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)));
    }

    /**
     * Gets the rounded distance to a block. Measures from the block underneath
     * the player to the targetblock Should only be used when passing player as
     * an constructor parameter
     * 
     * @return int
     */
    public int getDistanceToBlockRounded() {
        Vector blockUnderPlayer = new Vector(
                (int) Math.floor(loc.getX() + 0.5),
                (int) Math.floor(loc.getY() - 0.5),
                (int) Math.floor(loc.getZ() + 0.5));

        Block blk = getTargetBlock();
        double x = blk.getX() - blockUnderPlayer.getBlockX();
        double y = blk.getY() - blockUnderPlayer.getBlockY();
        double z = blk.getZ() - blockUnderPlayer.getBlockZ();

        return (int) Math
                .round((Math.sqrt((Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(
                        z, 2)))));
    }

    /**
     * Gets the floored x distance to a block.
     * 
     * @return int
     */
    public int getXDistanceToBlock() {
        this.reset();
        return (int) Math
                .floor(getTargetBlock().getX() - loc.getBlockX() + 0.5);
    }

    /**
     * Gets the floored y distance to a block
     * 
     * @return int
     */
    public int getYDistanceToBlock() {
        this.reset();
        return (int) Math.floor(getTargetBlock().getY() - loc.getBlockY()
                + viewHeight);
    }

    /**
     * Gets the floored z distance to a block
     * 
     * @return int
     */
    public int getZDistanceToBlock() {
        this.reset();
        return (int) Math
                .floor(getTargetBlock().getZ() - loc.getBlockZ() + 0.5);
    }

    /**
     * Returns the block at the sight. Returns null if out of range or if no
     * viable target was found
     * 
     * @return Block
     */
    public Block getTargetBlock() {
        this.reset();
        while ((getNextBlock() != null)
                && ((getCurrentBlock().getTypeId() == 0) || this
                        .blockToIgnoreHasValue(getCurrentBlock().getTypeId())))
            ;
        return getCurrentBlock();
    }

    /**
     * Sets the type of the block at the sight. Returns false if the block
     * wasn't set.
     * 
     * @param typeID
     *            ID of type to set the block to
     * @return boolean
     */
    public boolean setTargetBlock(int typeID) {
        if (Material.getMaterial(typeID) != null) {
            this.reset();
            while (getNextBlock() != null && getCurrentBlock().getTypeId() == 0)
                ;
            if (getCurrentBlock() != null) {
                Block blk = loc.getWorld().getBlockAt(targetPos.getBlockX(),
                        targetPos.getBlockY(), targetPos.getBlockZ());
                blk.setTypeId(typeID);
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the type of the block at the sight. Returns false if the block
     * wasn't set.
     * 
     * @param type
     *            Material to set the block to
     * @return boolean
     */
    public boolean setTargetBlock(Material type) {
        this.reset();
        while ((getNextBlock() != null)
                && ((getCurrentBlock().getTypeId() == 0) || this
                        .blockToIgnoreHasValue(getCurrentBlock().getTypeId())))
            ;
        if (getCurrentBlock() != null) {
            Block blk = loc.getWorld().getBlockAt(targetPos.getBlockX(),
                    targetPos.getBlockY(), targetPos.getBlockZ());
            blk.setType(type);
            return true;
        }
        return false;
    }

    /**
     * Sets the type of the block at the sight. Returns false if the block
     * wasn't set. Observe! At the moment this function is using the built-in
     * enumerator function .valueOf(String) but would preferably be changed to
     * smarter function, when implemented
     * 
     * @param type
     *            Name of type to set the block to
     * @return boolean
     */
    public boolean setTargetBlock(String type) {
        Material mat = Material.valueOf(type);
        if (mat != null) {
            this.reset();
            while ((getNextBlock() != null)
                    && ((getCurrentBlock().getTypeId() == 0) || this
                            .blockToIgnoreHasValue(getCurrentBlock()
                                    .getTypeId())))
                ;
            if (getCurrentBlock() != null) {
                Block blk = loc.getWorld().getBlockAt(targetPos.getBlockX(),
                        targetPos.getBlockY(), targetPos.getBlockZ());
                blk.setType(mat);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the block attached to the face at the sight. Returns null if out
     * of range or if no viable target was found
     * 
     * @return Block
     */
    public Block getFaceBlock() {
        while ((getNextBlock() != null)
                && ((getCurrentBlock().getTypeId() == 0) || this
                        .blockToIgnoreHasValue(getCurrentBlock().getTypeId())))
            ;
        if (getCurrentBlock() != null) {
            return getPreviousBlock();
        } else {
            return null;
        }
    }

    /**
     * Sets the type of the block attached to the face at the sight. Returns
     * false if the block wasn't set.
     * 
     * @param typeID
     * @return boolean
     */
    public boolean setFaceBlock(int typeID) {
        if (Material.getMaterial(typeID) != null) {
            if (getCurrentBlock() != null) {
                Block blk = loc.getWorld().getBlockAt(prevPos.getBlockX(),
                        prevPos.getBlockY(), prevPos.getBlockZ());
                blk.setTypeId(typeID);
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the type of the block attached to the face at the sight. Returns
     * false if the block wasn't set.
     * 
     * @param type
     * @return boolean
     */
    public boolean setFaceBlock(Material type) {
        if (getCurrentBlock() != null) {
            Block blk = loc.getWorld().getBlockAt(prevPos.getBlockX(),
                    prevPos.getBlockY(), prevPos.getBlockZ());
            blk.setType(type);
            return true;
        }
        return false;
    }

    /**
     * Sets the type of the block attached to the face at the sight. Returns
     * false if the block wasn't set. Observe! At the moment this function is
     * using the built-in enumerator function .valueOf(String) but would
     * preferably be changed to smarter function, when implemented
     * 
     * @param type
     * @return boolean
     */
    public boolean setFaceBlock(String type) {
        Material mat = Material.valueOf(type);
        if (mat != null) {
            if (getCurrentBlock() != null) {
                Block blk = loc.getWorld().getBlockAt(prevPos.getBlockX(),
                        prevPos.getBlockY(), prevPos.getBlockZ());
                blk.setType(mat);
                return true;
            }
        }
        return false;
    }

    /**
     * Get next block
     * 
     * @return Block
     */
    public Block getNextBlock() {
        prevPos = targetPos.clone();
        do {
            curDistance += checkDistance;

            targetPosDouble.setX(offset.getX() + targetPosDouble.getX());
            targetPosDouble.setY(offset.getY() + targetPosDouble.getY());
            targetPosDouble.setZ(offset.getZ() + targetPosDouble.getZ());
            targetPos = new Vector(targetPosDouble.getBlockX(),
                    targetPosDouble.getBlockY(), targetPosDouble.getBlockZ());
        } while (curDistance <= maxDistance
                && targetPos.getBlockX() == prevPos.getBlockX()
                && targetPos.getBlockY() == prevPos.getBlockY()
                && targetPos.getBlockZ() == prevPos.getBlockZ());
        if (curDistance > maxDistance) {
            return null;
        }

        return this.loc.getWorld().getBlockAt(this.targetPos.getBlockX(),
                this.targetPos.getBlockY(), this.targetPos.getBlockZ());
    }

    /**
     * Returns the current block along the line of vision
     * 
     * @return Block
     */
    public Block getCurrentBlock() {
        if (curDistance > maxDistance) {
            return null;
        } else {
            return this.loc.getWorld().getBlockAt(this.targetPos.getBlockX(),
                    this.targetPos.getBlockY(), this.targetPos.getBlockZ());
        }
    }

    /**
     * Sets current block type. Returns false if the block wasn't set.
     * 
     * @param typeID
     */
    public boolean setCurrentBlock(int typeID) {
        if (Material.getMaterial(typeID) != null) {
            Block blk = getCurrentBlock();
            if (blk != null) {
                blk.setTypeId(typeID);
                return true;
            }
        }
        return false;
    }

    /**
     * Sets current block type. Returns false if the block wasn't set.
     * 
     * @param type
     */
    public boolean setCurrentBlock(Material type) {
        Block blk = getCurrentBlock();
        if (blk != null) {
            blk.setType(type);
            return true;
        }
        return false;
    }

    /**
     * Sets current block type. Returns false if the block wasn't set. Observe!
     * At the moment this function is using the built-in enumerator function
     * .valueOf(String) but would preferably be changed to smarter function,
     * when implemented
     * 
     * @param type
     */
    public boolean setCurrentBlock(String type) {
        Material mat = Material.valueOf(type);
        if (mat != null) {
            Block blk = getCurrentBlock();
            if (blk != null) {
                blk.setType(mat);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the previous block in the aimed path
     * 
     * @return Block
     */
    public Block getPreviousBlock() {
        return this.loc.getWorld().getBlockAt(prevPos.getBlockX(),
                prevPos.getBlockY(), prevPos.getBlockZ());
    }

    /**
     * Sets previous block type id. Returns false if the block wasn't set.
     * 
     * @param typeID
     */
    public boolean setPreviousBlock(int typeID) {
        if (Material.getMaterial(typeID) != null) {
            Block blk = getPreviousBlock();
            if (blk != null) {
                blk.setTypeId(typeID);
                return true;
            }
        }
        return false;
    }

    /**
     * Sets previous block type id. Returns false if the block wasn't set.
     * 
     * @param type
     */
    public boolean setPreviousBlock(Material type) {
        Block blk = getPreviousBlock();
        if (blk != null) {
            blk.setType(type);
            return true;
        }
        return false;
    }

    /**
     * Sets previous block type id. Returns false if the block wasn't set.
     * Observe! At the moment this function is using the built-in enumerator
     * function .valueOf(String) but would preferably be changed to smarter
     * function, when implemented
     * 
     * @param type
     */
    public boolean setPreviousBlock(String type) {
        Material mat = Material.valueOf(type);
        if (mat != null) {
            Block blk = getPreviousBlock();
            if (blk != null) {
                blk.setType(mat);
                return true;
            }
        }
        return false;
    }

    private int[] convertStringArraytoIntArray(ArrayList<String> array) {
        if (array != null) {
            int intarray[] = new int[array.size()];
            for (int i = 0; i < array.size(); i++) {
                try {
                    intarray[i] = Integer.parseInt(array.get(i));
                } catch (NumberFormatException nfe) {
                    intarray[i] = 0;
                }
            }
            return intarray;
        }
        return null;
    }

    private boolean blockToIgnoreHasValue(int value) {
        if (this.blockToIgnore != null) {
            if (this.blockToIgnore.length > 0) {
                for (int i : this.blockToIgnore) {
                    if (i == value)
                        return true;
                }
            }
        }
        return false;
    }
}