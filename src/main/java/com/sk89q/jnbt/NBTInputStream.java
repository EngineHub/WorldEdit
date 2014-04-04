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

package com.sk89q.jnbt;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.DoubleTag;
import com.sk89q.jnbt.EndTag;
import com.sk89q.jnbt.FloatTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.LongTag;
import com.sk89q.jnbt.NBTConstants;
import com.sk89q.jnbt.NBTUtils;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;

/**
 * <p>
 * This class reads <strong>NBT</strong>, or <strong>Named Binary Tag</strong>
 * streams, and produces an object graph of subclasses of the <code>Tag</code>
 * object.
 * </p>
 * 
 * <p>
 * The NBT format was created by Markus Persson, and the specification may be
 * found at <a href="http://www.minecraft.net/docs/NBT.txt">
 * http://www.minecraft.net/docs/NBT.txt</a>.
 * </p>
 * 
 * @author Graham Edgecombe
 * 
 */
public final class NBTInputStream implements Closeable {

    /**
     * The data input stream.
     */
    private final DataInputStream is;

    /**
     * Creates a new <code>NBTInputStream</code>, which will source its data
     * from the specified input stream.
     * 
     * @param is
     *            The input stream.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public NBTInputStream(InputStream is) throws IOException {
        this.is = new DataInputStream(is);
    }

    /**
     * Reads an NBT tag from the stream.
     * 
     * @return The tag that was read.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public Tag readTag() throws IOException {
        return readTag(0);
    }

    /**
     * Reads an NBT from the stream.
     * 
     * @param depth
     *            The depth of this tag.
     * @return The tag that was read.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private Tag readTag(int depth) throws IOException {
        int type = is.readByte() & 0xFF;

        String name;
        if (type != NBTConstants.TYPE_END) {
            int nameLength = is.readShort() & 0xFFFF;
            byte[] nameBytes = new byte[nameLength];
            is.readFully(nameBytes);
            name = new String(nameBytes, NBTConstants.CHARSET);
        } else {
            name = "";
        }

        return readTagPayload(type, name, depth);
    }

    /**
     * Reads the payload of a tag, given the name and type.
     * 
     * @param type
     *            The type.
     * @param name
     *            The name.
     * @param depth
     *            The depth.
     * @return The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private Tag readTagPayload(int type, String name, int depth)
            throws IOException {
        switch (type) {
        case NBTConstants.TYPE_END:
            if (depth == 0) {
                throw new IOException(
                        "TAG_End found without a TAG_Compound/TAG_List tag preceding it.");
            } else {
                return new EndTag();
            }
        case NBTConstants.TYPE_BYTE:
            return new ByteTag(name, is.readByte());
        case NBTConstants.TYPE_SHORT:
            return new ShortTag(name, is.readShort());
        case NBTConstants.TYPE_INT:
            return new IntTag(name, is.readInt());
        case NBTConstants.TYPE_LONG:
            return new LongTag(name, is.readLong());
        case NBTConstants.TYPE_FLOAT:
            return new FloatTag(name, is.readFloat());
        case NBTConstants.TYPE_DOUBLE:
            return new DoubleTag(name, is.readDouble());
        case NBTConstants.TYPE_BYTE_ARRAY:
            int length = is.readInt();
            byte[] bytes = new byte[length];
            is.readFully(bytes);
            return new ByteArrayTag(name, bytes);
        case NBTConstants.TYPE_STRING:
            length = is.readShort();
            bytes = new byte[length];
            is.readFully(bytes);
            return new StringTag(name, new String(bytes, NBTConstants.CHARSET));
        case NBTConstants.TYPE_LIST:
            int childType = is.readByte();
            length = is.readInt();

            List<Tag> tagList = new ArrayList<Tag>();
            for (int i = 0; i < length; ++i) {
                Tag tag = readTagPayload(childType, "", depth + 1);
                if (tag instanceof EndTag) {
                    throw new IOException("TAG_End not permitted in a list.");
                }
                tagList.add(tag);
            }

            return new ListTag(name, NBTUtils.getTypeClass(childType), tagList);
        case NBTConstants.TYPE_COMPOUND:
            Map<String, Tag> tagMap = new HashMap<String, Tag>();
            while (true) {
                Tag tag = readTag(depth + 1);
                if (tag instanceof EndTag) {
                    break;
                } else {
                    tagMap.put(tag.getName(), tag);
                }
            }

            return new CompoundTag(name, tagMap);
        case NBTConstants.TYPE_INT_ARRAY:
            length = is.readInt();
            int[] data = new int[length];
            for (int i = 0; i < length; i++) {
                data[i] = is.readInt();
            }
            return new IntArrayTag(name, data);
        default:
            throw new IOException("Invalid tag type: " + type + ".");
        }
    }

    public void close() throws IOException {
        is.close();
    }

}
