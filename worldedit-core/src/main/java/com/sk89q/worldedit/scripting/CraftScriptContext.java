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

package com.sk89q.worldedit.scripting;

import com.sk89q.worldedit.DisallowedItemException;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.UnknownItemException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.InsufficientArgumentsException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.util.io.file.FilenameException;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * The context given to scripts.
 */
public class CraftScriptContext extends CraftScriptEnvironment {

    private List<EditSession> editSessions = new ArrayList<>();
    private String[] args;

    public CraftScriptContext(WorldEdit controller,
            Platform server, LocalConfiguration config,
            LocalSession session, Player player, String[] args) {
        super(controller, server, config, session, player);
        this.args = args;
    }

    /**
     * Get an edit session. Every subsequent call returns a new edit session.
     * Usually you only need to use one edit session.
     * 
     * @return an edit session
     */
    public EditSession remember() {
        EditSession editSession = controller.getEditSessionFactory()
                .getEditSession(player.getWorld(),
                        session.getBlockChangeLimit(), session.getBlockBag(player), player);
        editSession.enableQueue();
        editSessions.add(editSession);
        return editSession;
    }

    /**
     * Get the player.
     * 
     * @return the calling player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the player's session.
     * 
     * @return a session
     */
    public LocalSession getSession() {
        return session;
    }

    /**
     * Get the configuration for WorldEdit.
     * 
     * @return the configuration
     */
    public LocalConfiguration getConfiguration() {
        return config;
    }

    /**
     * Get a list of edit sessions that have been created.
     * 
     * @return a list of created {@code EditSession}s
     */
    public List<EditSession> getEditSessions() {
        return Collections.unmodifiableList(editSessions);
    }

    /**
     * Print a regular message to the user.
     * 
     * @param message a message
     */
    public void print(String message) {
        player.print(message);
    }

    /**
     * Print an error message to the user.
     * 
     * @param message a message
     */
    public void error(String message) {
        player.printError(message);
    }

    /**
     * Print an raw message to the user.
     * 
     * @param message a message
     */
    public void printRaw(String message) {
        player.printRaw(message);
    }

    /**
     * Checks to make sure that there are enough but not too many arguments.
     *
     * @param min a number of arguments
     * @param max -1 for no maximum
     * @param usage usage string
     * @throws InsufficientArgumentsException
     */
    public void checkArgs(int min, int max, String usage)
            throws InsufficientArgumentsException {
        if (args.length <= min || (max != -1 && args.length - 1 > max)) {
            throw new InsufficientArgumentsException("Usage: " + usage);
        }
    }

    /**
     * Get an item from an item name or an item ID number.
     *
     * @param input input to parse
     * @param allAllowed true to ignore blacklists
     * @return a block
     * @throws UnknownItemException
     * @throws DisallowedItemException
     */
    public BlockStateHolder getBlock(String input, boolean allAllowed) throws WorldEditException {
        ParserContext context = new ParserContext();
        context.setActor(player);
        context.setWorld(player.getWorld());
        context.setSession(session);
        context.setRestricted(!allAllowed);
        context.setPreferringWildcard(false);

        return controller.getBlockFactory().parseFromListInput(input, context).stream().findFirst().orElse(null);
    }

    /**
     * Get a block.
     *
     * @param id the type Id
     * @return a block
     * @throws UnknownItemException
     * @throws DisallowedItemException
     */
    public BlockStateHolder getBlock(String id) throws WorldEditException {
        return getBlock(id, false);
    }

    /**
     * Get a list of blocks as a set. This returns a Pattern.
     *
     * @param list the input
     * @return pattern
     * @throws UnknownItemException 
     * @throws DisallowedItemException 
     */
    public Pattern getBlockPattern(String list) throws WorldEditException {
        ParserContext context = new ParserContext();
        context.setActor(player);
        context.setWorld(player.getWorld());
        context.setSession(session);
        return controller.getPatternFactory().parseFromInput(list, context);
    }

    /**
     * Get a list of blocks as a set.
     *
     * @param list a list
     * @param allBlocksAllowed true if all blocks are allowed
     * @return set
     * @throws UnknownItemException 
     * @throws DisallowedItemException 
     */
    public Set<BlockStateHolder> getBlocks(String list, boolean allBlocksAllowed) throws WorldEditException {
        ParserContext context = new ParserContext();
        context.setActor(player);
        context.setWorld(player.getWorld());
        context.setSession(session);
        context.setRestricted(!allBlocksAllowed);
        return controller.getBlockFactory().parseFromListInput(list, context);
    }

    /**
     * Gets the path to a file for opening. This method will check to see if the
     * filename has valid characters and has an extension. It also prevents
     * directory traversal exploits by checking the root directory and the file
     * directory. On success, a {@code java.io.File} object will be
     * returned.
     * 
     * <p>Use this method if you need to read a file from a directory.</p>
     * 
     * @param folder sub-directory to look in
     * @param filename filename (user-submitted)
     * @param defaultExt default extension to append if there is none
     * @param exts list of extensions for file open dialog, null for no filter
     * @return a file
     * @throws FilenameException 
     */
    public File getSafeOpenFile(String folder, String filename, String defaultExt, String... exts) throws FilenameException {
        File dir = controller.getWorkingDirectoryFile(folder);
        return controller.getSafeOpenFile(player, dir, filename, defaultExt, exts);
    }

    /**
     * Gets the path to a file for saving. This method will check to see if the
     * filename has valid characters and has an extension. It also prevents
     * directory traversal exploits by checking the root directory and the file
     * directory. On success, a {@code java.io.File} object will be
     * returned.
     * 
     * <p>Use this method if you need to read a file from a directory.</p>
     * 
     * @param folder sub-directory to look in
     * @param filename filename (user-submitted)
     * @param defaultExt default extension to append if there is none
     * @param exts list of extensions for file save dialog, null for no filter
     * @return a file
     * @throws FilenameException 
     */
    public File getSafeSaveFile(String folder, String filename, String defaultExt, String... exts) throws FilenameException {
        File dir = controller.getWorkingDirectoryFile(folder);
        return controller.getSafeSaveFile(player, dir, filename, defaultExt, exts);
    }

}
