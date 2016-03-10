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

package com.sk89q.worldedit.event.platform;

import java.util.HashMap;

import com.sk89q.jnbt.*;
import com.sk89q.worldedit.event.Cancellable;
import com.sk89q.worldedit.event.Event;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.SchematicWriter;
import com.sk89q.worldedit.world.registry.WorldData;

/**
 * Called when a schematic is saved
 */
public abstract class SchematicEvent extends Event {

    private SchematicWriter schematicWriter;
    private Clipboard clipboard;
    private WorldData data;

    /**
     * Create a new event.
     */
    public SchematicEvent(SchematicWriter schematicWriter, Clipboard clipboard,
            WorldData data) {
        this.clipboard = clipboard;
        this.data = data;
        this.schematicWriter = schematicWriter;
    }

    /**
     * @return CuboidClipboard instance used to store the current clipboard.
     */
    public Clipboard getClipboard() {
        return this.clipboard;
    }

    /**
     * @return WorldData instance used to store the current clipboard's world
     *         data.
     */
    public WorldData getWorldData() {
        return data;
    }

    /**
     * @return SchematicWriter instance for this schematic.
     */
    public SchematicWriter getSchematicWriter() {
        return schematicWriter;
    }

    public static class WritePre extends SchematicEvent implements Cancellable {

        private boolean cancelled;
        private NBTOutputStream outputStream;

        /**
         * Create a new pre write event.
         * 
         * @param outputStream
         */
        public WritePre(SchematicWriter schematicWriter, Clipboard clipboard,
                WorldData data, NBTOutputStream outputStream) {
            super(schematicWriter, clipboard, data);
            this.outputStream = outputStream;
        }

        public NBTOutputStream getOutputStream() {
            return outputStream;
        }

        @Override
        public boolean isCancelled() {
            return this.cancelled;
        }

        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }

    public static class Write extends SchematicEvent implements Cancellable {

        private boolean cancelled;
        private CompoundTag schematicTag;
        private HashMap<String, Tag> schematic;
        private NBTOutputStream outputStream;

        /**
         * Create a new write event.
         * 
         * @param outputStream
         */
        public Write(SchematicWriter schematicWriter, Clipboard clipboard,
                WorldData data, CompoundTag schematicTag,
                HashMap<String, Tag> schematic, NBTOutputStream outputStream) {
            super(schematicWriter, clipboard, data);
            this.schematicTag = schematicTag;
            this.schematic = schematic;
            this.outputStream = outputStream;
        }

        /**
         * @return NBT Compound tag created for the schematic.
         */
        public CompoundTag getSchematicTag() {
            return this.schematicTag;
        }

        /**
         * @return HashMap<String, Tag> instance containing the schematic's
         *         values.
         */
        public HashMap<String, Tag> getSchematicMap() {
            return this.schematic;
        }

        public NBTOutputStream getOutputStream() {
            return outputStream;
        }

        @Override
        public boolean isCancelled() {
            return this.cancelled;
        }

        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }

    public static class WritePost extends SchematicEvent {

        private CompoundTag schematicTag;

        /**
         * Create a new post write event.
         */
        public WritePost(SchematicWriter schematicWriter, Clipboard clipboard,
                WorldData data, CompoundTag schematicTag) {
            super(schematicWriter, clipboard, data);
            this.schematicTag = schematicTag;
        }

        /**
         * @return NBT Compound tag created for the schematic.
         */
        public CompoundTag getSchematicTag() {
            return this.schematicTag;
        }
    }
}
