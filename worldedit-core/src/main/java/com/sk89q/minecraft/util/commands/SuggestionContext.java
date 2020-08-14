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

package com.sk89q.minecraft.util.commands;

public class SuggestionContext {

    private static final SuggestionContext FOR_LAST = new SuggestionContext(null, true);
    private static final SuggestionContext FOR_HANGING = new SuggestionContext(null, false);
    
    private final Character flag;
    private final boolean forLast;

    private SuggestionContext(Character flag, boolean forLast) {
        this.flag = flag;
        this.forLast = forLast; 
    }
    
    public boolean forHangingValue() {
        return flag == null && !forLast;
    }
    
    public boolean forLastValue() {
        return flag == null && forLast;
    }

    public boolean forFlag() {
        return flag != null;
    }

    public Character getFlag() {
        return flag;
    }
    
    @Override
    public String toString() {
        return forFlag() ? ("-" + getFlag()) : (forHangingValue() ? "hanging" : "last");
    }
    
    public static SuggestionContext flag(Character flag) {
        return new SuggestionContext(flag, false);
    }
    
    public static SuggestionContext lastValue() {
        return FOR_LAST;
    }
    
    public static SuggestionContext hangingValue() {
        return FOR_HANGING;
    }

}
