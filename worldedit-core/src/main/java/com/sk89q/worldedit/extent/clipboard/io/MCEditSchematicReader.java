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

package com.sk89q.worldedit.extent.clipboard.io;

import com.google.common.collect.ImmutableList;
import com.sk89q.jnbt.AdventureNBTConverter;
import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NamedTag;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.legacycompat.BannerBlockCompatibilityHandler;
import com.sk89q.worldedit.extent.clipboard.io.legacycompat.BedBlockCompatibilityHandler;
import com.sk89q.worldedit.extent.clipboard.io.legacycompat.EntityNBTCompatibilityHandler;
import com.sk89q.worldedit.extent.clipboard.io.legacycompat.FlowerPotCompatibilityHandler;
import com.sk89q.worldedit.extent.clipboard.io.legacycompat.NBTCompatibilityHandler;
import com.sk89q.worldedit.extent.clipboard.io.legacycompat.NoteBlockCompatibilityHandler;
import com.sk89q.worldedit.extent.clipboard.io.legacycompat.Pre13HangingCompatibilityHandler;
import com.sk89q.worldedit.extent.clipboard.io.legacycompat.SignCompatibilityHandler;
import com.sk89q.worldedit.extent.clipboard.io.legacycompat.SkullBlockCompatibilityHandler;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.entity.EntityTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import com.sk89q.worldedit.world.storage.NBTConversions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Reads schematic files that are compatible with MCEdit and other editors.
 */
public class MCEditSchematicReader extends NBTSchematicReader {

    private static final Logger log = LoggerFactory.getLogger(MCEditSchematicReader.class);
    private final NBTInputStream inputStream;
    private final DataFixer fixer;
    private static final ImmutableList<NBTCompatibilityHandler> COMPATIBILITY_HANDLERS
            = ImmutableList.of(
                new SignCompatibilityHandler(),
                new FlowerPotCompatibilityHandler(),
                new NoteBlockCompatibilityHandler(),
                new SkullBlockCompatibilityHandler(),
                new BannerBlockCompatibilityHandler(),
                new BedBlockCompatibilityHandler()
    );
    private static final ImmutableList<EntityNBTCompatibilityHandler> ENTITY_COMPATIBILITY_HANDLERS
            = ImmutableList.of(
                    new Pre13HangingCompatibilityHandler()
    );

    /**
     * Create a new instance.
     *
     * @param inputStream the input stream to read from
     */
    public MCEditSchematicReader(NBTInputStream inputStream) {
        checkNotNull(inputStream);
        this.inputStream = inputStream;
        this.fixer = null;
        //com.sk89q.worldedit.WorldEdit.getInstance().getPlatformManager().queryCapability(
        //com.sk89q.worldedit.extension.platform.Capability.WORLD_EDITING).getDataFixer();
    }

    @Override
    public Clipboard read() throws IOException {
        // Schematic tag
        NamedTag rootTag = inputStream.readNamedTag();
        if (!rootTag.getName().equals("Schematic")) {
            throw new IOException("Tag 'Schematic' does not exist or is not first");
        }
        CompoundTag schematicTag = (CompoundTag) rootTag.getTag();

        // Check
        Map<String, Tag> schematic = schematicTag.getValue();
        if (!schematic.containsKey("Blocks")) {
            throw new IOException("Schematic file is missing a 'Blocks' tag");
        }

        // Check type of Schematic
        String materials = requireTag(schematic, "Materials", StringTag.class).getValue();
        if (!materials.equals("Alpha")) {
            throw new IOException("Schematic file is not an Alpha schematic");
        }

        // ====================================================================
        // Metadata
        // ====================================================================

        BlockVector3 origin;
        Region region;

        // Get information
        short width = requireTag(schematic, "Width", ShortTag.class).getValue();
        short height = requireTag(schematic, "Height", ShortTag.class).getValue();
        short length = requireTag(schematic, "Length", ShortTag.class).getValue();

        try {
            int originX = requireTag(schematic, "WEOriginX", IntTag.class).getValue();
            int originY = requireTag(schematic, "WEOriginY", IntTag.class).getValue();
            int originZ = requireTag(schematic, "WEOriginZ", IntTag.class).getValue();
            BlockVector3 min = BlockVector3.at(originX, originY, originZ);

            int offsetX = requireTag(schematic, "WEOffsetX", IntTag.class).getValue();
            int offsetY = requireTag(schematic, "WEOffsetY", IntTag.class).getValue();
            int offsetZ = requireTag(schematic, "WEOffsetZ", IntTag.class).getValue();
            BlockVector3 offset = BlockVector3.at(offsetX, offsetY, offsetZ);

            origin = min.subtract(offset);
            region = new CuboidRegion(min, min.add(width, height, length).subtract(BlockVector3.ONE));
        } catch (IOException ignored) {
            origin = BlockVector3.ZERO;
            region = new CuboidRegion(origin, origin.add(width, height, length).subtract(BlockVector3.ONE));
        }

        // ====================================================================
        // Blocks
        // ====================================================================

        // Get blocks
        byte[] blockId = requireTag(schematic, "Blocks", ByteArrayTag.class).getValue();
        byte[] blockData = requireTag(schematic, "Data", ByteArrayTag.class).getValue();
        byte[] addId = new byte[0];
        short[] blocks = new short[blockId.length]; // Have to later combine IDs

        // We support 4096 block IDs using the same method as vanilla Minecraft, where
        // the highest 4 bits are stored in a separate byte array.
        if (schematic.containsKey("AddBlocks")) {
            addId = requireTag(schematic, "AddBlocks", ByteArrayTag.class).getValue();
        }

        // Combine the AddBlocks data with the first 8-bit block ID
        for (int index = 0; index < blockId.length; index++) {
            if ((index >> 1) >= addId.length) { // No corresponding AddBlocks index
                blocks[index] = (short) (blockId[index] & 0xFF);
            } else {
                if ((index & 1) == 0) {
                    blocks[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (blockId[index] & 0xFF));
                } else {
                    blocks[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (blockId[index] & 0xFF));
                }
            }
        }

        // Need to pull out tile entities
        final ListTag tileEntityTag = getTag(schematic, "TileEntities", ListTag.class);
        List<Tag> tileEntities = tileEntityTag == null ? new ArrayList<>() : tileEntityTag.getValue();
        Map<BlockVector3, Map<String, Tag>> tileEntitiesMap = new HashMap<>();
        Map<BlockVector3, BlockState> blockStates = new HashMap<>();

        for (Tag tag : tileEntities) {
            if (!(tag instanceof CompoundTag)) {
                continue;
            }
            CompoundTag t = (CompoundTag) tag;
            Map<String, Tag> values = new HashMap<>(t.getValue());
            String id = t.getString("id");
            values.put("id", new StringTag(convertBlockEntityId(id)));
            int x = t.getInt("x");
            int y = t.getInt("y");
            int z = t.getInt("z");
            int index = y * width * length + z * width + x;

            BlockState block = getBlockState(blocks[index], blockData[index]);
            BlockState newBlock = block;
            if (newBlock != null) {
                for (NBTCompatibilityHandler handler : COMPATIBILITY_HANDLERS) {
                    if (handler.isAffectedBlock(newBlock)) {
                        newBlock = handler.updateNBT(block, values).toImmutableState();
                        if (newBlock == null || values.isEmpty()) {
                            break;
                        }
                    }
                }
            }
            if (values.isEmpty()) {
                t = null;
            } else {
                t = new CompoundTag(values);
            }

            if (fixer != null && t != null) {
                t = (CompoundTag) AdventureNBTConverter.fromAdventure(fixer.fixUp(DataFixer.FixTypes.BLOCK_ENTITY, t.asBinaryTag(), -1));
            }

            BlockVector3 vec = BlockVector3.at(x, y, z);
            if (t != null) {
                tileEntitiesMap.put(vec, t.getValue());
            }
            blockStates.put(vec, newBlock);
        }

        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        clipboard.setOrigin(origin);


        Set<Integer> unknownBlocks = new HashSet<>();
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                    int index = y * width * length + z * width + x;
                    BlockVector3 pt = BlockVector3.at(x, y, z);
                    BlockState state = blockStates.computeIfAbsent(pt, p -> getBlockState(blocks[index], blockData[index]));

                    try {
                        if (state != null) {
                            if (tileEntitiesMap.containsKey(pt)) {
                                clipboard.setBlock(region.getMinimumPoint().add(pt), state.toBaseBlock(new CompoundTag(tileEntitiesMap.get(pt))));
                            } else {
                                clipboard.setBlock(region.getMinimumPoint().add(pt), state);
                            }
                        } else {
                            short block = blocks[index];
                            byte data = blockData[index];
                            int combined = block << 8 | data;
                            if (unknownBlocks.add(combined)) {
                                log.warn("Unknown block when loading schematic: "
                                        + block + ":" + data + ". This is most likely a bad schematic.");
                            }
                        }
                    } catch (WorldEditException ignored) { // BlockArrayClipboard won't throw this
                    }
                }
            }
        }

        // ====================================================================
        // Entities
        // ====================================================================

        ListTag entityList = getTag(schematic, "Entities", ListTag.class);
        if (entityList != null) {
            List<Tag> entityTags = entityList.getValue();
            for (Tag tag : entityTags) {
                if (tag instanceof CompoundTag) {
                    CompoundTag compound = (CompoundTag) tag;
                    if (fixer != null) {
                        compound = (CompoundTag) AdventureNBTConverter.fromAdventure(fixer.fixUp(DataFixer.FixTypes.ENTITY, compound.asBinaryTag(), -1));
                    }
                    String id = convertEntityId(compound.getString("id"));
                    Location location = NBTConversions.toLocation(clipboard, compound.getListTag("Pos"), compound.getListTag("Rotation"));
                    if (!id.isEmpty()) {
                        EntityType entityType = EntityTypes.get(id.toLowerCase(Locale.ROOT));
                        if (entityType != null) {
                            for (EntityNBTCompatibilityHandler compatibilityHandler : ENTITY_COMPATIBILITY_HANDLERS) {
                                if (compatibilityHandler.isAffectedEntity(entityType, compound)) {
                                    compound = compatibilityHandler.updateNBT(entityType, compound);
                                }
                            }
                            BaseEntity state = new BaseEntity(entityType, compound);
                            clipboard.createEntity(location, state);
                        } else {
                            log.warn("Unknown entity when pasting schematic: " + id.toLowerCase(Locale.ROOT));
                        }
                    }
                }
            }
        }

        return clipboard;
    }

    private String convertEntityId(String id) {
        switch (id) {
            case "AreaEffectCloud": return "area_effect_cloud";
            case "ArmorStand": return "armor_stand";
            case "CaveSpider": return "cave_spider";
            case "MinecartChest": return "chest_minecart";
            case "DragonFireball": return "dragon_fireball";
            case "ThrownEgg": return "egg";
            case "EnderDragon": return "ender_dragon";
            case "ThrownEnderpearl": return "ender_pearl";
            case "FallingSand": return "falling_block";
            case "FireworksRocketEntity": return "fireworks_rocket";
            case "MinecartFurnace": return "furnace_minecart";
            case "MinecartHopper": return "hopper_minecart";
            case "EntityHorse": return "horse";
            case "ItemFrame": return "item_frame";
            case "LeashKnot": return "leash_knot";
            case "LightningBolt": return "lightning_bolt";
            case "LavaSlime": return "magma_cube";
            case "MinecartRideable": return "minecart";
            case "MushroomCow": return "mooshroom";
            case "Ozelot": return "ocelot";
            case "PolarBear": return "polar_bear";
            case "ThrownPotion": return "potion";
            case "ShulkerBullet": return "shulker_bullet";
            case "SmallFireball": return "small_fireball";
            case "MinecartSpawner": return "spawner_minecart";
            case "SpectralArrow": return "spectral_arrow";
            case "PrimedTnt": return "tnt";
            case "MinecartTNT": return "tnt_minecart";
            case "VillagerGolem": return "villager_golem";
            case "WitherBoss": return "wither";
            case "WitherSkull": return "wither_skull";
            case "PigZombie": return "zombie_pigman";
            case "XPOrb":
            case "xp_orb":
                return "experience_orb";
            case "ThrownExpBottle":
            case "xp_bottle":
                return "experience_bottle";
            case "EyeOfEnderSignal":
            case "eye_of_ender_signal":
                return "eye_of_ender";
            case "EnderCrystal":
            case "ender_crystal":
                return "end_crystal";
            case "fireworks_rocket": return "firework_rocket";
            case "MinecartCommandBlock":
            case "commandblock_minecart":
                return "command_block_minecart";
            case "snowman": return "snow_golem";
            case "villager_golem": return "iron_golem";
            case "evocation_fangs": return "evoker_fangs";
            case "evocation_illager": return "evoker";
            case "vindication_illager": return "vindicator";
            case "illusion_illager": return "illusioner";
            default: return id;
        }
    }

    private String convertBlockEntityId(String id) {
        switch (id) {
            case "Cauldron":
                return "brewing_stand";
            case "Control":
                return "command_block";
            case "DLDetector":
                return "daylight_detector";
            case "Trap":
                return "dispenser";
            case "EnchantTable":
                return "enchanting_table";
            case "EndGateway":
                return "end_gateway";
            case "AirPortal":
                return "end_portal";
            case "EnderChest":
                return "ender_chest";
            case "FlowerPot":
                return "flower_pot";
            case "RecordPlayer":
                return "jukebox";
            case "MobSpawner":
                return "mob_spawner";
            case "Music":
            case "noteblock":
                return "note_block";
            case "Structure":
                return "structure_block";
            case "Chest":
                return "chest";
            case "Sign":
                return "sign";
            case "Banner":
                return "banner";
            case "Beacon":
                return "beacon";
            case "Comparator":
                return "comparator";
            case "Dropper":
                return "dropper";
            case "Furnace":
                return "furnace";
            case "Hopper":
                return "hopper";
            case "Skull":
                return "skull";
            default:
                return id;
        }
    }

    private BlockState getBlockState(int id, int data) {
        return LegacyMapper.getInstance().getBlockFromLegacy(id, data);
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
