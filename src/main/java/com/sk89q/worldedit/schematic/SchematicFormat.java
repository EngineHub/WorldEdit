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

package com.sk89q.worldedit.schematic;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.data.DataException;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Represents a format that a schematic can be stored as
 * @author zml2008
 */
public abstract class SchematicFormat {
    private static final Map<String, SchematicFormat> SCHEMATIC_FORMATS = new HashMap<String, SchematicFormat>();

    // Built-in schematic formats
    public static final SchematicFormat MCEDIT = new MCEditSchematicFormat();

    public static Set<SchematicFormat> getFormats() {
        return Collections.unmodifiableSet(new HashSet<SchematicFormat>(SCHEMATIC_FORMATS.values()));
    }

    public static SchematicFormat getFormat(String lookupName) {
        return SCHEMATIC_FORMATS.get(lookupName.toLowerCase());
    }

    public static SchematicFormat getFormat(File file) {
        if (!file.isFile()) {
            return null;
        }

        for (SchematicFormat format : SCHEMATIC_FORMATS.values()) {
            if (format.isOfFormat(file)) {
                return format;
            }
        }
        return null;
    }

    private final String name;
    private final String[] lookupNames;

    protected SchematicFormat(String name, String... lookupNames) {
        this.name = name;
        List<String> registeredLookupNames = new ArrayList<String>(lookupNames.length);
        for (int i = 0; i < lookupNames.length; ++i) {
            if (i == 0 || !SCHEMATIC_FORMATS.containsKey(lookupNames[i].toLowerCase())) {
                SCHEMATIC_FORMATS.put(lookupNames[i].toLowerCase(), this);
                registeredLookupNames.add(lookupNames[i].toLowerCase());
            }
        }
        this.lookupNames = registeredLookupNames.toArray(new String[registeredLookupNames.size()]);
    }

    /**
     * Gets the official/display name for this schematic format
     *
     * @return The display name for this schematic format
     */
    public String getName() {
        return name;
    }

    public String[] getLookupNames() {
        return lookupNames;
    }

    public BaseBlock getBlockForId(int id, short data) {
        BaseBlock block;
        switch (id) {
            /*case BlockID.WALL_SIGN:
            case BlockID.SIGN_POST:
                block = new SignBlock(id, data);
                break;

            case BlockID.CHEST:
                block = new ChestBlock(data);
                break;

            case BlockID.FURNACE:
            case BlockID.BURNING_FURNACE:
                block = new FurnaceBlock(id, data);
                break;

            case BlockID.DISPENSER:
                block = new DispenserBlock(data);
                break;

            case BlockID.MOB_SPAWNER:
                block = new MobSpawnerBlock(id);
                break;

            case BlockID.NOTE_BLOCK:
                block = new NoteBlock(data);
                break;*/

            default:
                block = new BaseBlock(id, data);
                break;
        }
        return block;
    }

    /**
     * Loads a schematic from the given file into a CuboidClipboard
     * @param file The file to load from
     * @return The CuboidClipboard containing the contents of this schematic
     * @throws IOException If an error occurs while reading data
     * @throws DataException if data is not in the correct format
     */
    public abstract CuboidClipboard load(File file) throws IOException, DataException;

    /**
     * Saves the data from the specified CuboidClipboard to the given file, overwriting any
     * existing data in the file
     * @param clipboard The clipboard to get data from
     * @param file The file to save to
     * @throws IOException If an error occurs while writing data
     * @throws DataException If the clipboard has data which cannot be stored
     */
    public abstract void save(CuboidClipboard clipboard, File file) throws IOException, DataException;

    public abstract boolean isOfFormat(File file);
}
