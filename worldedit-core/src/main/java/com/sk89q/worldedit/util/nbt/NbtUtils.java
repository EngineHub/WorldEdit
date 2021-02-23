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

package com.sk89q.worldedit.util.nbt;

import com.sk89q.worldedit.world.storage.InvalidFormatException;

public class NbtUtils {

    /**
     * Get child tag of a NBT structure.
     *
     * @param tag the tag to read from
     * @param key the key to look for
     * @param expected the expected NBT class type
     * @return child tag
     * @throws InvalidFormatException if the format of the items is invalid
     */
    public static <T extends BinaryTag> T getChildTag(CompoundBinaryTag tag, String key, BinaryTagType<T> expected) throws InvalidFormatException {
        BinaryTag childTag = tag.get(key);
        if (childTag == null) {
            throw new InvalidFormatException("Missing a \"" + key + "\" tag");
        }

        if (childTag.type().id() != expected.id()) {
            throw new InvalidFormatException(key + " tag is not of tag type " + expected.toString());
        }
        // SAFETY: same binary tag type checked above
        @SuppressWarnings("unchecked")
        T childTagCast = (T) childTag;
        return childTagCast;
    }

    /**
     * Tests a {@link BinaryTag} against a "mask" {@link BinaryTag}.
     *
     * <p>
     * This method true if "test" contains all values from "mask". It does not
     * matter if it contains more, as long as the mask values are present.
     *
     * For list tags, compound lists are treated as unordered sets, whereas
     * non-compound lists are treated as ordered. This matches the way Minecraft
     * treats NBT in items.
     * </p>
     *
     * @param mask The mask tag
     * @param test The tested tag
     * @return If the test tag contains the values from the mask
     */
    public static boolean matches(BinaryTag mask, BinaryTag test) {
        if (mask == null) {
            // If our mask is null, all match
            return true;
        }
        if (test == null) {
            // If our mask is not null but our test is, never match
            return false;
        }

        if (mask.type() != test.type()) {
            // If the types differ, they do not match
            return false;
        }

        if (mask.type() == BinaryTagTypes.COMPOUND) {
            // For compounds, we ensure that all the keys are available and values match
            CompoundBinaryTag maskCompound = (CompoundBinaryTag) mask;
            CompoundBinaryTag testCompound = (CompoundBinaryTag) test;

            for (String binaryKey : maskCompound.keySet()) {
                if (!matches(maskCompound.get(binaryKey), testCompound.get(binaryKey))) {
                    return false;
                }
            }

            return true;
        } else if (mask.type() == BinaryTagTypes.LIST) {
            // For lists, we ensure that all the values match
            ListBinaryTag maskList = (ListBinaryTag) mask;
            ListBinaryTag testList = (ListBinaryTag) test;

            if (!maskList.elementType().equals(testList.elementType())) {
                // These lists are of different types
                return false;
            }

            if (maskList.elementType() == BinaryTagTypes.COMPOUND) {
                // Treat compound lists like a set, due to how MC handle them
                for (BinaryTag binaryTag : maskList) {
                    boolean found = false;
                    for (BinaryTag testTag : testList) {
                        if (matches(binaryTag, testTag)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        return false;
                    }
                }
            } else {
                int startIndex = 0;
                for (BinaryTag binaryTag : maskList) {
                    boolean found = false;
                    for (int i = startIndex; i < testList.size(); i++) {
                        BinaryTag testTag = testList.get(i);
                        if (matches(binaryTag, testTag)) {
                            found = true;
                            startIndex = i + 1;
                            break;
                        }
                    }
                    if (!found) {
                        return false;
                    }
                }
            }

            return true;
        } else {
            // For types that are just a value, we can do direct equality.
            return mask.equals(test);
        }
    }

}
