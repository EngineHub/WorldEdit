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

package com.sk89q.worldedit.internal.command.exception;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.DisallowedItemException;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.InvalidItemException;
import com.sk89q.worldedit.MaxBrushRadiusException;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.MaxRadiusException;
import com.sk89q.worldedit.MissingWorldException;
import com.sk89q.worldedit.UnknownDirectionException;
import com.sk89q.worldedit.UnknownItemException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.InsufficientArgumentsException;
import com.sk89q.worldedit.command.tool.InvalidToolBindException;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.io.file.FileSelectionAbortedException;
import com.sk89q.worldedit.util.io.file.FilenameResolutionException;
import com.sk89q.worldedit.util.io.file.InvalidFilenameException;
import org.enginehub.piston.exception.CommandException;
import org.enginehub.piston.exception.UsageException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * converts WorldEdit exceptions and converts them into {@link CommandException}s.
 */
public class WorldEditExceptionConverter extends ExceptionConverterHelper {

    private static final Pattern numberFormat = Pattern.compile("^For input string: \"(.*)\"$");
    private final WorldEdit worldEdit;

    public WorldEditExceptionConverter(WorldEdit worldEdit) {
        checkNotNull(worldEdit);
        this.worldEdit = worldEdit;
    }

    private CommandException newCommandException(String message, Throwable cause) {
        return new CommandException(TextComponent.of(String.valueOf(message)), cause, ImmutableList.of());
    }

    @ExceptionMatch
    public void convert(NumberFormatException e) throws CommandException {
        final Matcher matcher = numberFormat.matcher(e.getMessage());

        if (matcher.matches()) {
            throw newCommandException("Number expected; string \"" + matcher.group(1)
                    + "\" given.", e);
        } else {
            throw newCommandException("Number expected; string given.", e);
        }
    }

    @ExceptionMatch
    public void convert(IncompleteRegionException e) throws CommandException {
        throw newCommandException("Make a region selection first.", e);
    }

    @ExceptionMatch
    public void convert(MissingWorldException e) throws CommandException {
        throw newCommandException("You need to provide a world (Try //world)", e);
    }

    @ExceptionMatch
    public void convert(UnknownItemException e) throws CommandException {
        throw newCommandException("Block name '" + e.getID() + "' was not recognized.", e);
    }

    @ExceptionMatch
    public void convert(InvalidItemException e) throws CommandException {
        throw newCommandException(e.getMessage(), e);
    }

    @ExceptionMatch
    public void convert(DisallowedItemException e) throws CommandException {
        throw newCommandException("Block '" + e.getID()
                + "' not allowed (see WorldEdit configuration).", e);
    }

    @ExceptionMatch
    public void convert(MaxChangedBlocksException e) throws CommandException {
        throw newCommandException("Max blocks changed in an operation reached ("
                + e.getBlockLimit() + ").", e);
    }

    @ExceptionMatch
    public void convert(MaxBrushRadiusException e) throws CommandException {
        throw newCommandException("Maximum brush radius (in configuration): " + worldEdit.getConfiguration().maxBrushRadius, e);
    }

    @ExceptionMatch
    public void convert(MaxRadiusException e) throws CommandException {
        throw newCommandException("Maximum radius (in configuration): " + worldEdit.getConfiguration().maxRadius, e);
    }

    @ExceptionMatch
    public void convert(UnknownDirectionException e) throws CommandException {
        throw newCommandException("Unknown direction: " + e.getDirection(), e);
    }

    @ExceptionMatch
    public void convert(InsufficientArgumentsException e) throws CommandException {
        throw newCommandException(e.getMessage(), e);
    }

    @ExceptionMatch
    public void convert(RegionOperationException e) throws CommandException {
        throw newCommandException(e.getMessage(), e);
    }

    @ExceptionMatch
    public void convert(ExpressionException e) throws CommandException {
        throw newCommandException(e.getMessage(), e);
    }

    @ExceptionMatch
    public void convert(EmptyClipboardException e) throws CommandException {
        throw newCommandException("Your clipboard is empty. Use //copy first.", e);
    }

    @ExceptionMatch
    public void convert(InvalidFilenameException e) throws CommandException {
        throw newCommandException("Filename '" + e.getFilename() + "' invalid: "
                + e.getMessage(), e);
    }

    @ExceptionMatch
    public void convert(FilenameResolutionException e) throws CommandException {
        throw newCommandException(
                "File '" + e.getFilename() + "' resolution error: " + e.getMessage(), e);
    }

    @ExceptionMatch
    public void convert(InvalidToolBindException e) throws CommandException {
        throw newCommandException("Can't bind tool to " + e.getItemType().getName() + ": " + e.getMessage(), e);
    }

    @ExceptionMatch
    public void convert(FileSelectionAbortedException e) throws CommandException {
        throw newCommandException("File selection aborted.", e);
    }

    @ExceptionMatch
    public void convert(WorldEditException e) throws CommandException {
        throw newCommandException(e.getMessage(), e);
    }

    // Prevent investigation into UsageExceptions
    @ExceptionMatch
    public void convert(UsageException e) throws CommandException {
        throw e;
    }

}
