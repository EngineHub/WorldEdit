// $Id$
/*
 * Quick shot music layout
 * Copyright (C) 2011 sk89q <http://www.sk89q.com>
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

importPackage(Packages.com.sk89q.worldedit);
importPackage(Packages.com.sk89q.worldedit.blocks);

var torchDirs = {};
torchDirs[Direction.NORTH] = [2, 4];
torchDirs[Direction.SOUTH] = [1, 3];
torchDirs[Direction.WEST] = [3, 2];
torchDirs[Direction.EAST] = [4, 1];

var pitches = {
    "f#": 0,
    "gb": 0,
    "g": 1,
    "g#": 2,
    "ab": 2,
    "a": 3,
    "a#": 4,
    "bb": 4,
    "b": 5,
    "c": 6,
    "c#": 7,
    "db": 7,
    "d": 8,
    "d#": 9,
    "eb": 9,
    "e": 10,
    "f": 11,
    "f#": 12,
    "gb": 12,
}

function generate(dir, notes) {
    var sess = context.remember();

    var origin = player.getBlockOn();
    var vec = dir.vector();
    var right = dir.leftVector().multiply(-1);
    var base = new BaseBlock(BlockID.DIRT);
    var instrument = new BaseBlock(BlockID.DIRT);
    var noteTorch = new BaseBlock(BlockID.REDSTONE_TORCH_ON, torchDirs[dir][1]);
    var backTorch = new BaseBlock(BlockID.REDSTONE_TORCH_ON, torchDirs[dir][0]);
    var wire = new BaseBlock(BlockID.REDSTONE_WIRE);
    
    var length = 4;
    
    for (var i = 0; i < notes.length; i++) {
        var note = new NoteBlock();
        note.setNote(notes[i]);
        
        var offset = origin.add(vec.multiply(i * length));
        sess.setBlock(offset, base);
        sess.setBlock(offset.add(0, 1, 0), wire);
        
        var forward1 = offset.add(vec.multiply(1));
        sess.setBlock(forward1.add(0, 1, 0), base);
        sess.setBlock(forward1.add(right.multiply(1)).add(0, 0, 0), instrument);
        sess.setBlock(forward1.add(right.multiply(1)).add(0, 1, 0), noteTorch);
        sess.setBlock(forward1.add(right.multiply(1)).add(0, 2, 0), note);
        sess.setBlock(forward1.add(right.multiply(2)), base);
        sess.setBlock(forward1.add(right.multiply(2)).add(0, 1, 0), wire);
        
        var forward2 = offset.add(vec.multiply(2));
        sess.setBlock(forward2.add(0, 1, 0), base);
        sess.setBlock(forward2.add(0, 2, 0), wire);
        sess.setBlock(forward2.add(right.multiply(1)).add(0, 1, 0), base);
        sess.setBlock(forward2.add(right.multiply(1)).add(0, 2, 0), wire);
        sess.setBlock(forward2.add(right.multiply(2)).add(0, 1, 0), base);
        sess.setBlock(forward2.add(right.multiply(2)).add(0, 2, 0), wire);
        
        var forward3 = offset.add(vec.multiply(3));
        sess.setBlock(forward3.add(0, 1, 0), backTorch);
    }
}

function main() {
    context.checkArgs(1, -1, "<note1> [note2] [note...]");

    var dir = player.getCardinalDirection();

    if (!dir.isOrthogonal()) {
        context.error("You need to be facing at a right angle to the world.");
        return;
    }
    
    var pattern = new RegExp("^([0-9])(.*)$");
    var notes = [];
    
    for (var i = 1; i < argv.length; i++) {
        var m = pattern.exec(argv[i])
        if (m == null) {
            context.error("Bad format (expected [octave][note]): " + argv[i]);
            return;
        }
        
        var octave = parseInt(m[1]);
        var n = m[2].toLowerCase();
        
        if (octave < 1 || octave > 2) {
            context.error("Valid octaves: 1 2");
            return;
        }
        
        if (!(n in pitches)) {
            context.error("Unknown note: " + n);
            return;
        }
        
        notes.push((octave - 1) * 12 + pitches[n]);
    }
    
    context.print(notes);
    
    generate(dir, notes);
}

main();