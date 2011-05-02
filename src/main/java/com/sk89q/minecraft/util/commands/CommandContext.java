// $Id$
/*
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.minecraft.util.commands;

import java.util.HashSet;
import java.util.Set;

public class CommandContext {
    protected String[] args;
    protected Set<Character> flags = new HashSet<Character>();
    
    public CommandContext(String args) {
        this(args.split(" "));
    }
    
    public CommandContext(String[] args) {
        int i = 1;
        for (; i < args.length; i++) {
            if (args[i].length() == 0) {
                // Ignore this
            } else if (args[i].charAt(0) == '-' && args[i].matches("^-[a-zA-Z]+$")) {
                for (int k = 1; k < args[i].length(); k++) {
                    flags.add(args[i].charAt(k));
                }
            } else {
                break;
            }
        }
        
        String[] newArgs = new String[args.length - i + 1];
        
        System.arraycopy(args, i, newArgs, 1, args.length - i);
        newArgs[0] = args[0];
        
        this.args = newArgs;
    }
    
    public String getCommand() {
        return args[0];
    }
    
    public boolean matches(String command) {
        return args[0].equalsIgnoreCase(command);
    }
    
    public String getString(int index) {
        return args[index + 1];
    }
    
    public String getString(int index, String def) {
        return index + 1 < args.length ? args[index + 1] : def;
    }

    public String getJoinedStrings(int initialIndex) {
        initialIndex = initialIndex + 1;
        StringBuilder buffer = new StringBuilder(args[initialIndex]);
        for (int i = initialIndex + 1; i < args.length; i++) {
            buffer.append(" ").append(args[i]);
        }
        return buffer.toString();
    }
    
    public int getInteger(int index) throws NumberFormatException {
        return Integer.parseInt(args[index + 1]);
    }
    
    public int getInteger(int index, int def) throws NumberFormatException {
        return index + 1 < args.length ? Integer.parseInt(args[index + 1]) : def;
    }
    
    public double getDouble(int index) throws NumberFormatException {
        return Double.parseDouble(args[index + 1]);
    }
    
    public double getDouble(int index, double def) throws NumberFormatException {
        return index + 1 < args.length ? Double.parseDouble(args[index + 1]) : def;
    }
    
    public String[] getSlice(int index) {
        String[] slice = new String[args.length - index];
        System.arraycopy(args, index, slice, 0, args.length - index);
        return slice;
    }
    
    public String[] getPaddedSlice(int index, int padding) {
        String[] slice = new String[args.length - index + padding];
        System.arraycopy(args, index, slice, padding, args.length - index);
        return slice;
    }
    
    public boolean hasFlag(char ch) {
        return flags.contains(ch);
    }
    
    public Set<Character> getFlags() {
        return flags;
    }
    
    public int length() {
        return args.length;
    }
    
    public int argsLength() {
        return args.length - 1;
    }
}
