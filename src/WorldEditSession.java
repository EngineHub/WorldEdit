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

import com.sk89q.worldedit.*;
import java.util.LinkedList;

/**
 *
 * @author sk89q
 */
public class WorldEditSession {
    public static final int MAX_HISTORY_SIZE = 15;
    private int[] pos1 = new int[3];
    private int[] pos2 = new int[3];
    private boolean hasSetPos1 = false;
    private boolean hasSetPos2 = false;
    private LinkedList<EditSession> history = new LinkedList<EditSession>();
    private int historyPointer = 0;
    private RegionClipboard clipboard;
    private boolean toolControl = true;
    private int[] lastToolPos1 = new int[3];
    private long lastToolClick = 0;
    private int maxBlocksChanged = -1;

    /**
     * Clear history.
     */
    public void clearHistory() {
        history.clear();
        historyPointer = 0;
    }

    /**
     * Get the edit session.
     *
     * @return
     */
    public void remember(EditSession editSession) {
        // Don't store anything if no changes were made
        if (editSession.size() == 0) { return; }

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
     * Undo.
     *
     * @return whether anything was undoed
     */
    public boolean undo() {
        historyPointer--;
        if (historyPointer >= 0) {
            history.get(historyPointer).undo();
            return true;
        } else {
            historyPointer = 0;
            return false;
        }
    }

    /**
     * Redo.
     *
     * @return whether anything was redoed
     */
    public boolean redo() {
        if (historyPointer < history.size()) {
            history.get(historyPointer).redo();
            historyPointer++;
            return true;
        }

        return false;
    }

    /**
     * Checks to make sure that position 1 is defined.
     * 
     * @throws IncompleteRegionException
     */
    private void checkPos1() throws IncompleteRegionException {
        if (!hasSetPos1) {
            throw new IncompleteRegionException();
        }
    }
    
    /**
     * Checks to make sure that position 2 is defined.
     *
     * @throws IncompleteRegionException
     */
    private void checkPos2() throws IncompleteRegionException {
        if (!hasSetPos2) {
            throw new IncompleteRegionException();
        }
    }

    /**
     * Gets defined position 1.
     * 
     * @return
     * @throws IncompleteRegionException
     */
    public int[] getPos1() throws IncompleteRegionException {
        checkPos1();
        return pos1;
    }

    /**
     * Sets position 1.
     *
     * @param x
     * @param y
     * @param z
     */
    public void setPos1(int x, int y, int z) {
        hasSetPos1 = true;
        pos1 = new int[]{x, y, z};
    }

    /**
     * Sets position 1.
     *
     * @param x
     * @param y
     * @param z
     */
    public void setPos1(int[] pos) {
        hasSetPos1 = true;
        pos1 = pos;
    }

    /**
     * Gets position 2.
     * 
     * @return
     * @throws IncompleteRegionException
     */
    public int[] getPos2() throws IncompleteRegionException {
        checkPos2();
        return pos2;
    }

    /**
     * Sets position 2.
     *
     * @param x
     * @param y
     * @param z
     */
    public void setPos2(int x, int y, int z) {
        hasSetPos2 = true;
        pos2 = new int[]{x, y, z};
    }

    /**
     * Sets position 2.
     *
     * @param x
     * @param y
     * @param z
     */
    public void setPos2(int[] pos) {
        hasSetPos2 = true;
        pos2 = pos;
    }

    /**
     * Get lower X bound.
     * 
     * @return
     * @throws IncompleteRegionException
     */
    public int getLowerX() throws IncompleteRegionException {
        checkPos1();
        checkPos2();
        return Math.min(pos1[0], pos2[0]);
    }

    /**
     * Get upper X bound.
     *
     * @return
     * @throws IncompleteRegionException
     */
    public int getUpperX() throws IncompleteRegionException {
        checkPos1();
        checkPos2();
        return Math.max(pos1[0], pos2[0]);
    }

    /**
     * Get lower Y bound.
     *
     * @return
     * @throws IncompleteRegionException
     */
    public int getLowerY() throws IncompleteRegionException {
        checkPos1();
        checkPos2();
        return Math.min(pos1[1], pos2[1]);
    }

    /**
     * Get upper Y bound.
     *
     * @return
     * @throws IncompleteRegionException
     */
    public int getUpperY() throws IncompleteRegionException {
        checkPos1();
        checkPos2();
        return Math.max(pos1[1], pos2[1]);
    }

    /**
     * Get lower Z bound.
     *
     * @return
     * @throws IncompleteRegionException
     */
    public int getLowerZ() throws IncompleteRegionException {
        checkPos1();
        checkPos2();
        return Math.min(pos1[2], pos2[2]);
    }

    /**
     * Get upper Z bound.
     *
     * @return
     * @throws IncompleteRegionException
     */
    public int getUpperZ() throws IncompleteRegionException {
        checkPos1();
        checkPos2();
        return Math.max(pos1[2], pos2[2]);
    }

    /**
     * Gets the size of the region as the number of blocks.
     * 
     * @return
     * @throws IncompleteRegionException
     */
    public int getSize() throws IncompleteRegionException {
        return (getUpperX() - getLowerX() + 1) *
               (getUpperY() - getLowerY() + 1) *
               (getUpperZ() - getLowerZ() + 1);
    }

    /**
     * Gets the clipboard.
     * 
     * @return
     */
    public RegionClipboard getClipboard() {
        return clipboard;
    }

    /**
     * Sets the clipboard.
     * 
     * @param clipboard
     */
    public void setClipboard(RegionClipboard clipboard) {
        this.clipboard = clipboard;
    }

    /**
     * See if tool control is enabled.
     * 
     * @return
     */
    public boolean isToolControlEnabled() {
        return toolControl;
    }

    /**
     * Change tool control setting.
     * 
     * @param
     */
    public void setToolControl(boolean toolControl) {
        this.toolControl = toolControl;
    }

    /**
     * @return the lastToolPos1
     */
    public int[] getLastToolPos1() {
        return lastToolPos1;
    }

    /**
     * @param lastToolPos1 the lastToolPos1 to set
     */
    public void setLastToolPos1(int[] lastToolPos1) {
        this.lastToolPos1 = lastToolPos1;
    }

    /**
     * Returns true if the tool has been double clicked.
     * 
     * @return
     */
    public boolean hasToolBeenDoubleClicked() {
        return System.currentTimeMillis() - lastToolClick < 500;
    }

    /**
     * Triggers a click of the tool.
     */
    public void triggerToolClick() {
        lastToolClick = System.currentTimeMillis();
    }

    /**
     * Get the maximum number of blocks that can be changed in an edit session.
     * @return
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
}
