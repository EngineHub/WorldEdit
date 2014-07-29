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

/**
 * Represents set of styles, such as color, bold, etc.
 */
public class StyleSet {
    
    private Boolean bold;
    private Boolean italic;
    private Boolean underline;
    private Boolean strikethrough;
    private Style color;
    
    /**
     * Create a new style set with no properties set.
     */
    public StyleSet() {
    }

    /**
     * Create a new style set with the given styles.
     * 
     * <p>{@link Style#RESET} will be ignored if provided.</p>
     * 
     * @param styles a list of styles
     */
    public StyleSet(Style... styles) {
        for (Style style : styles) {
            if (style.isColor()) {
                color = style;
            } else if (style == Style.BOLD) {
                bold = true;
            } else if (style == Style.ITALIC) {
                italic = true;
            } else if (style == Style.UNDERLINE) {
                underline = true;
            } else if (style == Style.STRIKETHROUGH) {
                strikethrough = true;
            }
        }
    }
    
    /**
     * Get whether this style set is bold.
     * 
     * @return true, false, or null if unset
     */
    public Boolean getBold() {
        return bold;
    }
    
    /**
     * Get whether the text is bold.
     * 
     * @return true if bold
     */
    public boolean isBold() {
        return getBold() != null && getBold();
    }
    
    /**
     * Set whether the text is bold.
     * 
     * @param bold true, false, or null to unset
     */
    public void setBold(Boolean bold) {
        this.bold = bold;
    }

    /**
     * Get whether this style set is italicized.
     * 
     * @return true, false, or null if unset
     */
    public Boolean getItalic() {
        return italic;
    }
    
    /**
     * Get whether the text is italicized.
     * 
     * @return true if italicized
     */
    public boolean isItalic() {
        return getItalic() != null && getItalic();
    }
    
    /**
     * Set whether the text is italicized.
     * 
     * @param italic false, or null to unset
     */
    public void setItalic(Boolean italic) {
        this.italic = italic;
    }

    /**
     * Get whether this style set is underlined.
     * 
     * @return true, false, or null if unset
     */
    public Boolean getUnderline() {
        return underline;
    }
    
    /**
     * Get whether the text is underlined.
     * 
     * @return true if underlined
     */
    public boolean isUnderline() {
        return getUnderline() != null && getUnderline();
    }
    
    /**
     * Set whether the text is underline.
     * 
     * @param underline false, or null to unset
     */
    public void setUnderline(Boolean underline) {
        this.underline = underline;
    }

    /**
     * Get whether this style set is stricken through.
     * 
     * @return true, false, or null if unset
     */
    public Boolean getStrikethrough() {
        return strikethrough;
    }
    
    /**
     * Get whether the text is stricken through.
     * 
     * @return true if there is strikethrough applied
     */
    public boolean isStrikethrough() {
        return getStrikethrough() != null && getStrikethrough();
    }
    
    /**
     * Set whether the text is stricken through.
     * 
     * @param strikethrough false, or null to unset
     */
    public void setStrikethrough(Boolean strikethrough) {
        this.strikethrough = strikethrough;
    }

    /**
     * Get the color of the text.
     * 
     * @return true, false, or null if unset
     */
    public Style getColor() {
        return color;
    }
    
    /**
     * Set the color of the text.
     * 
     * @param color the color
     */
    public void setColor(Style color) {
        this.color = color;
    }
    
    /**
     * Return whether text formatting (bold, italics, underline, strikethrough) is set.
     * 
     * @return true if formatting is set
     */
    public boolean hasFormatting() {
        return getBold() != null || getItalic() != null
                || getUnderline() != null || getStrikethrough() != null;
    }

    /**
     * Return where the text formatting of the given style set is different from
     * that assigned to this one.
     * 
     * @param other the other style set
     * @return true if there is a difference
     */
    public boolean hasEqualFormatting(StyleSet other) {
        return getBold() == other.getBold() && getItalic() == other.getItalic()
                && getUnderline() == other.getUnderline() && 
                getStrikethrough() == other.getStrikethrough();
    }

    /**
     * Create a new instance with styles inherited from this one but with new styles
     * from the given style set.
     * 
     * @param style the style set
     * @return a new style set instance
     */
    public StyleSet extend(StyleSet style) {
        StyleSet newStyle = clone();
        if (style.getBold() != null) {
            newStyle.setBold(style.getBold());
        }
        if (style.getItalic() != null) {
            newStyle.setItalic(style.getItalic());
        }
        if (style.getUnderline() != null) {
            newStyle.setUnderline(style.getUnderline());
        }
        if (style.getStrikethrough() != null) {
            newStyle.setStrikethrough(style.getStrikethrough());
        }
        if (style.getColor() != null) {
            newStyle.setColor(style.getColor());
        }
        return newStyle;
    }
    
    @Override
    public StyleSet clone() {
        StyleSet style = new StyleSet();
        style.setBold(getBold());
        style.setItalic(getItalic());
        style.setUnderline(getUnderline());
        style.setStrikethrough(getStrikethrough());
        style.setColor(getColor());
        return style;
    }

}
