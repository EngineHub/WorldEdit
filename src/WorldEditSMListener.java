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

/**
 * Event listener for Hey0's server mod plugin.
 *
 * @author sk89q
 */
public class WorldEditSMListener extends PluginListener {
    /**
     * Stores a reference to the WorldEdit object.
     */
    private WorldEdit worldEdit;

    /**
     * Construct the listener with a reference to the WorldEdit object.
     *
     * @param worldEdit
     */
    public WorldEditSMListener(WorldEdit worldEdit) {
        this.worldEdit = worldEdit;
    }

    /**
     *
     * @param player
     */
    @Override
    public void onDisconnect(Player player) {
        worldEdit.removeSession(new WorldEditPlayer(player));
    }

    /**
     * Called on right click.
     *
     * @param modPlayer
     * @param blockPlaced
     * @param blockClicked
     * @param itemInHand
     * @return false if you want the action to go through
     */
    @Override
    public boolean onBlockCreate(Player modPlayer, Block blockPlaced,
            Block blockClicked, int itemInHand) {
        WorldEditPlayer player = new WorldEditPlayer(modPlayer);
        
        if (itemInHand != 271) { return false; }
        if (!modPlayer.canUseCommand("/editpos2")) { return false; }

        WorldEditSession session = worldEdit.getSession(player);

        if (session.isToolControlEnabled()) {
            Vector cur = Vector.toBlockPoint(blockClicked.getX(),
                                           blockClicked.getY(),
                                           blockClicked.getZ());

            session.setPos2(cur);
            player.print("Second edit position set.");

            return true;
        }

        return false;
    }

    /**
     * Called on left click.
     *
     * @param modPlayer
     * @param blockClicked
     * @param itemInHand
     * @return false if you want the action to go through
     */
    @Override
    public boolean onBlockDestroy(Player modPlayer, Block blockClicked) {
        WorldEditPlayer player = new WorldEditPlayer(modPlayer);
        
        if (player.getItemInHand() != 271) { return false; }
        if (!modPlayer.canUseCommand("/editpos1")) { return false; }

        WorldEditSession session = worldEdit.getSession(player);

        if (session.isToolControlEnabled()) {
            Vector cur = Vector.toBlockPoint(blockClicked.getX(),
                                           blockClicked.getY(),
                                           blockClicked.getZ());

            try {
                if (session.getPos1().equals(cur)) {
                    return false;
                }
            } catch (IncompleteRegionException e) {
            }

            session.setPos1(cur);
            player.print("First edit position set.");

            return true;
        }

        return false;
    }

    /**
     *
     * @param modPlayer
     * @param split
     * @return whether the command was processed
     */
    @Override
    public boolean onCommand(Player modPlayer, String[] split) {
        try {
            if (worldEdit.getCommands().containsKey(split[0])) {
                if (modPlayer.canUseCommand(split[0])) {
                    WorldEditPlayer player = new WorldEditPlayer(modPlayer);
                    WorldEditSession session = worldEdit.getSession(player);
                    EditSession editSession =
                            new EditSession(session.getBlockChangeLimit());
                    editSession.enableQueue();

                    try {
                        return worldEdit.performCommand(player, session, editSession, split);
                    } finally {
                        session.remember(editSession);
                        editSession.flushQueue();
                    }
                }
            }

            return false;
        } catch (NumberFormatException e) {
            modPlayer.sendMessage(Colors.Rose + "Number expected; string given.");
        } catch (IncompleteRegionException e2) {
            modPlayer.sendMessage(Colors.Rose + "The edit region has not been fully defined.");
        } catch (UnknownItemException e3) {
            modPlayer.sendMessage(Colors.Rose + "Unknown item.");
        } catch (DisallowedItemException e4) {
            modPlayer.sendMessage(Colors.Rose + "Disallowed item.");
        } catch (MaxChangedBlocksException e5) {
            modPlayer.sendMessage(Colors.Rose + "The maximum number of blocks changed ("
                    + e5.getBlockLimit() + ") in an instance was reached.");
        } catch (InsufficientArgumentsException e6) {
            modPlayer.sendMessage(Colors.Rose + e6.getMessage());
        } catch (WorldEditException e7) {
            modPlayer.sendMessage(Colors.Rose + e7.getMessage());
        }

        return true;
    }
}