// $Id$
/*
 * WorldEdit
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

package com.sk89q.worldedit;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TimeZone;
import com.sk89q.jchronic.Chronic;
import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.utils.Span;
import com.sk89q.jchronic.utils.Time;
import com.sk89q.worldedit.snapshots.Snapshot;
import com.sk89q.worldedit.tools.BrushTool;
import com.sk89q.worldedit.tools.SinglePickaxe;
import com.sk89q.worldedit.tools.BlockTool;
import com.sk89q.worldedit.tools.Tool;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.cui.CUIPointBasedRegion;
import com.sk89q.worldedit.cui.CUIEvent;
import com.sk89q.worldedit.cui.SelectionShapeEvent;
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.regions.CuboidRegionSelector;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;

/**
 * An instance of this represents the WorldEdit session of a user. A session
 * stores history and settings. Sessions are not tied particularly to any
 * player and can be shuffled between players, saved, and loaded.
 *
 * @author sk89q
 */
public class LocalSession {
    public static int MAX_HISTORY_SIZE = 15;
    public static int EXPIRATION_GRACE = 600000;

    private LocalConfiguration config;

    private long expirationTime = 0;
    private LocalWorld selectionWorld;
    private RegionSelector selector = new CuboidRegionSelector();
    private boolean placeAtPos1 = false;
    private LinkedList<EditSession> history = new LinkedList<EditSession>();
    private int historyPointer = 0;
    private CuboidClipboard clipboard;
    private boolean toolControl = true;
    private boolean superPickaxe = false;
    private BlockTool pickaxeMode = new SinglePickaxe();
    private Map<Integer, Tool> tools = new HashMap<Integer, Tool>();
    private int maxBlocksChanged = -1;
    private boolean useInventory;
    private Snapshot snapshot;
    private String lastScript;
    private boolean beenToldVersion = false;
    private boolean hasCUISupport = false;
    private boolean fastMode = false;
    private Mask mask;
    private TimeZone timezone = TimeZone.getDefault();
    private Boolean jumptoBlock = true;

    /**
     * Construct the object.
     *
     * @param config
     */
    public LocalSession(LocalConfiguration config) {
        this.config = config;
    }

    /**
     * Get the session's timezone.
     *
     * @return
     */
    public TimeZone getTimeZone() {
        return timezone;
    }

    /**
     * Set the session's timezone.
     *
     * @param timezone
     */
    public void setTimezone(TimeZone timezone) {
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
     * @param editSession
     */
    public void remember(EditSession editSession) {
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
     * @param newBlockBag
     * @return whether anything was undone
     */
    public EditSession undo(BlockBag newBlockBag) {
        --historyPointer;
        if (historyPointer >= 0) {
            EditSession editSession = history.get(historyPointer);
            EditSession newEditSession =
                    new EditSession(editSession.getWorld(), -1, newBlockBag);
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
     * @param newBlockBag
     * @return whether anything was redone
     */
    public EditSession redo(BlockBag newBlockBag) {
        if (historyPointer < history.size()) {
            EditSession editSession = history.get(historyPointer);
            EditSession newEditSession =
                new EditSession(editSession.getWorld(), -1, newBlockBag);
            newEditSession.enableQueue();
            newEditSession.setFastMode(fastMode);
            editSession.redo(newEditSession);
            ++historyPointer;
            return editSession;
        }

        return null;
    }

    /**
     * Get the region selector for defining the selection. If the selection
     * was defined for a different world, the old selection will be discarded.
     *
     * @param world
     * @return position
     */
    public RegionSelector getRegionSelector(LocalWorld world) {
        if (selectionWorld == null) {
            selectionWorld = world;
        } else if (!selectionWorld.equals(world)) {
            selectionWorld = world;
            selector.clear();
        }
        return selector;
    }

    /**
     * Get the region selector. This won't check worlds so make sure that
     * this region selector isn't used blindly.
     *
     * @return position
     */
    public RegionSelector getRegionSelector() {
        return selector;
    }

    /**
     * Set the region selector.
     *
     * @param world
     * @param selector
     */
    public void setRegionSelector(LocalWorld world, RegionSelector selector) {
        selectionWorld = world;
        this.selector = selector;
    }

    /**
     * Returns true if the region is fully defined.
     *
     * @return
     */
    @Deprecated
    public boolean isRegionDefined() {
        return selector.isDefined();
    }

    /**
     * Returns true if the region is fully defined for the specified world.
     *
     * @param world
     * @return
     */
    public boolean isSelectionDefined(LocalWorld world) {
        if (selectionWorld == null || !selectionWorld.equals(world)) {
            return false;
        }
        return selector.isDefined();
    }

    /**
     * Use <code>getSelection()</code>.
     *
     * @return region
     * @throws IncompleteRegionException
     */
    @Deprecated
    public Region getRegion() throws IncompleteRegionException {
        return selector.getRegion();
    }

    /**
     * Get the selection region. If you change the region, you should
     * call learnRegionChanges().  If the selection is defined in
     * a different world, the <code>IncompleteRegionException</code>
     * exception will be thrown.
     *
     * @param world
     * @return region
     * @throws IncompleteRegionException
     */
    public Region getSelection(LocalWorld world) throws IncompleteRegionException {
        if (selectionWorld == null || !selectionWorld.equals(world)) {
            throw new IncompleteRegionException();
        }
        return selector.getRegion();
    }

    /**
     * Get the selection world.
     *
     * @return
     */
    public LocalWorld getSelectionWorld() {
        return selectionWorld;
    }

    /**
     * Gets the clipboard.
     *
     * @return clipboard, may be null
     * @throws EmptyClipboardException
     */
    public CuboidClipboard getClipboard() throws EmptyClipboardException {
        if (clipboard == null) {
            throw new EmptyClipboardException();
        }
        return clipboard;
    }

    /**
     * Sets the clipboard.
     *
     * @param clipboard
     */
    public void setClipboard(CuboidClipboard clipboard) {
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
     * @param toolControl
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
     * @param maxBlocksChanged
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
     * @return status
     */
    public boolean toggleSuperPickAxe() {
        superPickaxe = !superPickaxe;
        return superPickaxe;
    }

    /**
     * Get the placement position.
     *
     * @param player
     * @return position
     * @throws IncompleteRegionException
     */
    public Vector getPlacementPosition(LocalPlayer player)
            throws IncompleteRegionException {
        if (!placeAtPos1) {
            return player.getBlockIn();
        }

        return selector.getPrimaryPosition();
    }

    /**
     * Toggle placement position.
     *
     * @return
     */
    public boolean togglePlacementPosition() {
        placeAtPos1 = !placeAtPos1;
        return placeAtPos1;
    }

    /**
     * Get a block bag for a player.
     *
     * @param player
     * @return
     */
    public BlockBag getBlockBag(LocalPlayer player) {
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
    public Snapshot getSnapshot() {
        return snapshot;
    }

    /**
     * Select a snapshot.
     *
     * @param snapshot
     */
    public void setSnapshot(Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    /**
     * @return the superPickaxeMode
     */
    public BlockTool getSuperPickaxe() {
        return pickaxeMode;
    }

    /**
     * Set the super pickaxe tool.
     *
     * @param tool
     */
    public void setSuperPickaxe(BlockTool tool) {
        this.pickaxeMode = tool;
    }

    /**
     * Get the tool assigned to the item.
     *
     * @param item
     * @return the tool
     */
    public Tool getTool(int item) {
        return tools.get(item);
    }

    /**
     * Get the brush tool assigned to the item. If there is no tool assigned
     * or the tool is not assigned, the slot will be replaced with the
     * brush tool.
     *
     * @param item
     * @return the tool
     * @throws InvalidToolBindException
     */
    public BrushTool getBrushTool(int item) throws InvalidToolBindException {
        Tool tool = getTool(item);

        if (tool == null || !(tool instanceof BrushTool)) {
            tool = new BrushTool("worldedit.brush.sphere");
            setTool(item, tool);
        }

        return (BrushTool)tool;
    }

    /**
     * Set the tool.
     *
     * @param item
     * @param tool the tool to set
     * @throws InvalidToolBindException
     */
    public void setTool(int item, Tool tool) throws InvalidToolBindException {
        if (item > 0 && item < 255) {
            throw new InvalidToolBindException(item, "Blocks can't be used");
        } else if (item == config.wandItem) {
            throw new InvalidToolBindException(item, "Already used for the wand");
        } else if (item == config.navigationWand) {
            throw new InvalidToolBindException(item, "Already used for the navigation wand");
        }

        this.tools.put(item, tool);
    }

    /**
     * Returns whether inventory usage is enabled for this session.
     *
     * @return the useInventory
     */
    public boolean isUsingInventory() {
        return useInventory;
    }

    /**
     * Set the state of inventory usage.
     *
     * @param useInventory the useInventory to set
     */
    public void setUseInventory(boolean useInventory) {
        this.useInventory = useInventory;
    }

    /**
     * Get the last script used.
     *
     * @return the lastScript
     */
    public String getLastScript() {
        return lastScript;
    }

    /**
     * Set the last script used.
     *
     * @param lastScript the lastScript to set
     */
    public void setLastScript(String lastScript) {
        this.lastScript = lastScript;
    }

    /**
     * Tell the player the WorldEdit version.
     *
     * @param player
     */
    public void tellVersion(LocalPlayer player) {
        if (config.showFirstUseVersion) {
            if (!beenToldVersion) {
                player.printRaw("\u00A78WorldEdit ver. " + WorldEdit.getVersion()
                        + " (http://sk89q.com/projects/worldedit/)");
                beenToldVersion = true;
            }
        }
    }

    /**
     * Dispatch a CUI event but only if the player has CUI support.
     *
     * @param player
     * @param event
     */
    public void dispatchCUIEvent(LocalPlayer player, CUIEvent event) {
        if (hasCUISupport) {
            player.dispatchCUIEvent(event);
        }
    }

    /**
     * Dispatch the initial setup CUI messages.
     *
     * @param player
     */
    public void dispatchCUISetup(LocalPlayer player) {
        if (selector != null) {
            dispatchCUISelection(player);
        }
    }

    /**
     * Send the selection information.
     *
     * @param player
     */
    public void dispatchCUISelection(LocalPlayer player) {
        if (!hasCUISupport) {
            return;
        }

        player.dispatchCUIEvent(new SelectionShapeEvent(selector.getTypeId()));

        if (selector instanceof CUIPointBasedRegion) {
            ((CUIPointBasedRegion) selector).describeCUI(player);
        }
    }

    /**
     * Gets the status of CUI support.
     *
     * @return
     */
    public boolean hasCUISupport() {
        return hasCUISupport;
    }

    /**
     * Sets the status of CUI support.
     *
     * @param support
     */
    public void setCUISupport(boolean support) {
        hasCUISupport = true;
    }

    /**
     * Detect date from a user's input.
     *
     * @param input
     * @return
     */
    public Calendar detectDate(String input) {
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
     * Update the last update time for calculating expiration.
     */
    public void update() {
        expirationTime = System.currentTimeMillis();
    }

    /**
     * Returns whether this session has expired.
     *
     * @return
     */
    public boolean hasExpired() {
        return System.currentTimeMillis() - expirationTime > EXPIRATION_GRACE;
    }

    /**
     * Construct a new edit session.
     *
     * @param player
     * @return
     */
    public EditSession createEditSession(LocalPlayer player) {
        BlockBag blockBag = getBlockBag(player);

        // Create an edit session
        EditSession editSession =
                new EditSession(player.getWorld(),
                        getBlockChangeLimit(), blockBag);
        editSession.setFastMode(fastMode);
        editSession.setMask(mask);

        return editSession;
    }

    /**
     * Checks if the session has fast mode enabled.
     *
     * @return
     */
    public boolean hasFastMode() {
        return fastMode;
    }

    /**
     * Set fast mode.
     *
     * @param fastMode
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

    /**
     * This is used as a workaround for a bug.
     * It blocks the compass from using the jumpto function after the thru function
     */
    public void toggleJumptoBlock() {
        this.jumptoBlock = !jumptoBlock;
    }

    /**
     * This is used as a workaround for a bug.
     * @return true if the compass's jumpto function can be used again
     */
    public Boolean canUseJumpto() {
        return jumptoBlock;
    }
}
