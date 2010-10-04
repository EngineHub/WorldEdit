// $Id$
/*
 * Meteor shower generator
 * Copyright (c) 2010 sk89q <http://www.sk89q.com>
 * Licensed under the terms of GNU General Public License v3
*/

/*

    Usage:
        /editscript meteorshower
        /editscript meteorshower 20
    
    The first and only argument is the size of the square area to use. Default
    to 30 if unspecified.

*/

var size = args.length > 0 ? Math.max(2, parseInt(args[0])) : 30;

var fallenTypes = [1, 20, 15, 16];

function makeMeteorite(x, y, z) {
    minecraft.setBlock(x, y, z, fallenTypes[Math.floor(Math.random() * fallenTypes.length)]);
    minecraft.setBlock(x, y + 1, z, 17);
    minecraft.setBlock(x, y + 2, z, 51);
}

var initialBlockY = Math.min(63, player.blockY + 10); 

for (var x = player.blockX - size; x <= player.blockX + size; x++) {
    for (var z = player.blockZ - size; z <= player.blockZ + size; z++) {
        // Don't want to be in the ground
        if (minecraft.getBlock(x, player.blockY, z) != 0) { continue; }
        if (Math.random() < 0.994) { continue; }
        
        for (var y = player.blockY; y >= player.blockY - 10; y--) {
            // Check if we hit the ground
            var t = minecraft.getBlock(x, y, z);
            if (t != 0) {
                makeMeteorite(x, y + 1, z);
                break;
            }
        }
    }
}

context.print("Ruuuuuuuuun!");