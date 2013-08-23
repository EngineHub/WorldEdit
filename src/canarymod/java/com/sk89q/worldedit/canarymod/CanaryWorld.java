package com.sk89q.worldedit.canarymod;

import java.util.ArrayList;
import java.util.List;

import net.canarymod.Canary;
import net.canarymod.api.MobSpawnerEntry;
import net.canarymod.api.entity.Arrow;
import net.canarymod.api.entity.Entity;
import net.canarymod.api.entity.EntityItem;
import net.canarymod.api.entity.Fireball;
import net.canarymod.api.entity.TNTPrimed;
import net.canarymod.api.entity.XPOrb;
import net.canarymod.api.entity.hanging.ItemFrame;
import net.canarymod.api.entity.hanging.Painting;
import net.canarymod.api.entity.living.EntityLiving;
import net.canarymod.api.entity.living.Golem;
import net.canarymod.api.entity.living.animal.EntityAnimal;
import net.canarymod.api.entity.living.animal.Tameable;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.entity.living.humanoid.Villager;
import net.canarymod.api.entity.throwable.Snowball;
import net.canarymod.api.entity.vehicle.Boat;
import net.canarymod.api.entity.vehicle.Minecart;
import net.canarymod.api.inventory.Enchantment;
import net.canarymod.api.inventory.Inventory;
import net.canarymod.api.inventory.Item;
import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.Anvil;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.blocks.Furnace;
import net.canarymod.api.world.blocks.MobSpawner;
import net.canarymod.api.world.blocks.NoteBlock;
import net.canarymod.api.world.blocks.Sign;
import net.canarymod.api.world.blocks.TileEntity;
import net.canarymod.api.world.position.Vector3D;

import com.sk89q.worldedit.BiomeType;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EntityType;
import com.sk89q.worldedit.LocalEntity;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.MobType;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.blocks.ContainerBlock;
import com.sk89q.worldedit.blocks.FurnaceBlock;
import com.sk89q.worldedit.blocks.MobSpawnerBlock;
import com.sk89q.worldedit.blocks.SignBlock;
import com.sk89q.worldedit.regions.Region;

public class CanaryWorld extends LocalWorld {

    private World world;

    public CanaryWorld(World world) {
        this.world = world;
    }

    @Override
    public String getName() {
        return world.getFqName();
    }

    @Override
    @Deprecated
    public boolean setBlockType(Vector pt, int type) {
        world.setBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), (short) type);
        return true;
    }

    @Override
    public int getBlockType(Vector pt) {
        return world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getTypeId();
    }

    @Override
    @Deprecated
    public void setBlockData(Vector pt, int data) {
        world.setDataAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), (byte) data);
    }

    @Override
    @Deprecated
    public void setBlockDataFast(Vector pt, int data) {
        world.setDataAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), (byte) data);
    }

    @Override
    public BiomeType getBiome(Vector2D pt) {
        return CanaryBiomeType.fromNative(world.getBiomeType(pt.getBlockX(), pt.getBlockZ()));
    }

    @Override
    public void setBiome(Vector2D pt, BiomeType biome) {
        world.setBiome(pt.getBlockX(), pt.getBlockZ(), ((CanaryBiomeType) biome).getCanaryBiomeType());
    }

    @Override
    public int getBlockData(Vector pt) {
        return world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getData();
    }

    @Override
    public int getBlockLightLevel(Vector pt) {
        return world.getLightLevelAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
    }

    @Override
    public boolean regenerate(Region region, EditSession editSession) {
        // Code from l4mRh4x0rs implementation
        BaseBlock[] history = new BaseBlock[16 * 16 * 128];

        for (Vector2D chunk : region.getChunks()) {
            Vector min = new Vector(chunk.getBlockX() * 16, 0, chunk.getBlockZ() * 16);

            // First save all the blocks inside
            for (int x = 0; x < 16; ++x) {
                for (int y = 0; y < 128; ++y) {
                    for (int z = 0; z < 16; ++z) {
                        Vector pt = min.add(x, y, z);
                        int index = y * 16 * 16 + z * 16 + x;
                        history[index] = editSession.getBlock(pt);
                    }
                }
            }

            try {
                world.getChunkProvider().regenerateChunk(chunk.getBlockX(), chunk.getBlockZ());
            } catch (Throwable t) {
                WorldEdit.logger.logStacktrace(t.getMessage(), t);
            }

            // Then restore
            for (int x = 0; x < 16; ++x) {
                for (int y = 0; y < 128; ++y) {
                    for (int z = 0; z < 16; ++z) {
                        Vector pt = min.add(x, y, z);
                        int index = y * 16 * 16 + z * 16 + x;

                        // We have to restore the block if it was outside
                        if (!region.contains(pt)) {
                            editSession.smartSetBlock(pt, history[index]);
                        } else { // Otherwise fool with history
                            editSession.rememberChange(pt, history[index], editSession.rawGetBlock(pt));
                        }
                    }
                }
            }
        }

        return true;
    }

    @Override
    public boolean copyToWorld(Vector pt, BaseBlock block) {
        // Signs
        if (block instanceof SignBlock) {
            setSignText(pt, ((SignBlock) block).getText());
            return true;

            // Furnaces
        } else if (block instanceof FurnaceBlock) {
            TileEntity container = world.getOnlyTileEntityAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (container == null || !(container instanceof Furnace))
                return false;
            Furnace canary = (Furnace) container;
            FurnaceBlock we = (FurnaceBlock) block;
            canary.setBurnTime(we.getBurnTime());
            canary.setCookTime(we.getCookTime());
            return setContainerBlockContents(pt, ((ContainerBlock) block).getItems());

            // Chests/dispenser
        } else if (block instanceof ContainerBlock) {
            return setContainerBlockContents(pt, ((ContainerBlock) block).getItems());

            // Mob spawners
        } else if (block instanceof MobSpawnerBlock) {
            TileEntity container = world.getOnlyTileEntityAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (container == null || !(container instanceof MobSpawner))
                return false;
            MobSpawner canary = (MobSpawner) container;
            MobSpawnerBlock we = (MobSpawnerBlock) block;
            Entity spawn = Canary.factory().getEntityFactory().newEntity(we.getMobType(), world);
            MobSpawnerEntry entry = Canary.factory().getObjectFactory().newMobSpawnerEntry(spawn);
            canary.getLogic().setSpawnedEntities(entry);
            canary.getLogic().setDelay(we.getDelay());
            return true;

            // Note block
        } else if (block instanceof com.sk89q.worldedit.blocks.NoteBlock) {
            TileEntity container = world.getOnlyTileEntityAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (container == null || !(container instanceof NoteBlock))
                return false;
            NoteBlock canary = (NoteBlock) container;
            com.sk89q.worldedit.blocks.NoteBlock we = (com.sk89q.worldedit.blocks.NoteBlock) block;
            canary.setNote(we.getNote());
            return true;
        }

        return false;
    }

    @Override
    public boolean copyFromWorld(Vector pt, BaseBlock block) {
        // Signs
        if (block instanceof SignBlock) {
            ((SignBlock) block).setText(getSignText(pt));
            return true;

            // Furnaces
        } else if (block instanceof FurnaceBlock) {
            TileEntity canaryBlock = world.getOnlyTileEntityAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (canaryBlock == null || !(canaryBlock instanceof Furnace))
                return false;
            Furnace canary = (Furnace) canaryBlock;
            FurnaceBlock we = (FurnaceBlock) block;
            we.setBurnTime(canary.getBurnTime());
            we.setCookTime(canary.getCookTime());
            ((ContainerBlock) block).setItems(getContainerBlockContents(pt));
            return true;

            // Chests/dispenser
        } else if (block instanceof ContainerBlock) {
            ((ContainerBlock) block).setItems(getContainerBlockContents(pt));
            return true;

            // Mob spawners
        } else if (block instanceof MobSpawnerBlock) {
            TileEntity canaryBlock = world.getOnlyTileEntityAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (canaryBlock == null || !(canaryBlock instanceof MobSpawner))
                return false;
            MobSpawner canary = (MobSpawner) canaryBlock;
            MobSpawnerBlock we = (MobSpawnerBlock) block;
            //So this can happen. Apparently with player-placed spawner blocks
            if(canary.getLogic().getSpawns().length > 0) {
                we.setMobType(canary.getLogic().getSpawns()[0]);
            }
            else {
                we.setMobType(MobType.ZOMBIE.getName());
            }
            we.setDelay((short) canary.getLogic().getMaxDelay());
            return true;

            // Note block
        } else if (block instanceof com.sk89q.worldedit.blocks.NoteBlock) {
            TileEntity canaryBlock = world.getOnlyTileEntityAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
            if (canaryBlock == null || !(canaryBlock instanceof NoteBlock))
                return false;
            NoteBlock canary = (NoteBlock) canaryBlock;
            com.sk89q.worldedit.blocks.NoteBlock we = (com.sk89q.worldedit.blocks.NoteBlock) block;
            we.setNote(canary.getNote());
        }

        return false;
    }

    @Override
    public boolean clearContainerBlockContents(Vector pt) {
        TileEntity block = world.getOnlyTileEntityAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (block == null || !(block instanceof Inventory)) {
            return false;
        }

        Inventory chest = (Inventory) block;
        chest.clearContents();
        return true;
    }

    @Override
    public void dropItem(Vector pt, BaseItemStack item) {
        Item canary = Canary.factory().getItemFactory().newItem(item.getType(), item.getData(), item.getAmount());
        for (Integer t : item.getEnchantments().keySet()) {
            canary.addEnchantments(Canary.factory().getItemFactory().newEnchantment(Enchantment.Type.fromId(t), item.getEnchantments().get(t).shortValue()));
        }
        world.dropItem(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), canary);
    }

    @Override
    public int removeEntities(EntityType type, Vector origin, int radius) {
        int num = 0;

        for (Entity ent : world.getEntityTracker().getTrackedEntities()) {
            Vector entPos = new Vector(ent.getX(), ent.getY(), ent.getZ());
            if (radius != -1 && entPos.distance(origin) > radius)
                continue;

            switch (type) {
            case BOATS:
                if (ent instanceof Boat) {
                    ent.destroy();
                    num++;
                }
                break;
            case ITEMS:
                if (ent instanceof EntityItem) {
                    ent.destroy();
                    num++;
                }
                break;
            case MINECARTS:
                if (ent instanceof Minecart) {
                    ent.destroy();
                    num++;
                }
                break;
            case PAINTINGS:
                if (ent instanceof Painting) {
                    ent.destroy();
                    num++;
                }
                break;
            case TNT:
                if (ent instanceof TNTPrimed) {
                    ent.destroy();
                    num++;
                }
                break;
            case XP_ORBS:
                if (ent instanceof XPOrb) {
                    ent.destroy();
                    num++;
                }
                break;
            case ALL:
                ent.destroy();
                num++;
                break;
            case FALLING_BLOCKS:
                if (ent instanceof Anvil) {
                    ent.destroy();
                    num++;
                }
                break;
            case ITEM_FRAMES:
                if (ent instanceof ItemFrame) {
                    ent.destroy();
                    num++;
                }
                break;
            case PROJECTILES:
                if (ent instanceof Arrow || ent instanceof Snowball || ent instanceof Fireball) {
                    ent.destroy();
                    num++;
                }
                break;
            default:
                break;
            }

        }
        return num;
    }

    @Override
    public int killMobs(Vector origin, double radius, int flags) {
        boolean killPets = (flags & KillFlags.PETS) != 0;
        boolean killNPCs = (flags & KillFlags.NPCS) != 0;
        boolean killAnimals = (flags & KillFlags.ANIMALS) != 0;
        boolean withLightning = (flags & KillFlags.WITH_LIGHTNING) != 0;
        boolean killGolems = (flags & KillFlags.GOLEMS) != 0;

        int num = 0;
        double radiusSq = radius * radius;

        Vector3D p = new Vector3D(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());

        for (EntityLiving ent : world.getEntityLivingList()) {
            if (ent instanceof Player) {
                continue;
            }

            if (!killAnimals && ent instanceof EntityAnimal) {
                continue;
            }

            if (!killPets && ent instanceof Tameable && ((Tameable) ent).isTamed()) {
                continue; // tamed pet
            }

            if (!killGolems && ent instanceof Golem) {
                continue;
            }

            if (!killNPCs && ent instanceof Villager) {
                continue;
            }

            // We don't have this lol
            // if (!killAmbient && ent instanceof Ambient) {
            // continue;
            // }

            if (radius < 0 || (p.getDistance(ent.getLocation())) <= radiusSq) {
                if (withLightning) {
                    world.makeLightningBolt(ent.getPosition());
                }
                ent.kill();
                ++num;
            }
        }

        return num;
    }

    @Override
    public LocalEntity[] getEntities(Region region) {
        List<CanaryEntity> entities = new ArrayList<CanaryEntity>();
        for (Entity e : world.getEntityTracker().getTrackedEntities()) {
            if (region.contains(CanaryUtil.toVector(e.getPosition()))) {
                entities.add(new CanaryEntity(e));
            }
        }
        return entities.toArray(new CanaryEntity[entities.size()]);
    }

    @Override
    public boolean isValidBlockType(int type) {
        return BlockType.fromId(type) != null;
    }

    @Override
    public void checkLoadedChunk(Vector pt) {
        if (!world.isChunkLoaded(pt.getBlockX(), pt.getBlockZ())) {
            world.loadChunk(pt.getBlockX(), pt.getBlockZ());
        }
    }

    @Override
    public int getMaxY() {
        return world.getHeight();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof CanaryWorld && world.equals(((CanaryWorld) other).world);
    }

    @Override
    public int hashCode() {
        return world.hashCode();
    }

    /**
     * Helper to set text on signs at the given {@link Vector} in this world
     *
     * @param pt
     * @param text
     */
    public void setSignText(Vector pt, String[] text) {
        Sign signData = (Sign) world.getTileEntityAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (signData == null) {
            return;
        }
        for (byte i = 0; i < 4; i++) {
            signData.setTextOnLine(text[i], i);
        }
        signData.update();
    }

    /**
     * Helper to set the inventory of a {@link TileEntity} at the given
     * {@link Vector} in this world
     *
     * @param pt
     * @param items
     * @return
     */
    private boolean setContainerBlockContents(Vector pt, BaseItemStack[] items) {
        TileEntity complex = world.getOnlyTileEntityAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (complex == null || !(complex instanceof Inventory)) {
            return false;
        }

        Inventory container = (Inventory) complex;
        for (int i = 0; i < container.getContents().length; i++) {
            if(i >= items.length) {
                break;
            }
            BaseItemStack item = items[i];
            if (item != null)
                container.setSlot(item.getType(), item.getAmount(), item.getData(), i);
        }

        return true;
    }

    /**
     * Helper to get text on signs at the given {@link Vector} in this world
     *
     * @param pt
     * @return
     */
    public String[] getSignText(Vector pt) {
        Sign signData = (Sign) world.getTileEntityAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (signData == null) {
            return new String[] { "", "", "", "" };
        }
        String[] text = new String[4];
        for (byte i = 0; i < 4; i++) {
            text[i] = signData.getTextOnLine(i);
        }
        return text;
    }

    /**
     * Helper to get the inventory of a {@link TileEntity} at the given
     * {@link Vector} in this world
     *
     * @param pt
     * @return
     */
    private BaseItemStack[] getContainerBlockContents(Vector pt) {

        TileEntity block = world.getOnlyTileEntityAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (block == null || !(block instanceof Inventory)) {
            return new BaseItemStack[0];
        }

        Inventory container = (Inventory) block;
        int size = container.getSize();
        BaseItemStack[] contents = new BaseItemStack[size];

        for (int i = 0; i < size; ++i) {
            Item canaryItem = container.getSlot(i);
            if (canaryItem != null) {
                contents[i] = new BaseItemStack(canaryItem.getId(), canaryItem.getAmount(), (short) canaryItem.getDamage());
            }
        }

        return contents;
    }

    /**
     * get the CanaryMod {@link World} that is wrapped in this
     * {@link CanaryWorld}
     *
     * @return the wrapped {@link World}
     */
    public World getHandle() {
        return world;
    }
}
