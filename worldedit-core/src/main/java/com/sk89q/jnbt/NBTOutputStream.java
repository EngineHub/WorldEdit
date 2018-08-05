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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * This class writes <strong>NBT</strong>, or <strong>Named Binary Tag</strong>
 * {@code Tag} objects to an underlying {@code OutputStream}.
 * 
 * <p>The NBT format was created by Markus Persson, and the specification may be
 * found at <a href="http://www.minecraft.net/docs/NBT.txt">
 * http://www.minecraft.net/docs/NBT.txt</a>.</p>
 */
public final class NBTOutputStream implements Closeable {

    /**
     * The output stream.
     */
    private final DataOutputStream os;

    /**
     * Creates a new {@code NBTOutputStream}, which will write data to the
     * specified underlying output stream.
     * 
     * @param os
     *            The output stream.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public NBTOutputStream(OutputStream os) throws IOException {
        this.os = new DataOutputStream(os);
    }

    /**
     * Writes a tag.
     * 
     * @param tag
     *            The tag to write.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public void writeNamedTag(String name, Tag tag) throws IOException {
        checkNotNull(name);
        checkNotNull(tag);

        int type = NBTUtils.getTypeCode(tag.getClass());
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
        case NBTConstants.TYPE_LONG_ARRAY:
            writeLongArrayTagPayload((LongArrayTag) tag);
            break;
        default:
            throw new IOException("Invalid tag type: " + type + ".");
        }
    }

    /**
     * Writes a {@code TAG_Byte} tag.
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
     * Writes a {@code TAG_Byte_Array} tag.
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
     * Writes a {@code TAG_Compound} tag.
     * 
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeCompoundTagPayload(CompoundTag tag) throws IOException {
        for (Map.Entry<String, Tag> entry : tag.getValue().entrySet()) {
            writeNamedTag(entry.getKey(), entry.getValue());
        }
        os.writeByte((byte) 0); // end tag - better way?
    }

    /**
     * Writes a {@code TAG_List} tag.
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
        for (Tag tag1 : tags) {
            writeTagPayload(tag1);
        }
    }

    /**
     * Writes a {@code TAG_String} tag.
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
     * Writes a {@code TAG_Double} tag.
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
     * Writes a {@code TAG_Float} tag.
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
     * Writes a {@code TAG_Long} tag.
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
     * Writes a {@code TAG_Int} tag.
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
     * Writes a {@code TAG_Short} tag.
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
     * Writes a {@code TAG_Empty} tag.
     * 
     * @param tag the tag
     */
    private void writeEndTagPayload(EndTag tag) {
        /* empty */
    }
    
    private void writeIntArrayTagPayload(IntArrayTag tag) throws IOException {
        int[] data = tag.getValue();
        os.writeInt(data.length);
        for (int aData : data) {
            os.writeInt(aData);
        } 
    }

    private void writeLongArrayTagPayload(LongArrayTag tag) throws IOException {
        long[] data = tag.getValue();
        os.writeInt(data.length);
        for (long aData : data) {
            os.writeLong(aData);
        }
    }

    @Override
    public void close() throws IOException {
        os.close();
    }

}
