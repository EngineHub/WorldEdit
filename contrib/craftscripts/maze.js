// $Id$
/*
 * Maze generator CraftScript for WorldEdit
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

context.checkArgs(1, -1, "<block> [width] [length] [height] [size] [floor?] [ceiling?]");

var sess = context.remember();

// This may throw an exception that is caught by the script processor
var block = context.getBlock(argv[1]);
var w = argv.length > 2 ? parseInt(argv[2]) : 5;
var l = argv.length > 3 ? parseInt(argv[3]) : 5;
var h = argv.length > 4 ? parseInt(argv[4]) : 2;
var s = argv.length > 5 ? parseInt(argv[5]) : 1;
var f = argv.length > 6 ? String(argv[6]) : "no";
var c = argv.length > 7 ? String(argv[7]) : "no";

function id(x, y) {
    return y * (w + 1) + x;
}

function $x(i) {
    return i % (w + 1);
}

function $y(i) {
    return Math.floor(i / (w + 1));
}

function shuffle(arr) {
    var i = arr.length;
    if (i == 0) return false;
    while (--i) {
        var j = Math.floor(Math.random() * (i + 1));
        var tempi = arr[i];
        var tempj = arr[j];
        arr[i] = tempj;
        arr[j] = tempi;
    }
}

var stack = [];
var visited = {};
var noWallLeft = new Array(w * l);
var noWallAbove = new Array(w * l);
var current = 0;

stack.push(id(0, 0));

while (stack.length > 0) {
    var cell = stack.pop();
    var x = $x(cell),
        y = $y(cell);
    visited[cell] = true;

    var neighbors = [];

    if (x > 0) neighbors.push(id(x - 1, y));
    if (x < w - 1) neighbors.push(id(x + 1, y));
    if (y > 0) neighbors.push(id(x, y - 1));
    if (y < l - 1) neighbors.push(id(x, y + 1));

    shuffle(neighbors);

    while (neighbors.length > 0) {
        var neighbor = neighbors.pop();
        var nx = $x(neighbor),
            ny = $y(neighbor);

        if (visited[neighbor] != true) {
            stack.push(cell);

            if (y == ny) {
                if (nx < x) {
                    noWallLeft[cell] = true;
                } else {
                    noWallLeft[neighbor] = true;
                }
            } else {
                if (ny < y) {
                    noWallAbove[cell] = true;
                } else {
                    noWallAbove[neighbor] = true;
                }
            }

            stack.push(neighbor);
            break;
        }
    }
}

/*for (var y = -1; y < l; y++) {
    var line = "";
    for (var x = 0; x <= w; x++) {
        var cell = id(x, y)
        var a = y >= 0 ? (noWallLeft[cell] ? "_" : "|") : "_";
        var b = x < w ? (noWallAbove[id(x, y + 1)] ? "  " : "_") : "";
        line += a + b;
    }
    context.print(line);
}*/

var origin = player.getBlockIn();

for (var y = 0; y <= l; y++) {
    for (var x = 0; x <= w; x++) {
        var cell = id(x, y);
        if (!noWallLeft[cell] && y < l) {
            for (i = 0; i < s; i++) {
                for (z = 0; z < h; z++) {
                    sess.setBlock(origin.add(x * (s + 1) - 1, z, y * (s + 1) + i), block);
                }
            }
        }
        if (!noWallAbove[cell] && x < w) {
            for (i = 0; i < s; i++) {
                for (z = 0; z < h; z++) {
                    sess.setBlock(origin.add(x * (s + 1) + i, z, y * (s + 1) - 1), block);
                }
            }
        }
        for (z = 0; z < h; z++) {
            sess.setBlock(origin.add(x * (s + 1) - 1, z, y * (s + 1) - 1), block);
        }
    }
}

l = l * (s + 1) - 1;
w = w * (s + 1) - 1;

for (y = -1; y <= l; y++) {
    for (x = -1; x <= w; x++) {
        if (f == "yes") {
            sess.setBlock(origin.add(x, -1, y), block);
        }
        if (c == "yes") {
            sess.setBlock(origin.add(x, h, y), block);
        }
    }
}
