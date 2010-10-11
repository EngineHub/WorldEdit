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

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.io.*;
import org.mozilla.javascript.*;
import com.sk89q.worldedit.*;

/**
 * Plugin entry point for Hey0's mod.
 *
 * @author sk89q
 */
public class WorldEdit extends Plugin {
    private final static String DEFAULT_ALLOWED_BLOCKS =
        "0,1,2,3,4,5,7,8,9,10,11,12,13,14,15,16,17,18,19,20,35,41,42,43," +
        "44,45,47,48,49,52,53,54,56,57,58,60,61,62,67,73,78,79,80,81,82,85";

    private static final Logger logger = Logger.getLogger("Minecraft");
    private HashMap<WorldEditPlayer,WorldEditSession> sessions =
            new HashMap<WorldEditPlayer,WorldEditSession>();
    private HashMap<String,String> commands = new HashMap<String,String>();

    private PropertiesFile properties;
    private String[] allowedBlocks;
    private int defaultMaxBlocksChanged;
    private boolean mapScriptCommands = false;

    /**
     * Construct an instance of the plugin.
     */
    public WorldEdit() {
        super();

        commands.put("/editpos1", "Set editing position #1");
        commands.put("/editpos2", "Set editing position #2");
        commands.put("/editwand", "Gives you the \"edit wand\"");
        commands.put("/editundo", "Undo");
        commands.put("/editredo", "Redo");
        commands.put("/clearhistory", "Clear history");
        commands.put("/clearclipboard", "Clear clipboard");
        commands.put("/editsize", "Get size of selected region");
        commands.put("/editset", "[ID] - Set all blocks inside region");
        commands.put("/editoutline", "[ID] - Outline the region with blocks");
        commands.put("/editreplace", "[ID] <ToReplaceID> - Replace all existing blocks inside region");
        commands.put("/editoverlay", "[ID] - Overlay the area one layer");
        commands.put("/removeabove", "<Size> <Height> - Remove blocks above head");
        commands.put("/removebelow", "<Size> <Height> - Remove blocks below position");
        commands.put("/editcopy", "Copies the currently selected region");
        commands.put("/editpaste", "Pastes the clipboard");
        commands.put("/editpasteair", "Pastes the clipboard (with air)");
        commands.put("/editstack", "[Dir] <Count> - Stacks the clipboard");
        commands.put("/editstackair", "[Dir] <Count> - Stacks the clipboard (with air)");
        commands.put("/editload", "[Filename] - Load .schematic into clipboard");
        commands.put("/editsave", "[Filename] - Save clipboard to .schematic");
        commands.put("/editfill", "[ID] [Radius] <Depth> - Fill a hole");
        commands.put("/editscript", "[Filename] <Args...> - Run a WorldEdit script");
        commands.put("/editlimit", "[Num] - See documentation");
        commands.put("/unstuck", "Go up to the first free spot");
    }

    /**
     * Gets the WorldEdit session for a player.
     *
     * @param player
     * @return
     */
    private WorldEditSession getSession(WorldEditPlayer player) {
        if (sessions.containsKey(player)) {
            return sessions.get(player);
        } else {
            WorldEditSession session = new WorldEditSession();
            session.setBlockChangeLimit(defaultMaxBlocksChanged);
            sessions.put(player, session);
            return session;
        }
    }

    /**
     * Get an item ID from an item name or an item ID number.
     *
     * @param id
     * @return
     * @throws UnknownItemException
     * @throws DisallowedItemException
     */
    private int getItem(String id, boolean allAllowed)
            throws UnknownItemException, DisallowedItemException {
        int foundID;

        try {
            foundID = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            try {
                foundID = etc.getDataSource().getItem(id);
            } catch (NumberFormatException e2) {
                throw new UnknownItemException();
            }
        }

        // All items allowed
        if (allAllowed || allowedBlocks[0].equals("")) {
            return foundID;
        }

        for (String s : allowedBlocks) {
            if (s.equals(String.valueOf(foundID))) {
                return foundID;
            }
        }

        throw new DisallowedItemException();
    }

    /**
     * Get an item ID from an item name or an item ID number.
     *
     * @param id
     * @return
     * @throws UnknownItemException
     * @throws DisallowedItemException
     */
    private int getItem(String id) throws UnknownItemException,
                                          DisallowedItemException {
        return getItem(id, false);
    }

    /**
     *
     * @param player
     */
    @Override
    public void onDisconnect(Player player) {
        sessions.remove(player.getName());
    }

    /**
     * Checks to make sure that there are enough but not too many arguments.
     *
     * @param args
     * @param min
     * @param max -1 for no maximum
     * @param cmd command name
     * @throws InsufficientArgumentsException
     */
    private void checkArgs(String[] args, int min, int max, String cmd)
            throws InsufficientArgumentsException {
        if (args.length <= min || (max != -1 && args.length - 1 > max)) {
            if (commands.containsKey(cmd)) {
                throw new InsufficientArgumentsException(cmd + " usage: " +
                        commands.get(cmd));
            } else {
                throw new InsufficientArgumentsException("Invalid number of arguments");
            }
        }
    }

    /**
     * Enables the plugin.
     */
    public void enable() {
        if (properties == null) {
            properties = new PropertiesFile("worldedit.properties");
        } else {
            properties.load();
        }

        allowedBlocks = properties.getString("allowed-blocks", DEFAULT_ALLOWED_BLOCKS).split(",");
        mapScriptCommands = properties.getBoolean("map-script-commands", true);
        defaultMaxBlocksChanged = 
                Math.max(-1, properties.getInt("max-blocks-changed", -1));

        etc controller = etc.getInstance();

        for (Map.Entry<String,String> entry : commands.entrySet()) {
            controller.addCommand(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Disables the plugin.
     */
    public void disable() {
        etc controller = etc.getInstance();

        for (String key : commands.keySet()) {
            controller.removeCommand(key);
        }

        sessions.clear();
    }

    /**
     * Called on right click.
     *
     * @param player
     * @param blockPlaced
     * @param blockClicked
     * @param itemInHand
     * @return false if you want the action to go through
     */
    @Override
    public boolean onBlockCreate(Player modPlayer, Block blockPlaced,
            Block blockClicked, int itemInHand) {
        if (itemInHand == 271) { // Wooden axe
            if (!modPlayer.canUseCommand("/editpos1")
                    || !modPlayer.canUseCommand("/editpos2")) {
                return false;
            }

            WorldEditPlayer player = new WorldEditPlayer(modPlayer);
            WorldEditSession session = getSession(player);

            if (session.isToolControlEnabled()) {
                Point cur = Point.toBlockPoint(blockClicked.getX(),
                                               blockClicked.getY(),
                                               blockClicked.getZ());
                
                try {
                    if (session.hasToolBeenDoubleClicked()
                            && cur.equals(session.getPos1())) { // Pos 2
                        session.setPos2(cur);
                        session.setPos1(session.getLastToolPos1());
                        player.print("Second edit position set; first one restored.");
                    } else {
                        // Have to remember the original position because on
                        // double click, we are going to restore it
                        try {
                            session.setLastToolPos1(session.getPos1());
                        } catch (IncompleteRegionException e) {}
                        
                        session.setPos1(cur);
                        player.print("First edit position set.");
                    }
                } catch (IncompleteRegionException e) {}

                session.triggerToolClick();

                return true;
            }
        }

        return false;
    }

    /**
     * 
     * @param player
     * @param split
     * @return
     */
    @Override
    public boolean onCommand(Player modPlayer, String[] split) {
        try {            
            if (commands.containsKey(split[0])) {
                if (modPlayer.canUseCommand(split[0])) {
                    WorldEditPlayer player = new WorldEditPlayer(modPlayer);
                    WorldEditSession session = getSession(player);
                    EditSession editSession =
                            new EditSession(session.getBlockChangeLimit());
                    editSession.enableQueue();
                    
                    try {
                        return performCommand(player, session, editSession, split);
                    } finally {
                        session.remember(editSession);
                        editSession.flushQueue();
                    }
                }
            } else {
                // See if there is a script by the same name
                if (mapScriptCommands && modPlayer.canUseCommand("/editscript")) {
                    WorldEditPlayer player = new WorldEditPlayer(modPlayer);
                    WorldEditSession session = getSession(player);
                    EditSession editSession =
                            new EditSession(session.getBlockChangeLimit());
                    editSession.enableQueue();

                    String filename = split[0].substring(1) + ".js";
                    String[] args = new String[split.length - 1];
                    System.arraycopy(split, 1, args, 0, split.length - 1);

                    try {
                        return runScript(player, session, editSession, filename, args);
                    } catch (NoSuchScriptException nse) {
                        return false;
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

    /**
     * The main meat of command processing.
     * 
     * @param player
     * @param editPlayer
     * @param session
     * @param editSession
     * @param split
     * @return
     * @throws UnknownItemException
     * @throws IncompleteRegionException
     * @throws InsufficientArgumentsException
     * @throws DisallowedItemException
     */
    private boolean performCommand(WorldEditPlayer player,
            WorldEditSession session, EditSession editSession, String[] split)
            throws WorldEditException
    {
        // Jump to the first free position
        if (split[0].equalsIgnoreCase("/unstuck")) {
            checkArgs(split, 0, 0, split[0]);
            player.print("There you go!");
            player.findFreePosition();
            return true;

        // Set edit position #1
        } else if (split[0].equalsIgnoreCase("/editpos1")) {
            checkArgs(split, 0, 0, split[0]);
            session.setPos1(player.getBlockIn());
            player.print("First edit position set.");
            return true;

        // Set edit position #2
        } else if (split[0].equalsIgnoreCase("/editpos2")) {
            checkArgs(split, 0, 0, split[0]);
            session.setPos2(player.getBlockIn());
            player.print("Second edit position set.");
            return true;

        // Edit wand
        } else if (split[0].equalsIgnoreCase("/editwand")) {
            checkArgs(split, 0, 0, split[0]);
            player.giveItem(271, 1);
            player.print("Right click = sel. pos 1; double right click = sel. pos 2");
            return true;

        // Set max number of blocks to change at a time
        } else if (split[0].equalsIgnoreCase("/editlimit")) {
            checkArgs(split, 1, 1, split[0]);
            int limit = Math.max(-1, Integer.parseInt(split[1]));
            session.setBlockChangeLimit(limit);
            player.print("Block change limit set to " + limit + ".");
            return true;

        // Undo
        } else if (split[0].equalsIgnoreCase("/editundo")) {
            checkArgs(split, 0, 0, split[0]);
            if (session.undo()) {
                player.print("Undo successful.");
            } else {
                player.printError("Nothing to undo.");
            }
            return true;

        // Redo
        } else if (split[0].equalsIgnoreCase("/editredo")) {
            checkArgs(split, 0, 0, split[0]);
            if (session.redo()) {
                player.print("Redo successful.");
            } else {
                player.printError("Nothing to redo.");
            }
            return true;

        // Clear undo history
        } else if (split[0].equalsIgnoreCase("/clearhistory")) {
            checkArgs(split, 0, 0, split[0]);
            session.clearHistory();
            player.print("History cleared.");
            return true;

        // Clear clipboard
        } else if (split[0].equalsIgnoreCase("/clearclipboard")) {
            checkArgs(split, 0, 0, split[0]);
            session.setClipboard(null);
            player.print("Clipboard cleared.");
            return true;

        // Paste
        } else if (split[0].equalsIgnoreCase("/editpasteair") ||
                   split[0].equalsIgnoreCase("/editpaste")) {
            if (session.getClipboard() == null) {
                player.printError("Nothing is in your clipboard.");
            } else {
                Point pos = player.getBlockIn();
                session.getClipboard().paste(editSession, pos,
                    split[0].equalsIgnoreCase("/editpaste"));
                player.findFreePosition();
                player.print("Pasted. Undo with /editundo");
            }

            return true;

        // Fill a hole
        } else if (split[0].equalsIgnoreCase("/editfill")) {
            checkArgs(split, 2, 3, split[0]);
            int blockType = getItem(split[1]);
            int radius = Math.max(1, Integer.parseInt(split[2]));
            int depth = split.length > 3 ? Math.max(1, Integer.parseInt(split[3])) : 1;

            Point pos = player.getBlockIn();
            int affected = editSession.fillXZ((int)pos.getX(), (int)pos.getZ(),
                    pos, blockType, radius, depth);
            player.print(affected + " block(s) have been created.");

            return true;

        // Remove blocks above current position
        } else if (split[0].equalsIgnoreCase("/removeabove")) {
            int size = split.length > 1 ? Math.max(1, Integer.parseInt(split[1])) : 1;
            int height = split.length > 2 ? Math.min(128, Integer.parseInt(split[2]) + 2) : 128;

            int affected = editSession.removeAbove(player.getBlockIn(), size, height);
            player.print(affected + " block(s) have been removed.");

            return true;

        // Remove blocks below current position
        } else if (split[0].equalsIgnoreCase("/removebelow")) {
            int size = split.length > 1 ? Math.max(1, Integer.parseInt(split[1])) : 1;
            int height = split.length > 2 ? Math.max(1, Integer.parseInt(split[2])) : 128;

            int affected = editSession.removeBelow(player.getBlockIn(), size, height);
            player.print(affected + " block(s) have been removed.");

            return true;

        // Load .schematic to clipboard
        } else if (split[0].equalsIgnoreCase("/editload")) {
            checkArgs(split, 1, 1, split[0]);
            String filename = split[1].replace("\0", "") + ".schematic";
            File dir = new File("schematics");
            File f = new File("schematics", filename);

            try {
                String filePath = f.getCanonicalPath();
                String dirPath = dir.getCanonicalPath();

                if (!filePath.substring(0, dirPath.length()).equals(dirPath)) {
                    player.printError("Schematic could not read or it does not exist.");
                } else {
                    Point origin = player.getBlockIn();
                    session.setClipboard(CuboidClipboard.loadSchematic(filePath, origin));
                    logger.log(Level.INFO, player.getName() + " loaded " + filePath);
                    player.print(filename + " loaded.");
                }
            /*} catch (SchematicException e) {
                player.printError("Load error: " + e.getMessage());*/
            } catch (IOException e) {
                player.printError("Schematic could not read or it does not exist.");
            }

            return true;

        // Save clipboard to .schematic
        } else if (split[0].equalsIgnoreCase("/editsave")) {
            if (session.getClipboard() == null) {
                player.printError("Nothing is in your clipboard.");
                return true;
            }
            
            checkArgs(split, 1, 1, split[0]);
            String filename = split[1].replace("\0", "") + ".schematic";
            File dir = new File("schematics");
            File f = new File("schematics", filename);

            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    player.printError("A schematics/ folder could not be created.");
                    return true;
                }
            }

            try {
                String filePath = f.getCanonicalPath();
                String dirPath = dir.getCanonicalPath();

                if (!filePath.substring(0, dirPath.length()).equals(dirPath)) {
                    player.printError("Invalid path for Schematic.");
                } else {
                    // Create parent directories
                    File parent = f.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }

                    session.getClipboard().saveSchematic(filePath);
                    logger.log(Level.INFO, player.getName() + " saved " + filePath);
                    player.print(filename + " saved.");
                }
            } catch (SchematicException se) {
                player.printError("Save error: " + se.getMessage());
            } catch (IOException e) {
                player.printError("Schematic could not written.");
            }
            
            return true;

        // Run a script
        } else if (split[0].equalsIgnoreCase("/editscript")) {
            checkArgs(split, 1, -1, split[0]);
            String filename = split[1].replace("\0", "") + ".js";
            String[] args = new String[split.length - 2];
            System.arraycopy(split, 2, args, 0, split.length - 2);
            try {
                runScript(player, session, editSession, filename, args);
            } catch (NoSuchScriptException e) {
                player.printError("Script file does not exist.");
            }
            return true;
        } else if (split[0].equalsIgnoreCase("/editsize")) {
            player.print("# of blocks: " + session.getRegion().getSize());
            return true;

        // Replace all blocks in the region
        } else if(split[0].equalsIgnoreCase("/editset")) {
            checkArgs(split, 1, 1, split[0]);
            int blockType = getItem(split[1]);
            int affected = editSession.setBlocks(session.getRegion(), blockType);
            player.print(affected + " block(s) have been changed.");

            return true;

        // Set the outline of a region
        } else if(split[0].equalsIgnoreCase("/editoutline")) {
            checkArgs(split, 1, 1, split[0]);
            int blockType = getItem(split[1]);
            int affected = editSession.makeCuboidFaces(session.getRegion(), blockType);
            player.print(affected + " block(s) have been changed.");

            return true;

        // Replace all blocks in the region
        } else if(split[0].equalsIgnoreCase("/editreplace")) {
            checkArgs(split, 1, 2, split[0]);
            int from, to;
            if (split.length == 2) {
                from = -1;
                to = getItem(split[1]);
            } else {
                from = getItem(split[1]);
                to = getItem(split[2]);
            }
            
            int affected = editSession.replaceBlocks(session.getRegion(), from, to);
            player.print(affected + " block(s) have been replaced.");

            return true;

        // Lay blocks over an area
        } else if (split[0].equalsIgnoreCase("/editoverlay")) {
            checkArgs(split, 1, 1, split[0]);
            int blockType = getItem(split[1]);

            Region region = session.getRegion();
            int affected = editSession.overlayCuboidBlocks(region, blockType);
            player.print(affected + " block(s) have been overlayed.");

            return true;

        // Copy
        } else if (split[0].equalsIgnoreCase("/editcopy")) {
            checkArgs(split, 0, 0, split[0]);
            Region region = session.getRegion();
            Point min = region.getMinimumPoint();
            Point max = region.getMaximumPoint();
            Point pos = player.getBlockIn();

            CuboidClipboard clipboard = new CuboidClipboard(min, max, pos);
            clipboard.copy(editSession);
            session.setClipboard(clipboard);
            
            player.print("Block(s) copied.");

            return true;

        // Stack
        } else if (split[0].equalsIgnoreCase("/editstackair") ||
                   split[0].equalsIgnoreCase("/editstack")) {
            checkArgs(split, 0, 2, split[0]);
            int count = split.length > 1 ? Math.max(1, Integer.parseInt(split[1])) : 1;
            String dir = split.length > 2 ? split[2].toLowerCase() : "me";
            int xm = 0;
            int ym = 0;
            int zm = 0;
            boolean copyAir = split[0].equalsIgnoreCase("/editstackair");

            if (dir.equals("me")) {
                // From hey0's code
                double rot = (player.getYaw() - 90) % 360;
                if (rot < 0) {
                    rot += 360.0;
                }
                
                dir = etc.getCompassPointForDirection(rot).toLowerCase();
            }

            if (dir.charAt(0) == 'w') {
                zm += 1;
            } else if (dir.charAt(0) == 'e') {
                zm -= 1;
            } else if (dir.charAt(0) == 's') {
                xm += 1;
            } else if (dir.charAt(0) == 'n') {
                xm -= 1;
            } else if (dir.charAt(0) == 'u') {
                ym += 1;
            } else if (dir.charAt(0) == 'd') {
                ym -= 1;
            } else {
                player.printError("Unknown direction: " + dir);
                return true;
            }

            int affected = editSession.stackCuboidRegion(session.getRegion(),
                    xm, ym, zm, count, copyAir);
            player.print(affected + " blocks changed. Undo with /editundo");

            return true;
        }

        return false;
    }

    /**
     * Execute a script.
     *
     * @param player
     * @param filename
     * @param args
     * @return Whether the file was attempted execution
     */
    private boolean runScript(WorldEditPlayer player, WorldEditSession session,
            EditSession editSession, String filename, String[] args) throws
            NoSuchScriptException {
        File dir = new File("editscripts");
        File f = new File("editscripts", filename);

        try {
            String filePath = f.getCanonicalPath();
            String dirPath = dir.getCanonicalPath();

            if (!filePath.substring(0, dirPath.length()).equals(dirPath)) {
                throw new NoSuchScriptException();
            } else if (!f.exists()) {
                throw new NoSuchScriptException();
            } else {                
                // Read file
                StringBuffer buffer = new StringBuffer();
                FileInputStream stream = new FileInputStream(f);
                BufferedReader in = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                int c;
                while ((c = in.read()) > -1) {
                    buffer.append((char)c);
                }
                in.close();
                String code = buffer.toString();

                // Evaluate
                ScriptContextFactory factory = new ScriptContextFactory();
                Context cx = factory.enterContext();
                try {                    
                    ScriptableObject scope = cx.initStandardObjects();

                    // Add args
                    ScriptableObject.putProperty(scope, "args",
                        Context.javaToJS(args, scope));

                    // Add context
                    ScriptPlayer scriptPlayer = new ScriptPlayer(player);
                    ScriptContext context = new ScriptContext(
                        scriptPlayer);
                    ScriptableObject.putProperty(scope, "context",
                        Context.javaToJS(context, scope));
                    ScriptableObject.putProperty(scope, "player",
                        Context.javaToJS(scriptPlayer, scope));

                    // Add Minecraft context
                    ScriptMinecraftContext minecraft =
                        new ScriptMinecraftContext(editSession);
                    ScriptableObject.putProperty(scope, "minecraft",
                        Context.javaToJS(minecraft, scope));

                    logger.log(Level.INFO, player.getName() + ": executing " + filename + "...");
                    cx.evaluateString(scope, code, filename, 1, null);
                    logger.log(Level.INFO, player.getName() + ": script " + filename + " executed successfully.");
                    player.print(filename + " executed successfully.");
                } catch (RhinoException re) {
                    player.printError(filename + ": JS error: " + re.getMessage());
                    re.printStackTrace();
                } catch (Error err) {
                    player.printError(filename + ": execution error: " + err.getMessage());
                } finally {
                    Context.exit();
                }
            }

            return true;
        } catch (IOException e) {
            player.printError("Script could not read or it does not exist.");
        }

        return false;
    }

    /**
     * Gets the block type at a position x, y, z. Use an instance of
     * EditSession if possible.
     *
     * @param x
     * @param y
     * @param z
     * @return Block type
     */
    public int getBlock(int x, int y, int z) {
        return etc.getMCServer().e.a(x, y, z);
    }
}
