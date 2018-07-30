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

package com.sk89q.worldedit;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.jchronic.Chronic;
import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.utils.Span;
import com.sk89q.jchronic.utils.Time;
import com.sk89q.worldedit.command.tool.BlockTool;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.InvalidToolBindException;
import com.sk89q.worldedit.command.tool.SinglePickaxe;
import com.sk89q.worldedit.command.tool.Tool;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.internal.cui.CUIRegion;
import com.sk89q.worldedit.internal.cui.SelectionShapeEvent;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.regions.selector.RegionSelectorType;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.session.request.Request;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.snapshot.Snapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

/**
 * Stores session information.
 */
public class LocalSession {

    public transient static int MAX_HISTORY_SIZE = 15;

    // Non-session related fields
    private transient LocalConfiguration config;
    private transient final AtomicBoolean dirty = new AtomicBoolean();
    private transient int failedCuiAttempts = 0;

    // Session related
    private transient RegionSelector selector = new CuboidRegionSelector();
    private transient boolean placeAtPos1 = false;
    private transient LinkedList<EditSession> history = new LinkedList<>();
    private transient int historyPointer = 0;
    private transient ClipboardHolder clipboard;
    private transient boolean toolControl = true;
    private transient boolean superPickaxe = false;
    private transient BlockTool pickaxeMode = new SinglePickaxe();
    private transient Map<ItemType, Tool> tools = new HashMap<>();
    private transient int maxBlocksChanged = -1;
    private transient boolean useInventory;
    private transient Snapshot snapshot;
    private transient boolean hasCUISupport = false;
    private transient int cuiVersion = -1;
    private transient boolean fastMode = false;
    private transient Mask mask;
    private transient TimeZone timezone = TimeZone.getDefault();

    // Saved properties
    private String lastScript;
    private RegionSelectorType defaultSelector;

    /**
     * Construct the object.
     *
     * <p>{@link #setConfiguration(LocalConfiguration)} should be called
     * later with configuration.</p>
     */
    public LocalSession() {
    }

    /**
     * Construct the object.
     *
     * @param config the configuration
     */
    public LocalSession(@Nullable LocalConfiguration config) {
        this.config = config;
    }

    /**
     * Set the configuration.
     *
     * @param config the configuration
     */
    public void setConfiguration(LocalConfiguration config) {
        checkNotNull(config);
        this.config = config;
    }

    /**
     * Called on post load of the session from persistent storage.
     */
    public void postLoad() {
        if (defaultSelector != null) {
            this.selector = defaultSelector.createSelector();
        }
    }

    /**
     * Get whether this session is "dirty" and has changes that needs to
     * be committed.
     *
     * @return true if dirty
     */
    public boolean isDirty() {
        return dirty.get();
    }

    /**
     * Set this session as dirty.
     */
    private void setDirty() {
        dirty.set(true);
    }

    /**
     * Get whether this session is "dirty" and has changes that needs to
     * be committed, and reset it to {@code false}.
     *
     * @return true if the dirty value was {@code true}
     */
    public boolean compareAndResetDirty() {
        return dirty.compareAndSet(true, false);
    }

    /**
     * Get the session's timezone.
     *
     * @return the timezone
     */
    public TimeZone getTimeZone() {
        return timezone;
    }

    /**
     * Set the session's timezone.
     *
     * @param timezone the user's timezone
     */
    public void setTimezone(TimeZone timezone) {
        checkNotNull(timezone);
        this.timezone = timezone;
    }

    /**
     * Clear history.
     */
    public void clearHistory() {
        history.clear();
        historyPointer = 0;
    }

    /**
     * Remember an edit session for the undo history. If the history maximum
     * size is reached, old edit sessions will be discarded.
     *
     * @param editSession the edit session
     */
    public void remember(EditSession editSession) {
        checkNotNull(editSession);

        // Don't store anything if no changes were made
        if (editSession.size() == 0) return;

        // Destroy any sessions after this undo point
        while (historyPointer < history.size()) {
            history.remove(historyPointer);
        }
        history.add(editSession);
        while (history.size() > MAX_HISTORY_SIZE) {
            history.remove(0);
        }
        historyPointer = history.size();
    }

    /**
     * Performs an undo.
     *
     * @param newBlockBag a new block bag
     * @param player the player
     * @return whether anything was undone
     */
    public EditSession undo(@Nullable BlockBag newBlockBag, Player player) {
        checkNotNull(player);
        --historyPointer;
        if (historyPointer >= 0) {
            EditSession editSession = history.get(historyPointer);
            EditSession newEditSession = WorldEdit.getInstance().getEditSessionFactory()
                    .getEditSession(editSession.getWorld(), -1, newBlockBag, player);
            newEditSession.enableQueue();
            newEditSession.setFastMode(fastMode);
            editSession.undo(newEditSession);
            return editSession;
        } else {
            historyPointer = 0;
            return null;
        }
    }

    /**
     * Performs a redo
     *
     * @param newBlockBag a new block bag
     * @param player the player
     * @return whether anything was redone
     */
    public EditSession redo(@Nullable BlockBag newBlockBag, Player player) {
        checkNotNull(player);
        if (historyPointer < history.size()) {
            EditSession editSession = history.get(historyPointer);
            EditSession newEditSession = WorldEdit.getInstance().getEditSessionFactory()
                    .getEditSession(editSession.getWorld(), -1, newBlockBag, player);
            newEditSession.enableQueue();
            newEditSession.setFastMode(fastMode);
            editSession.redo(newEditSession);
            ++historyPointer;
            return editSession;
        }

        return null;
    }

    /**
     * Get the default region selector.
     *
     * @return the default region selector
     */
    public RegionSelectorType getDefaultRegionSelector() {
        return defaultSelector;
    }

    /**
     * Set the default region selector.
     *
     * @param defaultSelector the default region selector
     */
    public void setDefaultRegionSelector(RegionSelectorType defaultSelector) {
        checkNotNull(defaultSelector);
        this.defaultSelector = defaultSelector;
        setDirty();
    }

    /**
     * Get the region selector for defining the selection. If the selection
     * was defined for a different world, the old selection will be discarded.
     *
     * @param world the world
     * @return position the position
     */
    public RegionSelector getRegionSelector(World world) {
        checkNotNull(world);
        if (selector.getWorld() == null || !selector.getWorld().equals(world)) {
            selector.setWorld(world);
            selector.clear();
        }
        return selector;
    }

    /**
     * Set the region selector.
     *
     * @param world the world
     * @param selector the selector
     */
    public void setRegionSelector(World world, RegionSelector selector) {
        checkNotNull(world);
        checkNotNull(selector);
        selector.setWorld(world);
        this.selector = selector;
    }

    /**
     * Returns true if the region is fully defined for the specified world.
     *
     * @param world the world
     * @return true if a region selection is defined
     */
    public boolean isSelectionDefined(World world) {
        checkNotNull(world);
        if (selector.getIncompleteRegion().getWorld() == null || !selector.getIncompleteRegion().getWorld().equals(world)) {
            return false;
        }
        return selector.isDefined();
    }

    /**
     * Get the selection region. If you change the region, you should
     * call learnRegionChanges().  If the selection is defined in
     * a different world, the {@code IncompleteRegionException}
     * exception will be thrown.
     *
     * @param world the world
     * @return a region
     * @throws IncompleteRegionException if no region is selected
     */
    public Region getSelection(World world) throws IncompleteRegionException {
        checkNotNull(world);
        if (selector.getIncompleteRegion().getWorld() == null || !selector.getIncompleteRegion().getWorld().equals(world)) {
            throw new IncompleteRegionException();
        }
        return selector.getRegion();
    }

    /**
     * Get the selection world.
     *
     * @return the the world of the selection
     */
    public World getSelectionWorld() {
        return selector.getIncompleteRegion().getWorld();
    }

    /**
     * Gets the clipboard.
     *
     * @return clipboard
     * @throws EmptyClipboardException thrown if no clipboard is set
     */
    public ClipboardHolder getClipboard() throws EmptyClipboardException {
        if (clipboard == null) {
            throw new EmptyClipboardException();
        }
        return clipboard;
    }

    /**
     * Sets the clipboard.
     *
     * <p>Pass {@code null} to clear the clipboard.</p>
     *
     * @param clipboard the clipboard, or null if the clipboard is to be cleared
     */
    public void setClipboard(@Nullable ClipboardHolder clipboard) {
        this.clipboard = clipboard;
    }

    /**
     * See if tool control is enabled.
     *
     * @return true if enabled
     */
    public boolean isToolControlEnabled() {
        return toolControl;
    }

    /**
     * Change tool control setting.
     *
     * @param toolControl true to enable tool control
     */
    public void setToolControl(boolean toolControl) {
        this.toolControl = toolControl;
    }

    /**
     * Get the maximum number of blocks that can be changed in an edit session.
     *
     * @return block change limit
     */
    public int getBlockChangeLimit() {
        return maxBlocksChanged;
    }

    /**
     * Set the maximum number of blocks that can be changed.
     *
     * @param maxBlocksChanged the maximum number of blocks changed
     */
    public void setBlockChangeLimit(int maxBlocksChanged) {
        this.maxBlocksChanged = maxBlocksChanged;
    }

    /**
     * Checks whether the super pick axe is enabled.
     *
     * @return status
     */
    public boolean hasSuperPickAxe() {
        return superPickaxe;
    }

    /**
     * Enable super pick axe.
     */
    public void enableSuperPickAxe() {
        superPickaxe = true;
    }

    /**
     * Disable super pick axe.
     */
    public void disableSuperPickAxe() {
        superPickaxe = false;
    }

    /**
     * Toggle the super pick axe.
     *
     * @return whether the super pick axe is now enabled
     */
    public boolean toggleSuperPickAxe() {
        superPickaxe = !superPickaxe;
        return superPickaxe;
    }

    /**
     * Get the position use for commands that take a center point
     * (i.e. //forestgen, etc.).
     *
     * @param player the player
     * @return the position to use
     * @throws IncompleteRegionException thrown if a region is not fully selected
     */
    public Vector getPlacementPosition(Player player) throws IncompleteRegionException {
        checkNotNull(player);
        if (!placeAtPos1) {
            return player.getBlockIn().toVector();
        }

        return selector.getPrimaryPosition();
    }

    /**
     * Toggle placement position.
     *
     * @return whether "place at position 1" is now enabled
     */
    public boolean togglePlacementPosition() {
        placeAtPos1 = !placeAtPos1;
        return placeAtPos1;
    }

    /**
     * Get a block bag for a player.
     *
     * @param player the player to get the block bag for
     * @return a block bag
     */
    @Nullable
    public BlockBag getBlockBag(Player player) {
        checkNotNull(player);
        if (!useInventory) {
            return null;
        }
        return player.getInventoryBlockBag();
    }

    /**
     * Get the snapshot that has been selected.
     *
     * @return the snapshot
     */
    @Nullable
    public Snapshot getSnapshot() {
        return snapshot;
    }

    /**
     * Select a snapshot.
     *
     * @param snapshot a snapshot
     */
    public void setSnapshot(@Nullable Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    /**
     * Get the assigned block tool.
     *
     * @return the super pickaxe tool mode
     */
    public BlockTool getSuperPickaxe() {
        return pickaxeMode;
    }

    /**
     * Set the super pick axe tool.
     *
     * @param tool the tool to set
     */
    public void setSuperPickaxe(BlockTool tool) {
        checkNotNull(tool);
        this.pickaxeMode = tool;
    }

    /**
     * Get the tool assigned to the item.
     *
     * @param item the item type
     * @return the tool, which may be {@link null}
     */
    @Nullable
    public Tool getTool(ItemType item) {
        return tools.get(item);
    }

    /**
     * Get the brush tool assigned to the item. If there is no tool assigned
     * or the tool is not assigned, the slot will be replaced with the
     * brush tool.
     *
     * @param item the item type
     * @return the tool, or {@code null}
     * @throws InvalidToolBindException if the item can't be bound to that item
     */
    public BrushTool getBrushTool(ItemType item) throws InvalidToolBindException {
        Tool tool = getTool(item);

        if (!(tool instanceof BrushTool)) {
            tool = new BrushTool("worldedit.brush.sphere");
            setTool(item, tool);
        }

        return (BrushTool) tool;
    }

    /**
     * Set the tool.
     *
     * @param item the item type
     * @param tool the tool to set, which can be {@code null}
     * @throws InvalidToolBindException if the item can't be bound to that item
     */
    public void setTool(ItemType item, @Nullable Tool tool) throws InvalidToolBindException {
        if (item.hasBlockType()) {
            throw new InvalidToolBindException(item, "Blocks can't be used");
        } else if (item == ItemTypes.get(config.wandItem)) {
            throw new InvalidToolBindException(item, "Already used for the wand");
        } else if (item == ItemTypes.get(config.navigationWand)) {
            throw new InvalidToolBindException(item, "Already used for the navigation wand");
        }

        this.tools.put(item, tool);
    }

    /**
     * Returns whether inventory usage is enabled for this session.
     *
     * @return if inventory is being used
     */
    public boolean isUsingInventory() {
        return useInventory;
    }

    /**
     * Set the state of inventory usage.
     *
     * @param useInventory if inventory is to be used
     */
    public void setUseInventory(boolean useInventory) {
        this.useInventory = useInventory;
    }

    /**
     * Get the last script used.
     *
     * @return the last script's name
     */
    @Nullable
    public String getLastScript() {
        return lastScript;
    }

    /**
     * Set the last script used.
     *
     * @param lastScript the last script's name
     */
    public void setLastScript(@Nullable String lastScript) {
        this.lastScript = lastScript;
        setDirty();
    }

    /**
     * Tell the player the WorldEdit version.
     *
     * @param player the player
     */
    public void tellVersion(Actor player) {
    }

    /**
     * Dispatch a CUI event but only if the actor has CUI support.
     *
     * @param actor the actor
     * @param event the event
     */
    public void dispatchCUIEvent(Actor actor, CUIEvent event) {
        checkNotNull(actor);
        checkNotNull(event);

        if (hasCUISupport) {
            actor.dispatchCUIEvent(event);
        }
    }

    /**
     * Dispatch the initial setup CUI messages.
     *
     * @param actor the actor
     */
    public void dispatchCUISetup(Actor actor) {
        if (selector != null) {
            dispatchCUISelection(actor);
        }
    }

    /**
     * Send the selection information.
     *
     * @param actor the actor
     */
    public void dispatchCUISelection(Actor actor) {
        checkNotNull(actor);

        if (!hasCUISupport) {
            return;
        }

        if (selector instanceof CUIRegion) {
            CUIRegion tempSel = (CUIRegion) selector;

            if (tempSel.getProtocolVersion() > cuiVersion) {
                actor.dispatchCUIEvent(new SelectionShapeEvent(tempSel.getLegacyTypeID()));
                tempSel.describeLegacyCUI(this, actor);
            } else {
                actor.dispatchCUIEvent(new SelectionShapeEvent(tempSel.getTypeID()));
                tempSel.describeCUI(this, actor);
            }

        }
    }

    /**
     * Describe the selection to the CUI actor.
     *
     * @param actor the actor
     */
    public void describeCUI(Actor actor) {
        checkNotNull(actor);

        if (!hasCUISupport) {
            return;
        }

        if (selector instanceof CUIRegion) {
            CUIRegion tempSel = (CUIRegion) selector;

            if (tempSel.getProtocolVersion() > cuiVersion) {
                tempSel.describeLegacyCUI(this, actor);
            } else {
                tempSel.describeCUI(this, actor);
            }

        }
    }

    /**
     * Handle a CUI initialization message.
     *
     * @param text the message
     */
    public void handleCUIInitializationMessage(String text) {
        checkNotNull(text);
        if (this.failedCuiAttempts > 3) {
            return;
        }

        String[] split = text.split("\\|", 2);
        if (split.length > 1 && split[0].equalsIgnoreCase("v")) { // enough fields and right message
            if (split[1].length() > 4) {
                this.failedCuiAttempts ++;
                return;
            }
            setCUISupport(true);
            try {
                setCUIVersion(Integer.parseInt(split[1]));
            } catch (NumberFormatException e) {
                WorldEdit.logger.warning("Error while reading CUI init message: " + e.getMessage());
                this.failedCuiAttempts ++;
            }
        }
    }

    /**
     * Gets the status of CUI support.
     *
     * @return true if CUI is enabled
     */
    public boolean hasCUISupport() {
        return hasCUISupport;
    }

    /**
     * Sets the status of CUI support.
     *
     * @param support true if CUI is enabled
     */
    public void setCUISupport(boolean support) {
        hasCUISupport = support;
    }

    /**
     * Gets the client's CUI protocol version
     *
     * @return the CUI version
     */
    public int getCUIVersion() {
        return cuiVersion;
    }

    /**
     * Sets the client's CUI protocol version
     *
     * @param cuiVersion the CUI version
     */
    public void setCUIVersion(int cuiVersion) {
        this.cuiVersion = cuiVersion;
    }

    /**
     * Detect date from a user's input.
     *
     * @param input the input to parse
     * @return a date
     */
    @Nullable
    public Calendar detectDate(String input) {
        checkNotNull(input);

        Time.setTimeZone(getTimeZone());
        Options opt = new com.sk89q.jchronic.Options();
        opt.setNow(Calendar.getInstance(getTimeZone()));
        Span date = Chronic.parse(input, opt);
        if (date == null) {
            return null;
        } else {
            return date.getBeginCalendar();
        }
    }

    /**
     * Construct a new edit session.
     *
     * @param player the player
     * @return an edit session
     */
    public EditSession createEditSession(Player player) {
        checkNotNull(player);

        BlockBag blockBag = getBlockBag(player);

        // Create an edit session
        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory()
                .getEditSession(player.isPlayer() ? player.getWorld() : null,
                        getBlockChangeLimit(), blockBag, player);
        editSession.setFastMode(fastMode);
        Request.request().setEditSession(editSession);
        editSession.setMask(mask);

        return editSession;
    }

    /**
     * Checks if the session has fast mode enabled.
     *
     * @return true if fast mode is enabled
     */
    public boolean hasFastMode() {
        return fastMode;
    }

    /**
     * Set fast mode.
     *
     * @param fastMode true if fast mode is enabled
     */
    public void setFastMode(boolean fastMode) {
        this.fastMode = fastMode;
    }

    /**
     * Get the mask.
     *
     * @return mask, may be null
     */
    public Mask getMask() {
        return mask;
    }

    /**
     * Set a mask.
     *
     * @param mask mask or null
     */
    public void setMask(Mask mask) {
        this.mask = mask;
    }

}
