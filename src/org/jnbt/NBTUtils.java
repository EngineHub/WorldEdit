package org.jnbt;

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
 * A class which contains NBT-related utility methods.
 * @author Graham Edgecombe
 *
 */
public final class NBTUtils {
	
	/**
	 * Gets the type name of a tag.
	 * @param clazz The tag class.
	 * @return The type name.
	 */
	public static String getTypeName(Class<? extends Tag> clazz) {
		if(clazz.equals(ByteArrayTag.class)) {
			return "TAG_Byte_Array";
		} else if(clazz.equals(ByteTag.class)) {
			return "TAG_Byte";
		} else if(clazz.equals(CompoundTag.class)) {
			return "TAG_Compound";
		} else if(clazz.equals(DoubleTag.class)) {
			return "TAG_Double";
		} else if(clazz.equals(EndTag.class)) {
			return "TAG_End";
		} else if(clazz.equals(FloatTag.class)) {
			return "TAG_Float";
		} else if(clazz.equals(IntTag.class)) {
			return "TAG_Int";
		} else if(clazz.equals(ListTag.class)) {
			return "TAG_List";
		} else if(clazz.equals(LongTag.class)) {
			return "TAG_Long";
		} else if(clazz.equals(ShortTag.class)) {
			return "TAG_Short";
		} else if(clazz.equals(StringTag.class)) {
			return "TAG_String";
		} else {
			throw new IllegalArgumentException("Invalid tag classs (" + clazz.getName() + ").");
		}
	}
	
	/**
	 * Gets the type code of a tag class.
	 * @param clazz The tag class.
	 * @return The type code.
	 * @throws IllegalArgumentException if the tag class is invalid.
	 */
	public static int getTypeCode(Class<? extends Tag> clazz) {
		if(clazz.equals(ByteArrayTag.class)) {
			return NBTConstants.TYPE_BYTE_ARRAY;
		} else if(clazz.equals(ByteTag.class)) {
			return NBTConstants.TYPE_BYTE;
		} else if(clazz.equals(CompoundTag.class)) {
			return NBTConstants.TYPE_COMPOUND;
		} else if(clazz.equals(DoubleTag.class)) {
			return NBTConstants.TYPE_DOUBLE;
		} else if(clazz.equals(EndTag.class)) {
			return NBTConstants.TYPE_END;
		} else if(clazz.equals(FloatTag.class)) {
			return NBTConstants.TYPE_FLOAT;
		} else if(clazz.equals(IntTag.class)) {
			return NBTConstants.TYPE_INT;
		} else if(clazz.equals(ListTag.class)) {
			return NBTConstants.TYPE_LIST;
		} else if(clazz.equals(LongTag.class)) {
			return NBTConstants.TYPE_LONG;
		} else if(clazz.equals(ShortTag.class)) {
			return NBTConstants.TYPE_SHORT;
		} else if(clazz.equals(StringTag.class)) {
			return NBTConstants.TYPE_STRING;
		} else {
			throw new IllegalArgumentException("Invalid tag classs (" + clazz.getName() + ").");
		}
	}
	
	/**
	 * Gets the class of a type of tag.
	 * @param type The type.
	 * @return The class.
	 * @throws IllegalArgumentException if the tag type is invalid.
	 */
	public static Class<? extends Tag> getTypeClass(int type) {
		switch(type) {
		case NBTConstants.TYPE_END:
			return EndTag.class;
		case NBTConstants.TYPE_BYTE:
			return ByteTag.class;
		case NBTConstants.TYPE_SHORT:
			return ShortTag.class;
		case NBTConstants.TYPE_INT:
			return IntTag.class;
		case NBTConstants.TYPE_LONG:
			return LongTag.class;
		case NBTConstants.TYPE_FLOAT:
			return FloatTag.class;
		case NBTConstants.TYPE_DOUBLE:
			return DoubleTag.class;
		case NBTConstants.TYPE_BYTE_ARRAY:
			return ByteArrayTag.class;
		case NBTConstants.TYPE_STRING:
			return StringTag.class;
		case NBTConstants.TYPE_LIST:
			return ListTag.class;
		case NBTConstants.TYPE_COMPOUND:
			return CompoundTag.class;
		default:
			throw new IllegalArgumentException("Invalid tag type : " + type + ".");
		}
	}
	
	/**
	 * Default private constructor.
	 */
	private NBTUtils() {
		
	}

}
