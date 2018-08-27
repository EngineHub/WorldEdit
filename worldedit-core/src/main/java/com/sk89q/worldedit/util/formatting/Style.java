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

package com.sk89q.worldedit.util.formatting;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * All supported color values for chat.
 * 
 * <p>From Bukkit.</p>
 */
public enum Style {
    /**
     * Represents black
     */
    BLACK('0', 0x00),
    /**
     * Represents dark blue
     */
    BLUE_DARK('1', 0x1),
    /**
     * Represents dark green
     */
    GREEN_DARK('2', 0x2),
    /**
     * Represents dark blue (aqua)
     */
    CYAN_DARK('3', 0x3),
    /**
     * Represents dark red
     */
    RED_DARK('4', 0x4),
    /**
     * Represents dark purple
     */
    PURPLE_DARK('5', 0x5),
    /**
     * Represents gold
     */
    YELLOW_DARK('6', 0x6),
    /**
     * Represents gray
     */
    GRAY('7', 0x7),
    /**
     * Represents dark gray
     */
    GRAY_DARK('8', 0x8),
    /**
     * Represents blue
     */
    BLUE('9', 0x9),
    /**
     * Represents green
     */
    GREEN('a', 0xA),
    /**
     * Represents aqua
     */
    CYAN('b', 0xB),
    /**
     * Represents red
     */
    RED('c', 0xC),
    /**
     * Represents light purple
     */
    PURPLE('d', 0xD),
    /**
     * Represents yellow
     */
    YELLOW('e', 0xE),
    /**
     * Represents white
     */
    WHITE('f', 0xF),
    /**
     * Represents magical characters that change around randomly
     */
    RANDOMIZE('k', 0x10, true),
    /**
     * Makes the text bold.
     */
    BOLD('l', 0x11, true),
    /**
     * Makes a line appear through the text.
     */
    STRIKETHROUGH('m', 0x12, true),
    /**
     * Makes the text appear underlined.
     */
    UNDERLINE('n', 0x13, true),
    /**
     * Makes the text italic.
     */
    ITALIC('o', 0x14, true),
    /**
     * Resets all previous chat colors or formats.
     */
    RESET('r', 0x15);

    /**
     * The special character which prefixes all chat color codes. Use this if you need to dynamically
     * convert color codes from your custom format.
     */
    public static final char COLOR_CHAR = '\u00A7';
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + COLOR_CHAR + "[0-9A-FK-OR]");

    private final int intCode;
    private final char code;
    private final boolean isFormat;
    private final String toString;
    private final static Map<Integer, Style> BY_ID = Maps.newHashMap();
    private final static Map<Character, Style> BY_CHAR = Maps.newHashMap();

    Style(char code, int intCode) {
        this(code, intCode, false);
    }

    Style(char code, int intCode, boolean isFormat) {
        this.code = code;
        this.intCode = intCode;
        this.isFormat = isFormat;
        this.toString = new String(new char[] {COLOR_CHAR, code});
    }

    /**
     * Gets the char value associated with this color
     *
     * @return A char value of this color code
     */
    public char getChar() {
        return code;
    }

    @Override
    public String toString() {
        return toString;
    }

    /**
     * Checks if this code is a format code as opposed to a color code.
     * 
     * @return the if the code is a formatting code
     */
    public boolean isFormat() {
        return isFormat;
    }

    /**
     * Checks if this code is a color code as opposed to a format code.
     * 
     * @return the if the code is a color
     */
    public boolean isColor() {
        return !isFormat && this != RESET;
    }

    /**
     * Gets the color represented by the specified color code
     *
     * @param code Code to check
     * @return Associative Style with the given code, or null if it doesn't exist
     */
    public static Style getByChar(char code) {
        return BY_CHAR.get(code);
    }

    /**
     * Gets the color represented by the specified color code
     *
     * @param code Code to check
     * @return Associative Style with the given code, or null if it doesn't exist
     */
    public static Style getByChar(String code) {
        checkNotNull(code);
        checkArgument(!code.isEmpty(), "Code must have at least one character");

        return BY_CHAR.get(code.charAt(0));
    }

    /**
     * Strips the given message of all color codes
     *
     * @param input String to strip of color
     * @return A copy of the input string, without any coloring
     */
    public static String stripColor(final String input) {
        if (input == null) {
            return null;
        }

        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    /**
     * Translates a string using an alternate color code character into a string that uses the internal
     * ChatColor.COLOR_CODE color code character. The alternate color code character will only be replaced
     * if it is immediately followed by 0-9, A-F, a-f, K-O, k-o, R or r.
     *
     * @param altColorChar The alternate color code character to replace. Ex: &
     * @param textToTranslate Text containing the alternate color code character.
     * @return Text containing the ChatColor.COLOR_CODE color code character.
     */
    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i+1]) > -1) {
                b[i] = Style.COLOR_CHAR;
                b[i+1] = Character.toLowerCase(b[i+1]);
            }
        }
        return new String(b);
    }

    /**
     * Gets the ChatColors used at the end of the given input string.
     *
     * @param input Input string to retrieve the colors from.
     * @return Any remaining ChatColors to pass onto the next line.
     */
    public static String getLastColors(String input) {
        String result = "";
        int length = input.length();

        // Search backwards from the end as it is faster
        for (int index = length - 1; index > -1; index--) {
            char section = input.charAt(index);
            if (section == COLOR_CHAR && index < length - 1) {
                char c = input.charAt(index + 1);
                Style color = getByChar(c);

                if (color != null) {
                    result = color + result;

                    // Once we find a color or reset we can stop searching
                    if (color.isColor() || color == RESET) {
                        break;
                    }
                }
            }
        }

        return result;
    }

    static {
        for (Style color : values()) {
            BY_ID.put(color.intCode, color);
            BY_CHAR.put(color.code, color);
        }
    }
}
