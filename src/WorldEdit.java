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

import java.util.LinkedHashMap;

public class WorldEdit implements Plugin {
    private int[] editPos1 = new int[3];
    private int[] editPos2 = new int[3];
    private String editPos1Owner = "";
    private String editPos2Owner = "";
    private net.minecraft.server.MinecraftServer server;

    private boolean setBlock(int x, int y, int z, int blockType) {
        return server.e.d(x, y, z, blockType);
    }

    private int getBlock(int x, int y, int z) {
        return server.e.a(x, y, z);
    }

    public void enable() {
        server = etc.getMCServer();
        LinkedHashMap<String,String> commands = etc.getInstance().commands;
        commands.put("/editpos1", "Set editing position #1");
        commands.put("/editpos2", "Set editing position #2");
        commands.put("/editsize", "Get size of selected region");
        commands.put("/editset", "<Type> - Set all  blocks inside region");
        commands.put("/editreplace", "<ID> - Replace all existing blocks inside region");
        commands.put("/editoverlay", "<ID> - Overlay the area one layer");
        commands.put("/removeabove", "<Size> - Remove blocks above head");
        commands.put("/editfill", "<ID> <Radius> <Depth> - Fill a hole");
    }

    public void disable() {
    }

    public boolean onLoginChecks(String user) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public void onLogin(Player player) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public void onChat(Player player, String message) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public boolean onCommand(Player player, String[] split) {
        try {
            return handleEditCommand(player, split);
        } catch (NumberFormatException e) {
            player.sendMessage(Colors.Rose + "Number expected; string given.");
            return true;
        }
    }

    private boolean canDoEdit(Player player) {
        if (!player.getName().equals(editPos1Owner)) {
            player.sendMessage(Colors.Rose + "You don't own edit position #1. (Is someone else editing?)");
        } else if (!player.getName().equals(editPos2Owner)) {
            player.sendMessage(Colors.Rose + "You don't own edit position #2. (Is someone else editing?)");
        } else {
            return true;
        }
        return false;
    }

    private boolean handleEditCommand(Player player, String[] split) {
        int lowerX = Math.min(editPos1[0], editPos2[0]);
        int upperX = Math.max(editPos1[0], editPos2[0]);
        int lowerY = Math.min(editPos1[1], editPos2[1]);
        int upperY = Math.max(editPos1[1], editPos2[1]);
        int lowerZ = Math.min(editPos1[2], editPos2[2]);
        int upperZ = Math.max(editPos1[2], editPos2[2]);

        // Set edit position #1
        if (split[0].equalsIgnoreCase("/editpos1")) {
            editPos1Owner = player.getName();
            editPos1[0] = (int)Math.floor(player.getX());
            editPos1[1] = (int)Math.floor(player.getY());
            editPos1[2] = (int)Math.floor(player.getZ());
            player.sendMessage(Colors.LightPurple + "First edit position set.");
            return true;

        // Set edit position #2
        } else if (split[0].equalsIgnoreCase("/editpos2")) {
            editPos2Owner = player.getName();
            editPos2[0] = (int)Math.floor(player.getX());
            editPos2[1] = (int)Math.floor(player.getY());
            editPos2[2] = (int)Math.floor(player.getZ());
            player.sendMessage(Colors.LightPurple + "Second edit position set.");
            return true;
        
        // Get size of area
        } else if (split[0].equalsIgnoreCase("/editsize")) {
            if (!canDoEdit(player)) return true;
            int size = (upperX - lowerX + 1) * (upperY - lowerY + 1) * (upperZ - lowerZ + 1);
            player.sendMessage(Colors.LightPurple + "# of blocks: " + size);
            return true;

        // Replace all blocks in the region
        } else if(split[0].equalsIgnoreCase("/editset")) {
            if (!canDoEdit(player)) return true;
            int blockType = 0;
            if (split.length > 1) {
                blockType = Integer.parseInt(split[1]);
            }

            int affected = 0;

            for (int x = lowerX; x <= upperX; x++) {
                for (int y = lowerY; y <= upperY; y++) {
                    for (int z = lowerZ; z <= upperZ; z++) {
                        setBlock(x, y, z, blockType);
                        affected++;
                    }
                }
            }

            player.sendMessage(Colors.LightPurple + affected + " block(s) have been set.");

            return true;

        // Replace all blocks in the region
        } else if(split[0].equalsIgnoreCase("/editreplace")) {
            if (!canDoEdit(player)) return true;
            int blockType = 0;
            if (split.length > 1) {
                blockType = Integer.parseInt(split[1]);
            }

            int affected = 0;

            for (int x = lowerX; x <= upperX; x++) {
                for (int y = lowerY; y <= upperY; y++) {
                    for (int z = lowerZ; z <= upperZ; z++) {
                        if (getBlock(x, y, z) != 0) {
                            setBlock(x, y, z, blockType);
                            affected++;
                        }
                    }
                }
            }

            player.sendMessage(Colors.LightPurple + affected + " block(s) have been replaced.");

            return true;

        // Lay blocks over an area
        } else if (split[0].equalsIgnoreCase("/editoverlay")) {
            if (!canDoEdit(player)) return true;
            int blockType = 1;
            if (split.length > 1) {
                blockType = Integer.parseInt(split[1]);
            }

            // We don't want to pass beyond boundaries
            upperY = Math.min(127, upperY + 1);
            lowerY = Math.max(-128, lowerY - 1);

            int affected = 0;

            for (int x = lowerX; x <= upperX; x++) {
                for (int z = lowerZ; z <= upperZ; z++) {
                    for (int y = upperY; y >= lowerY; y--) {
                        if (y + 1 <= 127 && getBlock(x, y, z) != 0 && getBlock(x, y + 1, z) == 0) {
                            setBlock(x, y + 1, z, blockType);
                            affected++;
                            break;
                        }
                    }
                }
            }

            player.sendMessage(Colors.LightPurple + affected + " block(s) have been overlayed.");

            return true;

        // Fill a hole
        } else if (split[0].equalsIgnoreCase("/editfill")) {
            int blockType = 1;
            int radius = 10;
            int depth = 1;

            if (split.length > 1) {
                blockType = Integer.parseInt(split[1]);
            }
            if (split.length > 2) {
                radius = Integer.parseInt(split[2]);
            }
            if (split.length > 3) {
                depth = Integer.parseInt(split[3]);
            }

            int cx = (int)Math.floor(player.getX());
            int cy = (int)Math.floor(player.getY());
            int cz = (int)Math.floor(player.getZ());
            int minY = Math.max(-128, cy - depth);

            int affected = fill(cx, cz, cx, cy, cz, blockType, radius, minY);

            player.sendMessage(Colors.LightPurple + affected + " block(s) have been set.");

            return true;

        // Remove blocks above current position
        } else if (split[0].equalsIgnoreCase("/removeabove")) {
            int size = 0;
            if (split.length > 1) {
                size = Integer.parseInt(split[1]) - 1;
            }

            int affected = 0;
            int cx = (int)Math.floor(player.getX());
            int cy = (int)Math.floor(player.getY());
            int cz = (int)Math.floor(player.getZ());

            for (int x = cx - size; x <= cx + size; x++) {
                for (int z = cz - size; z <= cz + size; z++) {
                    for (int y = cy; y <= 127; y++) {
                        if (getBlock(x, y, z) != 0) {
                            setBlock(x, y, z, 0);
                            affected++;
                        }
                    }
                }
            }

            player.sendMessage(Colors.LightPurple + affected + " block(s) have been removed.");

            return true;
        }

        return false;
    }

    private int fill(int x, int z, int cx, int cy, int cz, int blockType, int radius, int minY) {
        double dist = Math.sqrt(Math.pow(cx - x, 2) + Math.pow(cz - z, 2));
        int affected = 0;
        
        if (dist > radius) {
            return 0;
        }

        if (getBlock(x, cy, z) == 0) {
            affected = fillY(x, cy, z, blockType, minY);
        } else {
            return 0;
        }
        
        affected += fill(x + 1, z, cx, cy, cz, blockType, radius, minY);
        affected += fill(x - 1, z, cx, cy, cz, blockType, radius, minY);
        affected += fill(x, z + 1, cx, cy, cz, blockType, radius, minY);
        affected += fill(x, z - 1, cx, cy, cz, blockType, radius, minY);

        return affected;
    }

    private int fillY(int x, int cy, int z, int blockType, int minY) {
        int affected = 0;
        
        for (int y = cy; y > minY; y--) {
            if (getBlock(x, y, z) == 0) {
                setBlock(x, y, z, blockType);
                affected++;
            } else {
                break;
            }
        }

        return affected;
    }

    public void onBan(Player player, String reason) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public void onIpBan(Player player, String reason) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public void onKick(Player player, String reason) {
        throw new UnsupportedOperationException("Not implemented.");
    }
}
