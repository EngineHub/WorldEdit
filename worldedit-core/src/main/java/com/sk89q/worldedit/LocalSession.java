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

package com.sk89q.worldedit;

import com.sk89q.jchronic.Chronic;
import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.utils.Span;
import com.sk89q.jchronic.utils.Time;
import com.sk89q.worldedit.command.tool.BlockTool;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.InvalidToolBindException;
import com.sk89q.worldedit.command.tool.NavigationWand;
import com.sk89q.worldedit.command.tool.SelectionWand;
import com.sk89q.worldedit.command.tool.SinglePickaxe;
import com.sk89q.worldedit.command.tool.Tool;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Locatable;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.internal.cui.CUIRegion;
import com.sk89q.worldedit.internal.cui.SelectionShapeEvent;
import com.sk89q.worldedit.internal.cui.ServerCUIHandler;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.regions.selector.RegionSelectorType;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.session.Placement;
import com.sk89q.worldedit.session.PlacementType;
import com.sk89q.worldedit.session.request.Request;
import com.sk89q.worldedit.util.Countable;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.snapshot.experimental.Snapshot;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinTagType;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Stores session information.
 */
public class LocalSession {

    private static final transient int CUI_VERSION_UNINITIALIZED = -1;
    public static transient int MAX_HISTORY_SIZE = 15;

    // Non-session related fields
    private transient LocalConfiguration config;
    private final transient AtomicBoolean dirty = new AtomicBoolean();

    // Single-connection lifetime fields
    private transient int failedCuiAttempts = 0;
    private transient boolean hasCUISupport = false;
    private transient int cuiVersion = CUI_VERSION_UNINITIALIZED;

    // Session related
    private transient RegionSelector selector = new CuboidRegionSelector();
    private transient Placement placement = new Placement(PlacementType.PLAYER, BlockVector3.ZERO);
    private final transient LinkedList<EditSession> history = new LinkedList<>();
    private transient int historyPointer = 0;
    private transient ClipboardHolder clipboard;
    private transient boolean superPickaxe = false;
    private transient BlockTool pickaxeMode = new SinglePickaxe();
    private final transient Map<ItemType, Tool> tools = new HashMap<>();
    private transient int maxBlocksChanged = -1;
    private transient int maxTimeoutTime;
    private transient boolean useInventory;
    private transient com.sk89q.worldedit.world.snapshot.Snapshot snapshot;
    private transient Snapshot snapshotExperimental;
    private transient SideEffectSet sideEffectSet = SideEffectSet.defaults();
    private transient Mask mask;
    private transient ZoneId timezone = ZoneId.systemDefault();
    private transient BlockVector3 cuiTemporaryBlock;
    @SuppressWarnings("deprecation")
    private transient EditSession.ReorderMode reorderMode = EditSession.ReorderMode.FAST;
    private transient List<Countable<BlockState>> lastDistribution;
    private transient World worldOverride;
    private transient boolean tickingWatchdog = true;
    private transient boolean hasBeenToldVersion;
    private transient boolean tracingActions;

    // Saved properties
    private String lastScript;
    private RegionSelectorType defaultSelector;
    private boolean useServerCUI = false; // Save this to not annoy players.
    private String wandItem;
    private Boolean wandItemDefault;
    private String navWandItem;
    private Boolean navWandItemDefault;

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
    public ZoneId getTimeZone() {
        return timezone;
    }

    /**
     * Set the session's timezone.
     *
     * @param timezone the user's timezone
     */
    public void setTimezone(ZoneId timezone) {
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
        if (editSession.size() == 0) {
            return;
        }

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
     * @param actor the actor
     * @return whether anything was undone
     */
    public EditSession undo(@Nullable BlockBag newBlockBag, Actor actor) {
        checkNotNull(actor);
        --historyPointer;
        if (historyPointer >= 0) {
            EditSession editSession = history.get(historyPointer);
            try (EditSession newEditSession =
                     WorldEdit.getInstance().newEditSessionBuilder()
                         .world(editSession.getWorld()).blockBag(newBlockBag).actor(actor)
                         .build()) {
                prepareEditingExtents(newEditSession, actor);
                editSession.undo(newEditSession);
            }
            return editSession;
        } else {
            historyPointer = 0;
            return null;
        }
    }

    /**
     * Performs a redo.
     *
     * @param newBlockBag a new block bag
     * @param actor the actor
     * @return whether anything was redone
     */
    public EditSession redo(@Nullable BlockBag newBlockBag, Actor actor) {
        checkNotNull(actor);
        if (historyPointer < history.size()) {
            EditSession editSession = history.get(historyPointer);
            try (EditSession newEditSession =
                     WorldEdit.getInstance().newEditSessionBuilder()
                         .world(editSession.getWorld()).blockBag(newBlockBag).actor(actor)
                         .build()) {
                prepareEditingExtents(newEditSession, actor);
                editSession.redo(newEditSession);
            }
            ++historyPointer;
            return editSession;
        }

        return null;
    }

    public boolean hasWorldOverride() {
        return this.worldOverride != null;
    }

    @Nullable
    public World getWorldOverride() {
        return this.worldOverride;
    }

    public void setWorldOverride(@Nullable World worldOverride) {
        this.worldOverride = worldOverride;
    }

    public boolean isTickingWatchdog() {
        return tickingWatchdog;
    }

    public void setTickingWatchdog(boolean tickingWatchdog) {
        this.tickingWatchdog = tickingWatchdog;
    }

    public boolean isTracingActions() {
        return tracingActions;
    }

    public void setTracingActions(boolean tracingActions) {
        this.tracingActions = tracingActions;
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
            if (hasWorldOverride() && !world.equals(getWorldOverride())) {
                setWorldOverride(null);
            }
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
        if (hasWorldOverride() && !world.equals(getWorldOverride())) {
            setWorldOverride(null);
        }
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
     * call learnRegionChanges(). If the selection is not fully defined,
     * the {@code IncompleteRegionException} exception will be thrown.
     *
     * <p>Note that this method will return a region in the current selection world,
     * which is not guaranteed to be the player's world or even the current world
     * override. If you require a specific world, use the
     * {@link LocalSession#getSelection(World)} overload instead.
     *
     * @return the selected region
     * @throws IncompleteRegionException if the region is not fully defined
     */
    public Region getSelection() throws IncompleteRegionException {
        return getSelection(getSelectionWorld());
    }

    /**
     * Get the selection region. If you change the region, you should
     * call learnRegionChanges(). If the selection is defined in
     * a different world, or the selection isn't fully defined,
     * the {@code IncompleteRegionException} exception will be thrown.
     *
     * @param world the world
     * @return a region
     * @throws IncompleteRegionException if no region is selected, or the provided world is null
     */
    public Region getSelection(@Nullable World world) throws IncompleteRegionException {
        if (world == null || selector.getIncompleteRegion().getWorld() == null
            || !selector.getIncompleteRegion().getWorld().equals(world)) {
            throw new IncompleteRegionException();
        }
        return selector.getRegion();
    }

    /**
     * Get the selection world.
     *
     * @return the the world of the selection
     */
    @Nullable
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
     * Check if tool control is enabled.
     *
     * @return true always - see deprecation notice
     * @deprecated The wand is now a tool that can be bound/unbound.
     */
    @Deprecated
    public boolean isToolControlEnabled() {
        return true;
    }

    /**
     * Set if tool control is enabled.
     *
     * @param toolControl unused - see deprecation notice
     * @deprecated The wand is now a tool that can be bound/unbound.
     */
    @Deprecated
    public void setToolControl(boolean toolControl) {
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
     * Get the maximum time allowed for certain executions to run before cancelling them, such as expressions.
     *
     * @return timeout time, in milliseconds
     */
    public int getTimeout() {
        return maxTimeoutTime;
    }

    /**
     * Set the maximum number of blocks that can be changed.
     *
     * @param timeout the time, in milliseconds, to limit certain executions to, or -1 to disable
     */
    public void setTimeout(int timeout) {
        this.maxTimeoutTime = timeout;
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
     * @param actor the actor
     * @return the position to use
     * @throws IncompleteRegionException thrown if a region is not fully selected
     */
    public BlockVector3 getPlacementPosition(Actor actor) throws IncompleteRegionException {
        return this.placement.getPlacementPosition(selector, actor);
    }

    /**
     * Returns the current placement used for many operations, like <code>//sphere</code>.
     *
     * @return the placement.
     */
    public Placement getPlacement() {
        return this.placement;
    }

    /**
     * Sets the current placement used for many operations, like <code>//sphere</code>.
     * <p>
     * Example usage: <code>session.setPlacement(new Placement(PlacementType.WORLD, BlockVector3.at(123, 456, 789)</code>
     * </p>
     *
     * @param placement the placement.
     */
    public void setPlacement(Placement placement) {
        if (placement.placementType() == PlacementType.HERE) {
            throw new IllegalStateException("PlacementType.HERE cannot be used. Use PLAYER or WORLD instead.");
        }

        this.placement = placement;
    }

    /**
     * Sets whether placement is at POS1 or PLAYER.
     *
     * @param placeAtPos1 true=POS1, false=PLAYER
     *
     * @deprecated Use {@link #setPlacement(Placement)}
     */
    @Deprecated
    public void setPlaceAtPos1(boolean placeAtPos1) {
        this.placement = new Placement(placeAtPos1 ? PlacementType.POS1 : PlacementType.PLAYER, BlockVector3.ZERO);
    }

    /**
     * Gets whether placement is at POS1 or PLAYER.
     *
     * @return true=POS1, false=PLAYER
     *
     * @deprecated Use {@link #getPlacement()}
     */
    @Deprecated
    public boolean isPlaceAtPos1() {
        return this.placement.placementType() == PlacementType.POS1;
    }

    /**
     * Toggle placement position between POS1 and PLAYER.
     * If placement was POS1, it is set to PLAYER.
     * If it was set to anything else, it is set to POS1.
     *
     * @return whether placement is now at position 1
     * @deprecated Use {@link #setPlacement(Placement)}
     */
    @Deprecated
    public boolean togglePlacementPosition() {
        if (this.placement.placementType() == PlacementType.POS1) {
            this.placement = new Placement(PlacementType.PLAYER, BlockVector3.ZERO);
        } else {
            this.placement = new Placement(PlacementType.POS1, BlockVector3.ZERO);
        }
        return this.placement.placementType() == PlacementType.POS1;
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
     * Get the legacy snapshot that has been selected.
     *
     * @return the legacy snapshot
     */
    @Nullable
    public com.sk89q.worldedit.world.snapshot.Snapshot getSnapshot() {
        return snapshot;
    }

    /**
     * Select a legacy snapshot.
     *
     * @param snapshot a legacy snapshot
     */
    public void setSnapshot(@Nullable com.sk89q.worldedit.world.snapshot.Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    /**
     * Get the snapshot that has been selected.
     *
     * @return the snapshot
     */
    public @Nullable Snapshot getSnapshotExperimental() {
        return snapshotExperimental;
    }

    /**
     * Select a snapshot.
     *
     * @param snapshotExperimental a snapshot
     */
    public void setSnapshotExperimental(@Nullable Snapshot snapshotExperimental) {
        this.snapshotExperimental = snapshotExperimental;
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
     * @return the tool, which may be {@code null}
     */
    @Nullable
    public Tool getTool(ItemType item) {
        return tools.get(item);
    }

    /**
     * Get the brush tool assigned to the item. If there is no tool assigned
     * or the tool is not a brush tool, the slot will be replaced with the
     * brush tool.
     *
     * @param item the item type
     * @return the tool, or {@code null}
     * @throws InvalidToolBindException if the item can't be bound to that item
     * @deprecated Use {@link #getBrush(ItemType)} or {@link #forceBrush(ItemType, Brush, String)}
     *     if you need to bind a specific brush
     */
    @Deprecated
    public BrushTool getBrushTool(ItemType item) throws InvalidToolBindException {
        Tool tool = getTool(item);

        if (!(tool instanceof BrushTool)) {
            tool = new BrushTool("worldedit.brush.sphere");
            setTool(item, tool);
        }

        return (BrushTool) tool;
    }

    /**
     * Get the brush tool assigned to this item.
     *
     * @param item the item type
     * @return the brush tool assigned to the item type
     */
    @Nullable
    public BrushTool getBrush(ItemType item) {
        return getTool(item) instanceof BrushTool tool ? tool : null;
    }

    /**
     * Force the tool to become a brush tool with the specified brush and permission.
     *
     * @param item the item type
     * @param brush the brush to bind
     * @param permission the permission to check before use is allowed
     * @return the brush tool assigned to the item type
     */
    public BrushTool forceBrush(ItemType item, Brush brush, String permission) throws InvalidToolBindException {
        BrushTool tool = getBrush(item);
        if (tool == null) {
            tool = new BrushTool(brush, permission);
            setTool(item, tool);
        } else {
            tool.setBrush(brush, permission);
        }
        return tool;
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
            throw new InvalidToolBindException(item, TranslatableComponent.of("worldedit.tool.error.item-only"));
        }
        if (tool instanceof SelectionWand) {
            setSingleItemTool(id -> {
                this.wandItem = id;
                this.wandItemDefault = id.equals(config.wandItem);
            }, this.wandItem, item);
        } else if (tool instanceof NavigationWand) {
            setSingleItemTool(id -> {
                this.navWandItem = id;
                this.navWandItemDefault = id.equals(config.navigationWand);
            }, this.navWandItem, item);
        } else if (tool == null) {
            // Check if un-setting sel/nav
            String id = item.id();
            if (id.equals(this.wandItem)) {
                this.wandItem = null;
                setDirty();
            } else if (id.equals(this.navWandItem)) {
                this.navWandItem = null;
                setDirty();
            }
        }

        this.tools.put(item, tool);
    }

    private void setSingleItemTool(Consumer<String> setter, @Nullable String itemId, ItemType newItem) {
        if (itemId != null) {
            ItemType item = ItemTypes.get(itemId);
            if (item != null) {
                this.tools.remove(item);
            }
        }
        setter.accept(newItem.id());
        setDirty();
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
     * @param actor the actor
     */
    public void tellVersion(Actor actor) {
        if (hasBeenToldVersion) {
            return;
        }
        hasBeenToldVersion = true;
        actor.sendAnnouncements();
    }

    public boolean shouldUseServerCUI() {
        return this.useServerCUI;
    }

    public void setUseServerCUI(boolean useServerCUI) {
        this.useServerCUI = useServerCUI;
        setDirty();
    }

    /**
     * Update server-side WorldEdit CUI.
     *
     * @param actor The player
     */
    public void updateServerCUI(Actor actor) {
        if (!actor.isPlayer()) {
            return; // This is for players only.
        }

        if (!config.serverSideCUI) {
            return; // Disabled in config.
        }

        Player player = (Player) actor;

        if (!useServerCUI || hasCUISupport) {
            if (cuiTemporaryBlock != null) {
                player.sendFakeBlock(cuiTemporaryBlock, null);
                cuiTemporaryBlock = null;
            }
            return; // If it's not enabled, ignore this.
        }

        BaseBlock block = ServerCUIHandler.createStructureBlock(player);
        if (block != null) {
            LinCompoundTag tags = Objects.requireNonNull(
                block.getNbt(), "createStructureBlock should return nbt"
            );
            BlockVector3 tempCuiTemporaryBlock = BlockVector3.at(
                tags.getTag("x", LinTagType.intTag()).valueAsInt(),
                tags.getTag("y", LinTagType.intTag()).valueAsInt(),
                tags.getTag("z", LinTagType.intTag()).valueAsInt()
            );
            // If it's null, we don't need to do anything. The old was already removed.
            if (cuiTemporaryBlock != null && !tempCuiTemporaryBlock.equals(cuiTemporaryBlock)) {
                // Update the existing block if it's the same location
                player.sendFakeBlock(cuiTemporaryBlock, null);
            }
            cuiTemporaryBlock = tempCuiTemporaryBlock;
            player.sendFakeBlock(cuiTemporaryBlock, block);
        } else if (cuiTemporaryBlock != null) {
            // Remove the old block
            player.sendFakeBlock(cuiTemporaryBlock, null);
            cuiTemporaryBlock = null;
        }
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
        } else if (useServerCUI) {
            updateServerCUI(actor);
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
            if (useServerCUI) {
                updateServerCUI(actor);
            }
            return;
        }

        if (selector instanceof CUIRegion tempSel) {
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

        if (selector instanceof CUIRegion tempSel) {
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
     * @param eventType the CUI event type
     * @param args the CUI packet arguments
     * @param actor the actor representing the sender
     */
    public void handleCUIInitializationMessage(String eventType, List<String> args, Actor actor) {
        checkNotNull(eventType);
        checkNotNull(args);

        if (this.hasCUISupport) {
            // WECUI is a bit aggressive about re-initializing itself
            // the last attempt to touch handshakes didn't go well, so this will do... for now
            dispatchCUISelection(actor);
            return;
        } else if (this.failedCuiAttempts > 3) {
            return;
        }

        if (!args.isEmpty() && eventType.equalsIgnoreCase("v")) { // enough fields and right message
            if (args.size() > 1) {
                this.failedCuiAttempts++;
                return;
            }

            int version;
            try {
                version = Integer.parseInt(args.getFirst());
            } catch (NumberFormatException e) {
                WorldEdit.logger.warn("Error while reading CUI init message");
                this.failedCuiAttempts++;
                return;
            }
            setCUISupport(true);
            setCUIVersion(version);
            dispatchCUISelection(actor);
        }
    }

    /**
     * Handle a CUI initialization message.
     *
     * @param text the message
     * @deprecated Use {@link #handleCUIInitializationMessage(String, List, Actor)}
     */
    @Deprecated
    public void handleCUIInitializationMessage(String text, Actor actor) {
        checkNotNull(text);

        String[] split = text.split("\\|");
        List<String> args = split.length > 1 ? new ArrayList<>(Arrays.asList(Arrays.copyOfRange(split, 1, split.length))) : List.of();

        handleCUIInitializationMessage(split[0], args, actor);
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
     * Gets the client's CUI protocol version.
     *
     * @return the CUI version
     */
    public int getCUIVersion() {
        return cuiVersion;
    }

    /**
     * Sets the client's CUI protocol version.
     *
     * @param cuiVersion the CUI version
     */
    public void setCUIVersion(int cuiVersion) {
        if (cuiVersion < 0) {
            throw new IllegalArgumentException("CUI protocol version must be non-negative, but '" + cuiVersion + "' was received.");
        }

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

        TimeZone tz = TimeZone.getTimeZone(getTimeZone());
        Time.setTimeZone(tz);
        Options opt = new com.sk89q.jchronic.Options();
        opt.setNow(Calendar.getInstance(tz));
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
     * @param actor the actor
     * @return an edit session
     */
    public EditSession createEditSession(Actor actor) {
        checkNotNull(actor);

        World world = null;
        if (hasWorldOverride()) {
            world = getWorldOverride();
        } else if (actor instanceof Locatable && ((Locatable) actor).getExtent() instanceof World) {
            world = (World) ((Locatable) actor).getExtent();
        }

        // Create an edit session
        EditSessionBuilder builder = WorldEdit.getInstance().newEditSessionBuilder()
            .world(world)
            .actor(actor)
            .maxBlocks(getBlockChangeLimit())
            .tracing(isTracingActions());
        if (actor.isPlayer() && actor instanceof Player) {
            builder.blockBag(getBlockBag((Player) actor));
        }
        EditSession editSession = builder.build();
        Request.request().setEditSession(editSession);

        editSession.setMask(mask);
        prepareEditingExtents(editSession, actor);

        return editSession;
    }

    @SuppressWarnings("deprecation")
    private void prepareEditingExtents(EditSession editSession, Actor actor) {
        editSession.setSideEffectApplier(sideEffectSet);
        editSession.setReorderMode(reorderMode);
        if (editSession.getSurvivalExtent() != null) {
            editSession.getSurvivalExtent().setStripNbt(!actor.hasPermission("worldedit.setnbt"));
        }
        editSession.setTickingWatchdog(tickingWatchdog);
    }

    /**
     * Gets the side effect applier of this session.
     *
     * @return the side effect applier
     */
    public SideEffectSet getSideEffectSet() {
        return this.sideEffectSet;
    }

    /**
     * Sets the side effect applier for this session.
     *
     * @param sideEffectSet the side effect applier
     */
    public void setSideEffectSet(SideEffectSet sideEffectSet) {
        this.sideEffectSet = sideEffectSet;
    }

    /**
     * Checks if the session has fast mode enabled.
     *
     * @return true if fast mode is enabled
     */
    @Deprecated
    public boolean hasFastMode() {
        return !this.sideEffectSet.doesApplyAny();
    }

    /**
     * Set fast mode.
     *
     * @param fastMode true if fast mode is enabled
     */
    @Deprecated
    public void setFastMode(boolean fastMode) {
        this.sideEffectSet = fastMode ? SideEffectSet.none() : SideEffectSet.defaults();
    }

    /**
     * Gets the reorder mode of the session.
     *
     * @return The reorder mode
     */
    @Deprecated
    public EditSession.ReorderMode getReorderMode() {
        return reorderMode;
    }

    /**
     * Sets the reorder mode of the session.
     *
     * @param reorderMode The reorder mode
     */
    @Deprecated
    public void setReorderMode(EditSession.ReorderMode reorderMode) {
        this.reorderMode = reorderMode;
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
     * Get the preferred wand item for this user, or {@code null} to use the default.
     * @return item id of wand item, or {@code null}
     */
    public String getWandItem() {
        return wandItem;
    }

    /**
     * Get if the selection wand item should use the default, or null if unknown.
     *
     * @return if it should use the default
     */
    public boolean isWandItemDefault() {
        if (wandItemDefault == null) {
            wandItemDefault = Objects.equals(wandItem, config.wandItem);
            setDirty();
        }
        return wandItemDefault;
    }

    /**
     * Get the preferred navigation wand item for this user, or {@code null} to use the default.
     * @return item id of nav wand item, or {@code null}
     */
    public String getNavWandItem() {
        return navWandItem;
    }

    /**
     * Get if the navigation wand item should use the default, or null if unknown.
     *
     * @return if it should use the default
     */
    public boolean isNavWandItemDefault() {
        if (navWandItemDefault == null) {
            navWandItemDefault = Objects.equals(navWandItem, config.navigationWand);
            setDirty();
        }
        return navWandItemDefault;
    }

    /**
     * Get the last block distribution stored in this session.
     *
     * @return block distribution or {@code null}
     */
    public List<Countable<BlockState>> getLastDistribution() {
        return lastDistribution == null ? null : Collections.unmodifiableList(lastDistribution);
    }

    /**
     * Store a block distribution in this session.
     */
    public void setLastDistribution(List<Countable<BlockState>> dist) {
        lastDistribution = dist;
    }

    /**
     * Call when this session has become inactive.
     *
     * <p>This is for internal use only.</p>
     */
    public void onIdle() {
        this.cuiVersion = CUI_VERSION_UNINITIALIZED;
        this.hasCUISupport = false;
        this.failedCuiAttempts = 0;
    }
}
