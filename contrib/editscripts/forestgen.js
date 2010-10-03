// $Id$
/*
 * Rudimentary ugly pine tree forest generator
 * Copyright (c) 2010 sk89q <http://www.sk89q.com>
 * Licensed under the terms of GNU General Public License v3
*/

/*

    Usage:
        /editscript forestgen
        /editscript forestgen 20
    
    The first and only argument is the size of the square forest to
    create as the distance away from your current position. Default 30 if
    unspecified.

*/

var size = args.length > 0 ? Math.max(2, parseInt(args[0])) : 30;
var affected = 0;

function chanceBlock(x, y, z, t, c) {
    if (Math.random() <= c) {
        minecraft.setBlock(x, y, z, t);
    }
}

function makeTree(x, y, z) {
    var trunkHeight = Math.floor(Math.random() * 2) + 3;
    var height = Math.floor(Math.random() * 5) + 8;
    
    // Create trunk
    for (i = 0; i < trunkHeight; i++) {
        minecraft.setBlock(x, y + i, z, 17);
    }
    
    // Move up
    y = y + trunkHeight;
    
    var pos2 = [-2, 2];
    
    // Create tree + leaves
    for (i = 0; i < height; i++) {
        minecraft.setBlock(x, y + i, z, 17);
        
        // Less leaves at these levels
        var chance = (i == 0 || i == height - 1) ? 0.6 : 1
        
        // Inner leaves
        chanceBlock(x - 1, y + i, z, 18, chance);
        chanceBlock(x + 1, y + i, z, 18, chance);
        chanceBlock(x, y + i, z - 1, 18, chance);
        chanceBlock(x, y + i, z + 1, 18, chance);
        chanceBlock(x + 1, y + i, z + 1, 18, chance);
        chanceBlock(x - 1, y + i, z + 1, 18, chance);
        chanceBlock(x + 1, y + i, z - 1, 18, chance);
        chanceBlock(x - 1, y + i, z - 1, 18, chance);
        
        if (!(i == 0 || i == height - 1)) {
            for (var j = -2; j <= 2; j++) {
                chanceBlock(x - 2, y + i, z + j, 18, 0.6);
            }
            for (var j = -2; j <= 2; j++) {
                chanceBlock(x + 2, y + i, z + j, 18, 0.6);
            }
            for (var j = -2; j <= 2; j++) {
                chanceBlock(x + j, y + i, z - 2, 18, 0.6);
            }
            for (var j = -2; j <= 2; j++) {
                chanceBlock(x + j, y + i, z + 2, 18, 0.6);
            }
        }
    }
}

for (var x = player.blockX - size; x <= player.blockX + size; x++) {
    for (var z = player.blockZ - size; z <= player.blockZ + size; z++) {
        // Don't want to be in the ground
        if (minecraft.getBlock(x, player.blockY, z) != 0) { continue; }
        // The gods don't want a tree here
        if (Math.random() < 0.95) { continue; }
        
        for (var y = player.blockY; y >= player.blockY - 10; y--) {
            // Check if we hit the ground
            var t = minecraft.getBlock(x, y, z);
            if (t == 2 || t == 3) {
                makeTree(x, y + 1, z);
                affected++;
                break;
            } else if (t != 0) { // Trees won't grow on this!
                break;
            }
        }
    }
}

context.print(affected + " created tree(s)");