package com.sk89q.jnbt;

import java.nio.charset.Charset;

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
 * A class which holds constant values.
 * 
 * @author Graham Edgecombe
 * 
 */
public final class NBTConstants {

    /**
     * The character set used by NBT (UTF-8).
     */
    public static final Charset CHARSET = Charset.forName("UTF-8");

    /**
     * Tag type constants.
     */
    public static final int TYPE_END = 0, TYPE_BYTE = 1, TYPE_SHORT = 2,
            TYPE_INT = 3, TYPE_LONG = 4, TYPE_FLOAT = 5, TYPE_DOUBLE = 6,
            TYPE_BYTE_ARRAY = 7, TYPE_STRING = 8, TYPE_LIST = 9,
            TYPE_COMPOUND = 10, TYPE_INT_ARRAY = 11;

    /**
     * Default private constructor.
     */
    private NBTConstants() {

    }
    
    /**
     * Convert a type ID to its corresponding {@link Tag} class.
     * 
     * @param id type ID
     * @return tag class
     * @throws IllegalArgumentException thrown if the tag ID is not valid
     */
    public static Class<? extends Tag> getClassFromType(int id) {
        switch (id) {
        case TYPE_END:
            return EndTag.class;
        case TYPE_BYTE:
            return ByteTag.class;
        case TYPE_SHORT:
            return ShortTag.class;
        case TYPE_INT:
            return IntTag.class;
        case TYPE_LONG:
            return LongTag.class;
        case TYPE_FLOAT:
            return FloatTag.class;
        case TYPE_DOUBLE:
            return DoubleTag.class;
        case TYPE_BYTE_ARRAY:
            return ByteArrayTag.class;
        case TYPE_STRING:
            return StringTag.class;
        case TYPE_LIST:
            return ListTag.class;
        case TYPE_COMPOUND:
            return CompoundTag.class;
        case TYPE_INT_ARRAY:
            return IntArrayTag.class;
        default:
            throw new IllegalArgumentException("Unknown tag type ID of " + id);
        }
    }

}
