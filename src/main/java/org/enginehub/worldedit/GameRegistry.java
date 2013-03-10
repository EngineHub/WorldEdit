// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package org.enginehub.worldedit;

import static org.enginehub.worldedit.Localizer.*;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import org.enginehub.common.WorldObject;
import org.enginehub.event.EventSystem;
import org.enginehub.event.Request;
import org.enginehub.worldedit.event.MatchBlockRequest;
import org.enginehub.worldedit.modifier.Modifier;

import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.foundation.Block;

/**
 * Knows about blocks, items, entities, and other {@link WorldObject}s.
 */
public final class GameRegistry {
    
    /**
     * The generic pattern for parsing blocks and items.
     */
    private static final java.util.regex.Pattern pattern =
            java.util.regex.Pattern.compile("^[^:]+(?::(\\S+))\\s*(?:\\{(.*)\\})?$");

    GameRegistry() {
    }
    
    /**
     * Match the given pattern against the pattern for a custom block or item.
     * 
     * @param pattern the pattern
     * @return a matcher
     * @throws MatchNotFoundException if the format is invalid
     */
    private static Matcher match(String pattern) throws MatchNotFoundException {
        Matcher m = GameRegistry.pattern.matcher(pattern);
        
        if (!m.matches()) {
            throw new MatchNotFoundException("Invalid pattern format: " + pattern)
                    .localize(_("Sorry, the pattern '{0}' is not understood.", pattern));
        }
        
        return m;
    }
    
    /**
     * Get a list of modifiers from a pattern.
     * 
     * @param pattern the pattern
     * @return a list of modifiers
     * @throws MatchNotFoundException thrown if a match could not be found
     */
    public List<Modifier> matchModifiers(String pattern) 
            throws MatchNotFoundException {
        if (pattern.length() > 0) {
            throw new MatchNotFoundException("Modifiers are not yet supported")
                .localize(_("Sorry, modifiers are not yet supported."));
        }
        
        return Collections.emptyList();
    }
    
    /**
     * Apply a list of modifiers to a block.
     * 
     * @param modifiers the list of modifiers
     * @param block the block
     */
    public void apply(List<Modifier> modifiers, Block block) {
        // @TODO: Apply modifiers
    }
    
    /**
     * Attempt to match a {@link Block} by syntax.
     * 
     * @param pattern the pattern
     * @return the found block
     * @throws MatchNotFoundException thrown if a match could not be found
     */
    public Block matchBlock(String pattern) throws MatchNotFoundException {
        Matcher m = match(pattern);

        String rawName = m.group(1);
        String rawData = m.group(2);
        String rawModifiers = m.group(3);
        
        int typeId = -1;
        short dataValue = -1;
        MatchBlockRequest request = new MatchBlockRequest(rawName);
        
        // Parse the data value first
        try {
            if (rawData != null) {
                dataValue = Short.parseShort(rawData);
            }
        } catch (NumberFormatException e) {
            throw new MatchNotFoundException("Data value '" + rawData + "' not a number")
                .localize(_("Sorry, the part after : must be a number, but '{0}' is not.", rawData));
        }

        // Assume that the type ID is a number...
        try {
            typeId = Integer.parseInt(rawName);
        } catch (NumberFormatException e) { // Nope, not a number
            BlockType blockType = BlockType.lookup(rawName);
            
            if (blockType == null) {
                // We'll go with our match
                request.setResponseIf(new BaseBlock(typeId, dataValue), Request.LIKELY);
            }
        }

        // Now ask everyone to see if they can do better
        EventSystem.getInstance().dispatch(request);
        Block block = request.getResponse();
        
        // No block found?
        if (block == null) {
            throw new MatchNotFoundException("Unknown block name: " + rawName)
                    .localize(_("Sorry, '{0}' did not match a type of block.", rawName));
        }
        
        List<Modifier> modifiers = matchModifiers(rawModifiers);
        apply(modifiers, block);
        
        return block;
    }

}
