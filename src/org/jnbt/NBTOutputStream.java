package org.jnbt;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/*
 * JNBT License
 * 
 * Copyright (c) 2010 Graham Edgecombe
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *       
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *       
 *     * Neither the name of the JNBT team nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
 */

/**
 * <p>This class writes <strong>NBT</strong>, or
 * <strong>Named Binary Tag</strong> <code>Tag</code> objects to an underlying
 * <code>OutputStream</code>.</p>
 * 
 * <p>The NBT format was created by Markus Persson, and the specification may
 * be found at <a href="http://www.minecraft.net/docs/NBT.txt">
 * http://www.minecraft.net/docs/NBT.txt</a>.</p>
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
	 * @param os The output stream.
	 * @throws IOException if an I/O error occurs.
	 */
	public NBTOutputStream(OutputStream os) throws IOException {
		this.os = new DataOutputStream(new GZIPOutputStream(os));
	}
	
	/**
	 * Writes a tag.
	 * @param tag The tag to write.
	 * @throws IOException if an I/O error occurs.
	 */
	public void writeTag(Tag tag) throws IOException {
		int type = NBTUtils.getTypeCode(tag.getClass());
		String name = tag.getName();
		byte[] nameBytes = name.getBytes(NBTConstants.CHARSET);
		
		os.writeByte(type);
		os.writeShort(nameBytes.length);
		os.write(nameBytes);
		
		if(type == NBTConstants.TYPE_END) {
			throw new IOException("Named TAG_End not permitted.");
		}
		
		writeTagPayload(tag);
	}

	/**
	 * Writes tag payload.
	 * @param tag The tag.
	 * @throws IOException if an I/O error occurs.
	 */
	private void writeTagPayload(Tag tag) throws IOException {
		int type = NBTUtils.getTypeCode(tag.getClass());
		switch(type) {
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
		default:
			throw new IOException("Invalid tag type: " + type + ".");
		}
	}

	/**
	 * Writes a <code>TAG_Byte</code> tag.
	 * @param tag The tag.
	 * @throws IOException if an I/O error occurs.
	 */
	private void writeByteTagPayload(ByteTag tag) throws IOException {
		os.writeByte(tag.getValue());
	}

	/**
	 * Writes a <code>TAG_Byte_Array</code> tag.
	 * @param tag The tag.
	 * @throws IOException if an I/O error occurs.
	 */
	private void writeByteArrayTagPayload(ByteArrayTag tag) throws IOException {
		byte[] bytes = tag.getValue();
		os.writeInt(bytes.length);
		os.write(bytes);
	}

	/**
	 * Writes a <code>TAG_Compound</code> tag.
	 * @param tag The tag.
	 * @throws IOException if an I/O error occurs.
	 */
	private void writeCompoundTagPayload(CompoundTag tag) throws IOException {
		for(Tag childTag : tag.getValue().values()) {
			writeTag(childTag);
		}
		os.writeByte((byte) 0); // end tag - better way?
	}

	/**
	 * Writes a <code>TAG_List</code> tag.
	 * @param tag The tag.
	 * @throws IOException if an I/O error occurs.
	 */
	private void writeListTagPayload(ListTag tag) throws IOException {
		Class<? extends Tag> clazz = tag.getType();
		List<Tag> tags = tag.getValue();
		int size = tags.size();
		
		os.writeByte(NBTUtils.getTypeCode(clazz));
		os.writeInt(size);
		for(int i = 0; i < size; i++) {
			writeTagPayload(tags.get(i));
		}
	}

	/**
	 * Writes a <code>TAG_String</code> tag.
	 * @param tag The tag.
	 * @throws IOException if an I/O error occurs.
	 */
	private void writeStringTagPayload(StringTag tag) throws IOException {
		byte[] bytes = tag.getValue().getBytes(NBTConstants.CHARSET);
		os.writeShort(bytes.length);
		os.write(bytes);
	}

	/**
	 * Writes a <code>TAG_Double</code> tag.
	 * @param tag The tag.
	 * @throws IOException if an I/O error occurs.
	 */
	private void writeDoubleTagPayload(DoubleTag tag) throws IOException {
		os.writeDouble(tag.getValue());
	}

	/**
	 * Writes a <code>TAG_Float</code> tag.
	 * @param tag The tag.
	 * @throws IOException if an I/O error occurs.
	 */
	private void writeFloatTagPayload(FloatTag tag) throws IOException {
		os.writeFloat(tag.getValue());
	}

	/**
	 * Writes a <code>TAG_Long</code> tag.
	 * @param tag The tag.
	 * @throws IOException if an I/O error occurs.
	 */
	private void writeLongTagPayload(LongTag tag) throws IOException {
		os.writeLong(tag.getValue());
	}

	/**
	 * Writes a <code>TAG_Int</code> tag.
	 * @param tag The tag.
	 * @throws IOException if an I/O error occurs.
	 */
	private void writeIntTagPayload(IntTag tag) throws IOException {
		os.writeInt(tag.getValue());
	}

	/**
	 * Writes a <code>TAG_Short</code> tag.
	 * @param tag The tag.
	 * @throws IOException if an I/O error occurs.
	 */
	private void writeShortTagPayload(ShortTag tag) throws IOException {
		os.writeShort(tag.getValue());
	}

	/**
	 * Writes a <code>TAG_Empty</code> tag.
	 * @param tag The tag.
	 * @throws IOException if an I/O error occurs.
	 */
	private void writeEndTagPayload(EndTag tag) {
		/* empty */
	}

	@Override
	public void close() throws IOException {
		os.close();
	}

}
