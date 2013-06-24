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

package com.sk89q.worldedit.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.rebar.command.parametric.ExceptionConverterHelper;
import com.sk89q.rebar.command.parametric.ExceptionMatch;
import com.sk89q.worldedit.DisallowedItemException;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.InvalidFilenameException;
import com.sk89q.worldedit.InvalidItemException;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.MaxRadiusException;
import com.sk89q.worldedit.PlayerNeededException;
import com.sk89q.worldedit.UnknownDirectionException;
import com.sk89q.worldedit.UnknownItemException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldedit.commands.InsufficientArgumentsException;
import com.sk89q.worldedit.expression.ExpressionException;
import com.sk89q.worldedit.operation.RejectedOperationException;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.tools.InvalidToolBindException;

/**
 * converts WorldEdit exceptions and converts them into {@link CommandException}s.
 */
public class WorldEditExceptionConverter extends ExceptionConverterHelper {

    private static final Pattern numberFormat = Pattern
            .compile("^For input string: \"(.*)\"$");
    
    private final LocalConfiguration config;
    
    public WorldEditExceptionConverter(LocalConfiguration config) {
        this.config = config;
    }

    @ExceptionMatch
    public void convert(RejectedOperationException e) throws CommandException {
        throw new CommandException("Sorry, there are currently waiting operations " +
        		"and yours cannot be queued at the moment."); // TODO: List operations
    }

    @ExceptionMatch
    public void convert(PlayerNeededException e) throws CommandException {
        throw new CommandException(e.getMessage());
    }

    @ExceptionMatch
    public void convert(NumberFormatException e) throws CommandException {
        final Matcher matcher = numberFormat.matcher(e.getMessage());

        if (matcher.matches()) {
            throw new CommandException("Number expected; string \"" + matcher.group(1)
                    + "\" given.");
        } else {
            throw new CommandException("Number expected; string given.");
        }
    }

    @ExceptionMatch
    public void convert(IncompleteRegionException e) throws CommandException {
        throw new CommandException("Make a region selection first.");
    }

    @ExceptionMatch
    public void convert(UnknownItemException e) throws CommandException {
        throw new CommandException("Block name '" + e.getID() + "' was not recognized.");
    }

    @ExceptionMatch
    public void convert(InvalidItemException e) throws CommandException {
        throw new CommandException(e.getMessage());
    }

    @ExceptionMatch
    public void convert(DisallowedItemException e) throws CommandException {
        throw new CommandException("Block '" + e.getID()
                + "' not allowed (see WorldEdit configuration).");
    }

    @ExceptionMatch
    public void convert(MaxChangedBlocksException e) throws CommandException {
        throw new CommandException("Max blocks changed in an operation reached ("
                + e.getBlockLimit() + ").");
    }

    @ExceptionMatch
    public void convert(MaxRadiusException e) throws CommandException {
        throw new CommandException("Maximum radius: " + config.maxRadius);
    }

    @ExceptionMatch
    public void convert(UnknownDirectionException e) throws CommandException {
        throw new CommandException("Unknown direction: " + e.getDirection());
    }

    @ExceptionMatch
    public void convert(InsufficientArgumentsException e) throws CommandException {
        throw new CommandException(e.getMessage());
    }

    @ExceptionMatch
    public void convert(RegionOperationException e) throws CommandException {
        throw new CommandException(e.getMessage());
    }

    @ExceptionMatch
    public void convert(ExpressionException e) throws CommandException {
        throw new CommandException(e.getMessage());
    }

    @ExceptionMatch
    public void convert(EmptyClipboardException e) throws CommandException {
        throw new CommandException("Your clipboard is empty. Use //copy first.");
    }

    @ExceptionMatch
    public void convert(InvalidFilenameException e) throws CommandException {
        throw new CommandException("Filename '" + e.getFilename() + "' invalid: "
                + e.getMessage());
    }

    @ExceptionMatch
    public void convert(FilenameResolutionException e) throws CommandException {
        throw new CommandException(
                "File '" + e.getFilename() + "' resolution error: " + e.getMessage());
    }

    @ExceptionMatch
    public void convert(InvalidToolBindException e) throws CommandException {
        throw new CommandException("Can't bind tool to "
                + ItemType.toHeldName(e.getItemId()) + ": " + e.getMessage());
    }

    @ExceptionMatch
    public void convert(FileSelectionAbortedException e) throws CommandException {
        throw new CommandException("File selection aborted.");
    }

    @ExceptionMatch
    public void convert(WorldEditException e) throws CommandException {
        throw new CommandException(e.getMessage());
    }

}
