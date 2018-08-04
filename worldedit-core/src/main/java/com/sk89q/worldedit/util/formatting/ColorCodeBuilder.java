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

import com.google.common.base.Joiner;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ColorCodeBuilder {

    private static final ColorCodeBuilder instance = new ColorCodeBuilder();
    private static final Joiner newLineJoiner = Joiner.on("\n");
    public static final int GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH = 47;
    
    /**
     * Convert a message into color-coded text.
     * 
     * @param message the message
     * @return a list of lines
     */
    public String[] build(StyledFragment message) {
        StringBuilder builder = new StringBuilder();
        buildFragment(builder, message, message.getStyle(), new StyleSet());
        return builder.toString().split("\r?\n");
    }
    
    /**
     * Build a fragment.
     * 
     * @param builder the string builder
     * @param message the message
     * @param parentStyle the parent style
     * @param lastStyle the last style
     * @return the last style used
     */
    private StyleSet buildFragment(StringBuilder builder, StyledFragment message, StyleSet parentStyle, StyleSet lastStyle) {
        for (Fragment node : message.getChildren()) {
            if (node instanceof StyledFragment) {
                StyledFragment fragment = (StyledFragment) node;
                lastStyle = buildFragment(
                        builder, fragment, 
                        parentStyle.extend(message.getStyle()), lastStyle);
            } else {
                StyleSet style = parentStyle.extend(message.getStyle());
                builder.append(getAdditive(style, lastStyle));
                builder.append(node);
                lastStyle = style;
            }
        }
        
        return lastStyle;
    }
    
    /**
     * Get the formatting codes.
     * 
     * @param style the style
     * @return the color codes
     */
    public static String getFormattingCode(StyleSet style) {
        StringBuilder builder = new StringBuilder();
        if (style.isBold()) {
            builder.append(Style.BOLD);
        }
        if (style.isItalic()) {
            builder.append(Style.ITALIC);
        }
        if (style.isUnderline()) {
            builder.append(Style.UNDERLINE);
        }
        if (style.isStrikethrough()) {
            builder.append(Style.STRIKETHROUGH);
        }
        return builder.toString();
    }
    
    /**
     * Get the formatting and color codes.
     * 
     * @param style the style
     * @return the color codes
     */
    public static String getCode(StyleSet style) {
        StringBuilder builder = new StringBuilder();
        builder.append(getFormattingCode(style));
        if (style.getColor() != null) {
            builder.append(style.getColor());
        }
        return builder.toString();
    }

    /**
     * Get the additional color codes needed to set the given style when the current
     * style is the other given one.
     * 
     * @param resetTo the style to reset to
     * @param resetFrom the style to reset from
     * @return the color codes
     */
    public static String getAdditive(StyleSet resetTo, StyleSet resetFrom) {
        if (!resetFrom.hasFormatting() && resetTo.hasFormatting()) {
            StringBuilder builder = new StringBuilder();
            builder.append(getFormattingCode(resetTo));
            if (resetFrom.getColor() != resetTo.getColor()) {
                builder.append(resetTo.getColor());
            }
            return builder.toString();
        } else if (!resetFrom.hasEqualFormatting(resetTo) || 
                (resetFrom.getColor() != null && resetTo.getColor() == null)) {
            // Have to set reset code and add back all the formatting codes
            return String.valueOf(Style.RESET) + getCode(resetTo);
        } else {
            if (resetFrom.getColor() != resetTo.getColor()) {
                return String.valueOf(resetTo.getColor());
            }
        }
        
        return "";
    }

    /**
     * Word wrap the given text and maintain color codes throughout lines.
     * 
     * <p>This is borrowed from Bukkit.</p>
     * 
     * @param rawString the raw string
     * @param lineLength the maximum line length
     * @return a list of lines
     */
    private String[] wordWrap(String rawString, int lineLength) {
        // A null string is a single line
        if (rawString == null) {
            return new String[] {""};
        }

        // A string shorter than the lineWidth is a single line
        if (rawString.length() <= lineLength && !rawString.contains("\n")) {
            return new String[] {rawString};
        }

        char[] rawChars = (rawString + ' ').toCharArray(); // add a trailing space to trigger pagination
        StringBuilder word = new StringBuilder();
        StringBuilder line = new StringBuilder();
        List<String> lines = new LinkedList<>();
        int lineColorChars = 0;

        for (int i = 0; i < rawChars.length; i++) {
            char c = rawChars[i];

            // skip chat color modifiers
            if (c == Style.COLOR_CHAR) {
                word.append(Style.getByChar(rawChars[i + 1]));
                lineColorChars += 2;
                i++; // Eat the next character as we have already processed it
                continue;
            }

            if (c == ' ' || c == '\n') {
                if (line.length() == 0 && word.length() > lineLength) { // special case: extremely long word begins a line
                    String wordStr = word.toString();
                    String transformed;
                    if ((transformed = transform(wordStr)) != null) {
                        line.append(transformed);
                    } else {
                        lines.addAll(Arrays.asList(word.toString().split("(?<=\\G.{" + lineLength + "})")));
                    }
                } else if (line.length() + word.length() - lineColorChars == lineLength) { // Line exactly the correct length...newline
                    line.append(' ');
                    line.append(word);
                    lines.add(line.toString());
                    line = new StringBuilder();
                    lineColorChars = 0;
                } else if (line.length() + 1 + word.length() - lineColorChars > lineLength) { // Line too long...break the line
                    String wordStr = word.toString();
                    String transformed;
                    if (word.length() > lineLength && (transformed = transform(wordStr)) != null) {
                        if (line.length() + 1 + transformed.length() - lineColorChars > lineLength) {
                            lines.add(line.toString());
                            line = new StringBuilder(transformed);
                            lineColorChars = 0;
                        } else {
                            if (line.length() > 0) {
                                line.append(' ');
                            }
                            line.append(transformed);
                        }
                    } else {
                        for (String partialWord : wordStr.split("(?<=\\G.{" + lineLength + "})")) {
                            lines.add(line.toString());
                            line = new StringBuilder(partialWord);
                        }
                        lineColorChars = 0;
                    }
                } else {
                    if (line.length() > 0) {
                        line.append(' ');
                    }
                    line.append(word);
                }
                word = new StringBuilder();

                if (c == '\n') { // Newline forces the line to flush
                    lines.add(line.toString());
                    line = new StringBuilder();
                }
            } else {
                word.append(c);
            }
        }

        if(line.length() > 0) { // Only add the last line if there is anything to add
            lines.add(line.toString());
        }

        // Iterate over the wrapped lines, applying the last color from one line to the beginning of the next
        if (lines.get(0).isEmpty() || lines.get(0).charAt(0) != Style.COLOR_CHAR) {
            lines.set(0, Style.WHITE + lines.get(0));
        }
        for (int i = 1; i < lines.size(); i++) {
            final String pLine = lines.get(i-1);
            final String subLine = lines.get(i);

            char color = pLine.charAt(pLine.lastIndexOf(Style.COLOR_CHAR) + 1);
            if (subLine.isEmpty() || subLine.charAt(0) != Style.COLOR_CHAR) {
                lines.set(i, Style.getByChar(color) + subLine);
            }
        }

        return lines.toArray(new String[lines.size()]);
    }
    
    /**
     * Callback for transforming a word, such as a URL.
     * 
     * @param word the word
     * @return the transformed value, or null to do nothing
     */
    protected String transform(String word) {
        return null;
    }

    /**
     * Convert the given styled fragment into color codes.
     *
     * @param fragment the fragment
     * @return color codes
     */
    public static String asColorCodes(StyledFragment fragment) {
        return newLineJoiner.join(instance.build(fragment));
    }

}
