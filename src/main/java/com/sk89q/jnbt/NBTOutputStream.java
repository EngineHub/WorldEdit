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
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;
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
 * This class writes <strong>NBT</strong>, or <strong>Named Binary Tag</strong>
 * <code>Tag</code> objects to an underlying <code>OutputStream</code>.
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
public final class NBTOutputStream implements Closeable {

    /**
     * The output stream.
     */
    private final DataOutputStream os;

    /**
     * Creates a new <code>NBTOutputStream</code>, which will write data to the
     * specified underlying output stream.
     * 
     * @param os
     *            The output stream.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public NBTOutputStream(OutputStream os) throws IOException {
        this.os = new DataOutputStream(new GZIPOutputStream(os));
    }

    /**
     * Writes a tag.
     * 
     * @param tag
     *            The tag to write.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public void writeTag(Tag tag) throws IOException {
        int type = NBTUtils.getTypeCode(tag.getClass());
        String name = tag.getName();
        byte[] nameBytes = name.getBytes(NBTConstants.CHARSET);

        os.writeByte(type);
        os.writeShort(nameBytes.length);
        os.write(nameBytes);

        if (type == NBTConstants.TYPE_END) {
            throw new IOException("Named TAG_End not permitted.");
        }

        writeTagPayload(tag);
    }

    /**
     * Writes tag payload.
     * 
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeTagPayload(Tag tag) throws IOException {
        int type = NBTUtils.getTypeCode(tag.getClass());
        switch (type) {
        case NBTConstants.TYPE_END:
            writeEndTagPayload((EndTag) tag);
            break;
        case NBTConstants.TYPE_BYTE:
            writeByteTagPayload((ByteTag) tag);
            break;
        case NBTConstants.TYPE_SHORT:
            writeShortTagPayload((ShortTag) tag);
            break;
        case NBTConstants.TYPE_INT:
            writeIntTagPayload((IntTag) tag);
            break;
        case NBTConstants.TYPE_LONG:
            writeLongTagPayload((LongTag) tag);
            break;
        case NBTConstants.TYPE_FLOAT:
            writeFloatTagPayload((FloatTag) tag);
            break;
        case NBTConstants.TYPE_DOUBLE:
            writeDoubleTagPayload((DoubleTag) tag);
            break;
        case NBTConstants.TYPE_BYTE_ARRAY:
            writeByteArrayTagPayload((ByteArrayTag) tag);
            break;
        case NBTConstants.TYPE_STRING:
            writeStringTagPayload((StringTag) tag);
            break;
        case NBTConstants.TYPE_LIST:
            writeListTagPayload((ListTag) tag);
            break;
        case NBTConstants.TYPE_COMPOUND:
            writeCompoundTagPayload((CompoundTag) tag);
            break;
        case NBTConstants.TYPE_INT_ARRAY:
            writeIntArrayTagPayload((IntArrayTag) tag);
            break;
        default:
            throw new IOException("Invalid tag type: " + type + ".");
        }
    }

    /**
     * Writes a <code>TAG_Byte</code> tag.
     * 
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeByteTagPayload(ByteTag tag) throws IOException {
        os.writeByte(tag.getValue());
    }

    /**
     * Writes a <code>TAG_Byte_Array</code> tag.
     * 
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeByteArrayTagPayload(ByteArrayTag tag) throws IOException {
        byte[] bytes = tag.getValue();
        os.writeInt(bytes.length);
        os.write(bytes);
    }

    /**
     * Writes a <code>TAG_Compound</code> tag.
     * 
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeCompoundTagPayload(CompoundTag tag) throws IOException {
        for (Tag childTag : tag.getValue().values()) {
            writeTag(childTag);
        }
        os.writeByte((byte) 0); // end tag - better way?
    }

    /**
     * Writes a <code>TAG_List</code> tag.
     * 
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeListTagPayload(ListTag tag) throws IOException {
        Class<? extends Tag> clazz = tag.getType();
        List<Tag> tags = tag.getValue();
        int size = tags.size();

        os.writeByte(NBTUtils.getTypeCode(clazz));
        os.writeInt(size);
        for (int i = 0; i < size; ++i) {
            writeTagPayload(tags.get(i));
        }
    }

    /**
     * Writes a <code>TAG_String</code> tag.
     * 
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeStringTagPayload(StringTag tag) throws IOException {
        byte[] bytes = tag.getValue().getBytes(NBTConstants.CHARSET);
        os.writeShort(bytes.length);
        os.write(bytes);
    }

    /**
     * Writes a <code>TAG_Double</code> tag.
     * 
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeDoubleTagPayload(DoubleTag tag) throws IOException {
        os.writeDouble(tag.getValue());
    }

    /**
     * Writes a <code>TAG_Float</code> tag.
     * 
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeFloatTagPayload(FloatTag tag) throws IOException {
        os.writeFloat(tag.getValue());
    }

    /**
     * Writes a <code>TAG_Long</code> tag.
     * 
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeLongTagPayload(LongTag tag) throws IOException {
        os.writeLong(tag.getValue());
    }

    /**
     * Writes a <code>TAG_Int</code> tag.
     * 
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeIntTagPayload(IntTag tag) throws IOException {
        os.writeInt(tag.getValue());
    }

    /**
     * Writes a <code>TAG_Short</code> tag.
     * 
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeShortTagPayload(ShortTag tag) throws IOException {
        os.writeShort(tag.getValue());
    }

    /**
     * Writes a <code>TAG_Empty</code> tag.
     * 
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeEndTagPayload(EndTag tag) {
        /* empty */
    }
    
    private void writeIntArrayTagPayload(IntArrayTag tag) throws IOException {
        int[] data = tag.getValue();
        os.writeInt(data.length);
        for (int i = 0; i < data.length; i++) {
            os.writeInt(data[i]);
        } 
    }

    public void close() throws IOException {
        os.close();
    }

}
