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

package com.sk89q.worldedit.spout;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.sk89q.worldedit.BiomeType;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EntityType;
import com.sk89q.worldedit.LocalEntity;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.regions.Region;

import com.sk89q.worldedit.util.TreeGenerator;
import org.spout.api.component.Component;
import org.spout.api.entity.Entity;
import org.spout.api.generator.biome.BiomeGenerator;
import org.spout.api.geo.LoadOption;
import org.spout.api.geo.World;
import org.spout.api.geo.cuboid.Chunk;
import org.spout.api.inventory.ItemStack;
import org.spout.api.material.BlockMaterial;
import org.spout.api.material.Material;
import org.spout.vanilla.plugin.component.substance.Painting;
import org.spout.vanilla.plugin.component.substance.XPOrb;
import org.spout.vanilla.plugin.component.substance.object.Item;
import org.spout.vanilla.plugin.component.substance.object.Tnt;
import org.spout.vanilla.plugin.component.substance.object.projectile.Arrow;
import org.spout.vanilla.plugin.component.substance.object.vehicle.Boat;
import org.spout.vanilla.api.material.VanillaMaterial;
import org.spout.vanilla.plugin.component.substance.object.vehicle.minecart.Minecart;
import org.spout.vanilla.plugin.material.VanillaMaterials;
import org.spout.vanilla.plugin.world.generator.normal.object.tree.TreeObject;
import org.spout.vanilla.plugin.world.generator.normal.object.tree.SmallTreeObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SpoutWorld extends LocalWorld {
    private World world;

    /**
     * Construct the object.
     * @param world
     */
    public SpoutWorld(World world) {
        this.world = world;
    }

    /**
     * Get the world handle.
     *
     * @return
     */
    public World getWorld() {
        return world;
    }

    /**
     * Get the name of the world
     *
     * @return
     */
    @Override
    public String getName() {
        return world.getName();
    }

    public Material getSpoutMaterial(int id) {
        switch (id) {
            case 0:
                return BlockMaterial.AIR;
            default:
                return VanillaMaterials.getMaterial((short) id);
        }
    }

    public Material getSpoutMaterial(int id, int data) {
        switch (id) {
            case 0:
                return BlockMaterial.AIR;
            default:
                return VanillaMaterials.getMaterial((short) id, (short) data);
        }
    }

    /**
     * Set block type.
     *
     * @param pt
     * @param type
     * @return
     */
    @Override
    public boolean setBlockType(Vector pt, int type) {
        Material mat = getSpoutMaterial(type);
        if (mat != null && mat instanceof BlockMaterial) {
            final int x = pt.getBlockX();
            final int y = pt.getBlockY();
            final int z = pt.getBlockZ();
            return world.getChunkFromBlock(x, y, z, LoadOption.LOAD_GEN).setBlockMaterial(x, y, z, (BlockMaterial) mat, (short) 0, WorldEditPlugin.asCause());
        }
        return false;
    }

    /**
     * Set block type.
     *
     * @param pt
     * @param type
     * @return
     */
    @Override
    public boolean setBlockTypeFast(Vector pt, int type) {
        return setBlockType(pt, type);
    }

    /**
     * set block type & data
     * @param pt
     * @param type
     * @param data
     * @return
     */
    @Override
    public boolean setTypeIdAndData(Vector pt, int type, int data) {
        Material mat = getSpoutMaterial(type, data);
        if (mat != null && mat instanceof BlockMaterial) {
            final int x = pt.getBlockX();
            final int y = pt.getBlockY();
            final int z = pt.getBlockZ();
            return world.getChunkFromBlock(x, y, z, LoadOption.LOAD_GEN).setBlockMaterial(x, y, z, (BlockMaterial) mat, (short) data, WorldEditPlugin.asCause());
        }
        return false;
    }

    /**
     * set block type & data
     * Everything is threaded, so no need for fastmode here.
     * @param pt
     * @param type
     * @param data
     * @return
     */
    @Override
    public boolean setTypeIdAndDataFast(Vector pt, int type, int data) {
        return setTypeIdAndData(pt, type, data);
    }

    /**
     * Get block type.
     *
     * @param pt
     * @return
     */
    @Override
    public int getBlockType(Vector pt) {
        final int x = pt.getBlockX();
        final int y = pt.getBlockY();
        final int z = pt.getBlockZ();
        Material mat =  world.getChunkFromBlock(x, y, z, LoadOption.LOAD_GEN).getBlockMaterial(x, y, z);
        return mat instanceof VanillaMaterial ? ((VanillaMaterial) mat).getMinecraftId() : 0;
    }

    /**
     * Set block data.
     *
     * @param pt
     * @param data
     */
    @Override
    public void setBlockData(Vector pt, int data) {
        final int x = pt.getBlockX();
        final int y = pt.getBlockY();
        final int z = pt.getBlockZ();
        world.getChunkFromBlock(x, y, z, LoadOption.LOAD_GEN).setBlockData(x, y, z, (short) data, WorldEditPlugin.asCause());
    }

    /**
     * Set block data.
     *
     * @param pt
     * @param data
     */
    @Override
    public void setBlockDataFast(Vector pt, int data) {
        setBlockData(pt, data);
    }

    /**
     * Get block data.
     *
     * @param pt
     * @return
     */
    @Override
    public int getBlockData(Vector pt) {
        final int x = pt.getBlockX();
        final int y = pt.getBlockY();
        final int z = pt.getBlockZ();
        return world.getChunkFromBlock(x, y, z, LoadOption.LOAD_GEN).getBlockData(x, y, z);
    }

    /**
     * Get block light level.
     *
     * @param pt
     * @return
     */
    @Override
    public int getBlockLightLevel(Vector pt) {
        final int x = pt.getBlockX();
        final int y = pt.getBlockY();
        final int z = pt.getBlockZ();
        return world.getChunkFromBlock(x, y, z, LoadOption.LOAD_GEN).getBlockLight(x, y, z);
    }

    /**
     * Get biome type
     *
     * @param pt
     * @return
     */
    public BiomeType getBiome(Vector2D pt) {
        if (world.getGenerator() instanceof BiomeGenerator) {
            BiomeGenerator gen = (BiomeGenerator) world.getGenerator();
            return new SpoutBiomeType(gen.getBiome(pt.getBlockX(), pt.getBlockZ(), world.getSeed()));
        }
        return BiomeType.UNKNOWN;
    }

    public void setBiome(Vector2D pt, BiomeType biome) {
        if (biome instanceof SpoutBiomeType &&
                world.getGenerator() instanceof BiomeGenerator) {
            throw new UnsupportedOperationException("Biome changing is not yet supported in Spout");
            //BiomeGenerator gen = (BiomeGenerator) world.getGenerator();
            //gen.setBiome(new Vector3(pt.getBlockX(), 0, pt.getBlockZ()), ((SpoutBiomeType) biome).getSpoutBiome());
        }
    }

    /**
     * Regenerate an area.
     *
     * @param region
     * @param editSession
     * @return
     */
    @Override
    public boolean regenerate(Region region, EditSession editSession) {
        /*BaseBlock[] history = new BaseBlock[16 * 16 * (getMaxY() + 1)];

        for (Vector2D chunk : region.getChunks()) {
            Vector min = new Vector(chunk.getBlockX() * 16, 0, chunk.getBlockZ() * 16);

            // First save all the blocks inside
            for (int x = 0; x < 16; ++x) {
                for (int y = 0; y < (getMaxY() + 1); ++y) {
                    for (int z = 0; z < 16; ++z) {
                        Vector pt = min.add(x, y, z);
                        int index = y * 16 * 16 + z * 16 + x;
                        history[index] = editSession.getBlock(pt);
                    }
                }
            }

            try {
                world.regenerateChunk(chunk.getBlockX(), chunk.getBlockZ());
            } catch (Throwable t) {
                t.printStackTrace();
            }

            // Then restore
            for (int x = 0; x < 16; ++x) {
                for (int y = 0; y < (getMaxY() + 1); ++y) {
                    for (int z = 0; z < 16; ++z) {
                        Vector pt = min.add(x, y, z);
                        int index = y * 16 * 16 + z * 16 + x;

                        // We have to restore the block if it was outside
                        if (!region.contains(pt)) {
                            editSession.smartSetBlock(pt, history[index]);
                        } else { // Otherwise fool with history
                            editSession.rememberChange(pt, history[index],
                                    editSession.rawGetBlock(pt));
                        }
                    }
                }
            }
        }

        return true;*/
        return false;
    }

    /**
     * Attempts to accurately copy a BaseBlock's extra data to the world.
     *
     * @param pt
     * @param block
     * @return
     */
    @Override
    public boolean copyToWorld(Vector pt, BaseBlock block) {
        /*if (block instanceof SignBlock) {
            // Signs
            setSignText(pt, ((SignBlock) block).getText());
            return true;
        }

        if (block instanceof FurnaceBlock) {
            // Furnaces
            Block bukkitBlock = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (bukkitBlock == null) return false;
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof Furnace)) return false;
            Furnace bukkit = (Furnace) state;
            FurnaceBlock we = (FurnaceBlock) block;
            bukkit.setBurnTime(we.getBurnTime());
            bukkit.setCookTime(we.getCookTime());
            return setContainerBlockContents(pt, ((ContainerBlock) block).getItems());
        }

        if (block instanceof ContainerBlock) {
            // Chests/dispenser
            return setContainerBlockContents(pt, ((ContainerBlock) block).getItems());
        }

        if (block instanceof MobSpawnerBlock) {
            // Mob spawners
            Block bukkitBlock = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (bukkitBlock == null) return false;
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof CreatureSpawner)) return false;
            CreatureSpawner bukkit = (CreatureSpawner) state;
            MobSpawnerBlock we = (MobSpawnerBlock) block;
            bukkit.setCreatureTypeId(we.getMobType());
            bukkit.setDelay(we.getDelay());
            return true;
        }

        if (block instanceof NoteBlock) {
            // Note block
            Block bukkitBlock = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (bukkitBlock == null) return false;
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof org.bukkit.block.NoteBlock)) return false;
            org.bukkit.block.NoteBlock bukkit = (org.bukkit.block.NoteBlock) state;
            NoteBlock we = (NoteBlock) block;
            bukkit.setRawNote(we.getNote());
            return true;
        }*/

        return false;
    }

    /**
     * Attempts to read a BaseBlock's extra data from the world.
     *
     * @param pt
     * @param block
     * @return
     */
    @Override
    public boolean copyFromWorld(Vector pt, BaseBlock block) {
        /*if (block instanceof SignBlock) {
            // Signs
            ((SignBlock) block).setText(getSignText(pt));
            return true;
        }

        if (block instanceof FurnaceBlock) {
            // Furnaces
            Block bukkitBlock = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (bukkitBlock == null) return false;
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof Furnace)) return false;
            Furnace bukkit = (Furnace) state;
            FurnaceBlock we = (FurnaceBlock) block;
            we.setBurnTime(bukkit.getBurnTime());
            we.setCookTime(bukkit.getCookTime());
            ((ContainerBlock) block).setItems(getContainerBlockContents(pt));
            return true;
        }

        if (block instanceof ContainerBlock) {
            // Chests/dispenser
            ((ContainerBlock) block).setItems(getContainerBlockContents(pt));
            return true;
        }

        if (block instanceof MobSpawnerBlock) {
            // Mob spawners
            Block bukkitBlock = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (bukkitBlock == null) return false;
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof CreatureSpawner)) return false;
            CreatureSpawner bukkit = (CreatureSpawner) state;
            MobSpawnerBlock we = (MobSpawnerBlock) block;
            we.setMobType(bukkit.getCreatureTypeId());
            we.setDelay((short) bukkit.getDelay());
            return true;
        }

        if (block instanceof NoteBlock) {
            // Note block
            Block bukkitBlock = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (bukkitBlock == null) return false;
            BlockState state = bukkitBlock.getState();
            if (!(state instanceof org.bukkit.block.NoteBlock)) return false;
            org.bukkit.block.NoteBlock bukkit = (org.bukkit.block.NoteBlock) state;
            NoteBlock we = (NoteBlock) block;
            we.setNote(bukkit.getRawNote());
        }*/

        return false;
    }

    /**
     * Clear a chest's contents.
     *
     * @param pt
     */
    @Override
    public boolean clearContainerBlockContents(Vector pt) {
       /* Block block = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (block == null) {
            return false;
        }
        BlockState state = block.getState();
        if (!(state instanceof org.bukkit.block.ContainerBlock)) {
            return false;
        }

        org.bukkit.block.ContainerBlock chest = (org.bukkit.block.ContainerBlock) state;
        Inventory inven = chest.getInventory();
        inven.clear();
        return true;*/
        return false;
    }

    @Override
    public boolean generateTree(TreeGenerator.TreeType type, EditSession editSession, Vector pt)
            throws MaxChangedBlocksException {
        //VanillaObjects.byName()
        TreeObject tree = new SmallTreeObject(); //TODO: properly check for tree type
        if (!tree.canPlaceObject(world, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ())) {
            return false;
        }
        tree.placeObject(world, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        return true;
    }

    /**
     * Drop an item.
     *
     * @param pt
     * @param item
     */
    @Override
    public void dropItem(Vector pt, BaseItemStack item) {
        Material mat = VanillaMaterials.getMaterial((short) item.getType());
        if (mat.hasSubMaterials()) {
            mat = mat.getSubMaterial(item.getData());
        }
        ItemStack spoutItem = new ItemStack(mat, item.getData(), item.getAmount());
        world.createEntity(SpoutUtil.toPoint(world, pt), Item.class).get(Item.class).setItemStack(spoutItem);
    }

    /**
     * Kill mobs in an area.
     *
     * @param origin The center of the area to kill mobs in.
     * @param radius Maximum distance to kill mobs at; radius < 0 means kill all mobs
     * @param flags various flags that determine what to kill
     * @return
     */
    @Override
    public int killMobs(Vector origin, double radius, int flags) {
        /*boolean killPets = (flags & KillFlags.PETS) != 0;
        boolean killNPCs = (flags & KillFlags.NPCS) != 0;
        boolean killAnimals = (flags & KillFlags.ANIMALS) != 0;*/

        int num = 0;
        /*double radiusSq = radius * radius;

        Point bukkitOrigin = SpoutUtil.toPoint(world, origin);


        for (LivingEntity ent : world.getLivingEntities()) {
            if (ent instanceof HumanEntity) {
                continue;
            }

            if (!killAnimals && ent instanceof Animals) {
                continue;
            }

            if (!killPets && ent instanceof Tameable && ((Tameable) ent).isTamed()) {
                continue; // tamed wolf
            }

            try {
                // Temporary solution until org.bukkit.entity.NPC is widely deployed.
                if (!killNPCs && Class.forName("org.bukkit.entity.NPC").isAssignableFrom(ent.getClass())) {
                    continue;
                }
            } catch (ClassNotFoundException e) {}

            if (radius < 0 || bukkitOrigin.distanceSquared(ent.getLocation()) <= radiusSq) {
                ent.remove();
                ++num;
            }
        }*/

        return num;
    }

    /**
     * Remove entities in an area.
     *
     * @param origin
     * @param radius
     * @return
     */
    @Override
    public int removeEntities(EntityType type, Vector origin, int radius) {
        int num = 0;
        double radiusSq = radius * radius;

        for (Entity ent : world.getAll()) {
            if (radius != -1
                    && origin.distanceSq(SpoutUtil.toVector(ent.getScene().getPosition())) > radiusSq) {
                continue;
            }

            if (type == EntityType.ARROWS) {
                if (ent.has(Arrow.class)) {
                    ent.remove();
                    ++num;
                }
            } else if (type == EntityType.BOATS) {
                if (ent.has(Boat.class)) {
                    ent.remove();
                    ++num;
                }
            } else if (type == EntityType.ITEMS) {
                if (ent.has(Item.class)) {
                    ent.remove();
                    ++num;
                }
            } else if (type == EntityType.MINECARTS) {
                if (ent.has(Minecart.class)) {
                    ent.remove();
                    ++num;
                }
            } else if (type == EntityType.PAINTINGS) {
                if (ent.has(Painting.class)) {
                    ent.remove();
                    ++num;
                }
            } else if (type == EntityType.TNT) {
                if (ent.has(Tnt.class)) {
                    ent.remove();
                    ++num;
                }
            } else if (type == EntityType.XP_ORBS) {
                if (ent.has(XPOrb.class)) {
                    ent.remove();
                    ++num;
                }
            }
        }

        return num;
    }

    /**
     * Set a sign's text.
     *
     * @param pt
     * @param text
     * @return
     */
    /*private boolean setSignText(Vector pt, String[] text) {
        Block block = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (block == null) return false;
        BlockState state = block.getState();
        if (state == null || !(state instanceof Sign)) return false;
        Sign sign = (Sign) state;
        sign.setLine(0, text[0]);
        sign.setLine(1, text[1]);
        sign.setLine(2, text[2]);
        sign.setLine(3, text[3]);
        sign.update();
        return true;
    }*/

    /**
     * Get a sign's text.
     *
     * @param pt
     * @return
     */
    /*private String[] getSignText(Vector pt) {
        Block block = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (block == null) return new String[] { "", "", "", "" };
        BlockState state = block.getState();
        if (state == null || !(state instanceof Sign)) return new String[] { "", "", "", "" };
        Sign sign = (Sign) state;
        String line0 = sign.getLine(0);
        String line1 = sign.getLine(1);
        String line2 = sign.getLine(2);
        String line3 = sign.getLine(3);
        return new String[] {
                line0 != null ? line0 : "",
                line1 != null ? line1 : "",
                line2 != null ? line2 : "",
                line3 != null ? line3 : "",
            };
    }*/

    /**
     * Get a container block's contents.
     *
     * @param pt
     * @return
     */
    /*private BaseItemStack[] getContainerBlockContents(Vector pt) {
        Block block = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (block == null) {
            return new BaseItemStack[0];
        }
        BlockState state = block.getState();
        if (!(state instanceof org.bukkit.block.ContainerBlock)) {
            return new BaseItemStack[0];
        }

        org.bukkit.block.ContainerBlock container = (org.bukkit.block.ContainerBlock) state;
        Inventory inven = container.getInventory();
        int size = inven.getContents().length
        BaseItemStack[] contents = new BaseItemStack[size];

        for (int i = 0; i < size; ++i) {
            ItemStack bukkitStack = inven.getItem(i);
            if (bukkitStack.getMaterial() != MaterialData.air) {
                contents[i] = new BaseItemStack(
                        bukkitStack.getMaterial().getRawId(),
                        bukkitStack.getAmount(),
                        bukkitStack.getDamage());
                try {
                    for (Map.Entry<Enchantment, Integer> entry : bukkitStack.getEnchantments().entrySet()) {
                        contents[i].getEnchantments().put(entry.getKey().getId(), entry.getValue());
                    }
                } catch (Throwable ignore) {}
            }
        }

        return contents;
    }*/

    /**
     * Set a container block's contents.
     *
     * @param pt
     * @param contents
     * @return
     */
    /*private boolean setContainerBlockContents(Vector pt, BaseItemStack[] contents) {
        Block block = world.getBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (block == null) {
            return false;
        }
        BlockState state = block.getState();
        if (!(state instanceof org.bukkit.block.ContainerBlock)) {
            return false;
        }

        org.bukkit.block.ContainerBlock chest = (org.bukkit.block.ContainerBlock) state;
        Inventory inven = chest.getInventory();
        int size = inven.getSize();

        for (int i = 0; i < size; ++i) {
            if (i >= contents.length) {
                break;
            }

            if (contents[i] != null) {
                ItemStack toAdd = new ItemStack(contents[i].getType(),
                        contents[i].getAmount(),
                        (byte) contents[i].getDamage());
                try {
                    for (Map.Entry<Integer, Integer> entry : contents[i].getEnchantments().entrySet()) {
                        toAdd.addEnchantment(Enchantment.getById(entry.getKey()), entry.getValue());
                    }
                } catch (Throwable ignore) {}
                inven.setItem(i, toAdd);
            } else {
                inven.setItem(i, null);
            }
        }

        return true;
    }*/

    /**
     * Returns whether a block has a valid ID.
     *
     * @param type
     * @return
     */
    @Override
    public boolean isValidBlockType(int type) {
        return getSpoutMaterial(type) instanceof BlockMaterial;
    }

    @Override
    public void checkLoadedChunk(Vector pt) {
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SpoutWorld)) {
            return false;
        }

        return ((SpoutWorld) other).world.equals(world);
    }

    @Override
    public int hashCode() {
        return world.hashCode();
    }

    @Override
    public int getMaxY() {
        return world.getHeight() - 1; //TODO: We have infinite-height worlds now
    }

    @Override
    public void fixAfterFastMode(Iterable<BlockVector2D> chunks) {
        /*for (BlockVector2D chunkPos : chunks) {
            world.refreshChunk(chunkPos.getBlockX(), chunkPos.getBlockZ());
        }*/
    }

    /*private static final Map<Integer, Effect> effects = new HashMap<Integer, Effect>();
    static {
        for (Effect effect : Effect.values()) {
            effects.put(effect.getId(), effect);
        }
    }*/

    @Override
    public boolean playEffect(Vector position, int type, int data) {
        /*final Effect effect = effects.get(type);
        if (effect == null) {
            return false;
        }

        world.playEffect(SpoutUtil.toLocation(world, position), effect, data);

        return true;
        */
        return false;
    }

    @Override
    public SpoutEntity[] getEntities(Region region) {
        List<SpoutEntity> entities = new ArrayList<SpoutEntity>();
        for (Vector pt : region.getChunkCubes()) {
            Chunk chunk = world.getChunkFromBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), LoadOption.LOAD_GEN);
            if (chunk == null) {
                continue;
            }
            for (Entity ent : chunk.getEntities()) {
                if (region.contains(SpoutUtil.toVector(ent.getScene().getPosition()))) {
                    Collection<Class<? extends Component>> revisedComponents = Collections2.transform(ent.values(), new Function<Component, Class<? extends Component>>() {
                        @Override
                        public Class<? extends Component> apply(@Nullable Component component) {
                            return component == null ? null : component.getClass();
                        }
                    });
                    entities.add(new SpoutEntity(SpoutUtil.toLocation(ent), ent.getId(), revisedComponents, ent.getData().getBaseMap())); // TODO:; Fix entity adding
                }
            }
        }
        return entities.toArray(new SpoutEntity[entities.size()]);
    }

    @Override
    public int killEntities(LocalEntity... entities) {
        int amount = 0;
        for (LocalEntity weEnt : entities) {
            SpoutEntity entity = (SpoutEntity) weEnt;
            Entity spoutEntity = world.getEntity(entity.getEntityId());
            if (spoutEntity != null) {
                spoutEntity.remove();
                ++amount;
            }
        }
        return amount;
    }
}
