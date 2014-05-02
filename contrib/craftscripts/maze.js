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

usage = "<block> [width] [length] [height] [size] [thickness] flags\n";
usage += "\n";
usage += "Maze Flags:\n";
usage += "• i adds an entry and an exit\n";
usage += "• y places the entry and the exit randomly\n";
usage += "• f adds a floor\n";
usage += "• c adds a ceiling\n";
usage += "• e places air blocks\n";
usage += "• a places air blocks only\n";
usage += "• v creates a vertical maze\n";
usage += "\n";
usage += "Solver Flags:\n";
usage += "• s enables the maze solver\n";
usage += "• g places glass if wrong or unvisited\n";
usage += "• r places red wool if wrong\n";
usage += "• b places blue wool if unvisited";

context.checkArgs(1, -1, usage);

sess = context.remember();
origin = player.getBlockIn();

// This may throw an exception that is caught by the script processor
block = context.getBlock(argv[1]);

if (argv.length > 7) flags = String(argv[7]);
else flags = false;

if (argv.length > 6) {
    if (parseInt(argv[6], 10)) wa = parseInt(argv[6], 10);
    else flags = String(argv[6]), wa = 1;
} else wa = 1;

if (argv.length > 5) {
    if (parseInt(argv[5], 10)) s = parseInt(argv[5], 10);
    else flags = String(argv[5]), s = 1, wa = 1;
} else s = 1;

if (argv.length > 4) {
    if (parseInt(argv[4], 10)) h = parseInt(argv[4], 10);
    else flags = String(argv[4]), h = 2, s = 1, wa = 1;
} else h = 2;

if (argv.length > 3) {
    if (parseInt(argv[3], 10)) l = parseInt(argv[3], 10);
    else flags = String(argv[3]), l = 5, h = 2, s = 1, wa = 1;
} else l = 5;

if (argv.length > 2) {
    if (parseInt(argv[2], 10)) w = parseInt(argv[2], 10);
    else flags = String(argv[2]), w = 5, l = 5, h = 2, s = 1, wa = 1;
} else w = 5;

if (flags) {
    ee = flags.search("i") != -1 ? true : false;
    r = flags.search("y") != -1 ? true : false;
    if (r) ee = true;
    f = flags.search("f") != -1 ? true : false;
    c = flags.search("c") != -1 ? true : false;
    e = flags.search("e") != -1 ? true : false;
    ao = flags.search("a") != -1 ? true : false;
    if (ao) f = c = false, e = true;
    v = flags.search("v") != -1 ? true : false;
    so = flags.search("s") != -1 ? true : false;
    if (so) ee = true;
    g = flags.search("g") != -1 ? true : false;
    re = flags.search("r") != -1 ? true : false;
    bl = flags.search("b") != -1 ? true : false;
    if (g || re || bl) so = ee = true;
} else ee = r = f = c = e = ao = v = so = g = re = bl = false;

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
    i = arr.length;
    if (i === 0) return false;
    while (--i) {
        j = Math.floor(Math.random() * (i + 1));
        tempi = arr[i];
        tempj = arr[j];
        arr[i] = tempj;
        arr[j] = tempi;
    }
}

if (f || c) {
    for (z = 1; z <= wa; z++) for (y = -wa; y <= l * (s + wa) - 1; y++) for (x = -wa; x <= w * (s + wa) - 1; x++) {
        if (f) {
            if (!v) sess.setBlock(origin.add(x, -z, y), block);
            else sess.setBlock(origin.add(x, y, +z), block);
        }

        if (c) {
            if (!v) sess.setBlock(origin.add(x, z - 1 + h, y), block);
            else sess.setBlock(origin.add(x, y, -z + 1 - h), block);
        }
    }
}

stack = [];
visited = [];
noWallLeft = [];
noWallAbove = [];

stack.push(id(Math.floor(Math.random() * w), Math.floor(Math.random() * l)));

while (stack.length > 0) {
    cell = stack.pop();
    x = $x(cell);
    y = $y(cell);
    visited[cell] = true;
    neighbors = [];

    if (x > 0) neighbors.push(id(x - 1, y));
    if (x < w - 1) neighbors.push(id(x + 1, y));
    if (y > 0) neighbors.push(id(x, y - 1));
    if (y < l - 1) neighbors.push(id(x, y + 1));

    shuffle(neighbors);

    while (neighbors.length > 0) {
        neighbor = neighbors.pop();
        nx = $x(neighbor);
        ny = $y(neighbor);

        if (!visited[neighbor]) {
            stack.push(cell);

            if (y == ny) {
                if (nx < x) noWallLeft[cell] = true;
                else noWallLeft[neighbor] = true;
            } else {
                if (ny < y) noWallAbove[cell] = true;
                else noWallAbove[neighbor] = true;
            }

            stack.push(neighbor);
            break;
        }
    }
}

if (!r) {
    start = id(0, 0);
    end = id(w - 1, l - 1);
} else {
    start = id(0, Math.floor(Math.random() * l));
    end = id(w - 1, Math.floor(Math.random() * l));
}

if (ee) {
    noWallLeft[start] = true;
    noWallLeft[end + 1] = true;
}

/*for (y = -1; y < l; y++) {
    line = "";
    for (x = 0; x <= w; x++) {
        cell = id(x, y);
        a = y >= 0 ? (noWallLeft[cell] ? "_" : "|") : "_";
        b = x < w ? (noWallAbove[id(x, y + 1)] ? "  " : "_") : "";
        line += a + b;
    }
    context.print(line);
}*/

for (y = 0; y <= l; y++) for (x = 0; x <= w; x++) {
    cell = id(x, y);

    if (!noWallLeft[cell] && cell != id(x, l)) {
        if (!ao) {
            for (z = 0; z < h; z++) for (yi = 0; yi < s; yi++) for (xi = 1; xi <= wa; xi++) {
                if (!v) sess.setBlock(origin.add(x * (s + wa) - xi, z, y * (s + wa) + yi), block);
                else sess.setBlock(origin.add(x * (s + wa) - xi, y * (s + wa) + yi, -z), block);
            }
        }
    } else if (e && cell != id(x, l)) {
        for (z = 0; z < h; z++) for (yi = 0; yi < s; yi++) for (xi = 1; xi <= wa; xi++) {
            if (!v) sess.setBlock(origin.add(x * (s + wa) - xi, z, y * (s + wa) + yi), BaseBlock(0));
            else sess.setBlock(origin.add(x * (s + wa) - xi, y * (s + wa) + yi, -z), BaseBlock(0));
        }
    }

    if (!noWallAbove[cell] && cell != id(w, y)) {
        if (!ao) {
            for (z = 0; z < h; z++) for (yi = 1; yi <= wa; yi++) for (xi = 0; xi < s; xi++) {
                if (!v) sess.setBlock(origin.add(x * (s + wa) + xi, z, y * (s + wa) - yi), block);
                else sess.setBlock(origin.add(x * (s + wa) + xi, y * (s + wa) - yi, -z), block);
            }
        }
    } else if (e && cell != id(w, y)) {
        for (z = 0; z < h; z++) for (yi = 1; yi <= wa; yi++) for (xi = 0; xi < s; xi++) {
            if (!v) sess.setBlock(origin.add(x * (s + wa) + xi, z, y * (s + wa) - yi), BaseBlock(0));
            else sess.setBlock(origin.add(x * (s + wa) + xi, y * (s + wa) - yi, -z), BaseBlock(0));
        }
    }

    if (!ao) {
        for (z = 0; z < h; z++) for (yi = 1; yi <= wa; yi++) for (xi = 1; xi <= wa; xi++) {
            if (!v) sess.setBlock(origin.add(x * (s + wa) - xi, z, y * (s + wa) - yi), block);
            else sess.setBlock(origin.add(x * (s + wa) - xi, y * (s + wa) - yi, -z), block);
        }
    }

    if (e && cell != id(x, l) && cell != id(w, y)) {
        for (z = 0; z < h; z++) for (yi = 0; yi < s; yi++) for (xi = 0; xi < s; xi++) {
            if (!v) sess.setBlock(origin.add(x * (s + wa) + xi, z, y * (s + wa) + yi), BaseBlock(0));
            else sess.setBlock(origin.add(x * (s + wa) + xi, y * (s + wa) + yi, -z), BaseBlock(0));
        }
    }
}

if (so) {
    stack = [];
    visited = [];
    wrong = [];

    stack.push(start);

    while (cell != end) {
        if (visited[stack[stack.length - 1]]) wrong[cell] = true;

        cell = stack.pop();
        x = $x(cell);
        y = $y(cell);
        visited[cell] = true;
        neighbors = [];

        if (noWallLeft[cell] && cell != start) neighbors.push(id(x - 1, y));
        if (noWallLeft[id(x + 1, y)]) neighbors.push(id(x + 1, y));
        if (noWallAbove[cell]) neighbors.push(id(x, y - 1));
        if (noWallAbove[id(x, y + 1)]) neighbors.push(id(x, y + 1));

        shuffle(neighbors);

        while (neighbors.length > 0) {
            neighbor = neighbors.pop();

            if (!visited[neighbor]) {
                stack.push(cell);
                stack.push(neighbor);
                break;
            }
        }
    }

    for (y = 0; y <= l; y++) for (x = 0; x <= w; x++) {
        cell = id(x, y);

        if (visited[cell] && !wrong[cell]) {
            for (yi = 0; yi < s; yi++) for (xi = 0; xi < s; xi++) {
                if (!v) sess.setBlock(origin.add(x * (s + wa) + xi, -1, y * (s + wa) + yi), BaseBlock(35, 5));
                else sess.setBlock(origin.add(x * (s + wa) + xi, y * (s + wa) + yi, +1), BaseBlock(35, 5));
            }
        }

        if ((visited[cell] && !wrong[cell] && visited[id(x - 1, y)] && !wrong[id(x - 1, y)] && noWallLeft[cell]) || cell == start || id(x - 1, y) == end) {
            for (xi = 1; xi <= wa; xi++) for (yi = 0; yi < s; yi++) {
                if (!v) sess.setBlock(origin.add(x * (s + wa) - xi, -1, y * (s + wa) + yi), BaseBlock(35, 5));
                else sess.setBlock(origin.add(x * (s + wa) - xi, y * (s + wa) + yi, +1), BaseBlock(35, 5));
            }
        }

        if (visited[cell] && !wrong[cell] && visited[id(x, y - 1)] && !wrong[id(x, y - 1)] && noWallAbove[cell]) {
            for (xi = 0; xi < s; xi++) for (yi = 1; yi <= wa; yi++) {
                if (!v) sess.setBlock(origin.add(x * (s + wa) + xi, -1, y * (s + wa) - yi), BaseBlock(35, 5));
                else sess.setBlock(origin.add(x * (s + wa) + xi, y * (s + wa) - yi, +1), BaseBlock(35, 5));
            }
        }

        if (g) {
            if (visited[cell] && !wrong[cell] && (!visited[id(x - 1, y)] || wrong[id(x - 1, y)]) && noWallLeft[cell] && cell != start) {
                for (z = 0; z < h; z++) for (xi = 1; xi <= wa; xi++) for (yi = 0; yi < s; yi++) {
                    if (!v) sess.setBlock(origin.add(x * (s + wa) - xi, z, y * (s + wa) + yi), BaseBlock(20));
                    else sess.setBlock(origin.add(x * (s + wa) - xi, y * (s + wa) + yi, -z), BaseBlock(20));
                }
            }

            if ((!visited[cell] || wrong[cell]) && visited[id(x - 1, y)] && !wrong[id(x - 1, y)] && noWallLeft[cell] && id(x - 1, y) != end) {
                for (z = 0; z < h; z++) for (xi = 1; xi <= wa; xi++) for (yi = 0; yi < s; yi++) {
                    if (!v) sess.setBlock(origin.add(x * (s + wa) - xi, z, y * (s + wa) + yi), BaseBlock(20));
                    else sess.setBlock(origin.add(x * (s + wa) - xi, y * (s + wa) + yi, -z), BaseBlock(20));
                }
            }

            if (visited[cell] && !wrong[cell] && (!visited[id(x, y - 1)] || wrong[id(x, y - 1)]) && noWallAbove[cell]) {
                for (z = 0; z < h; z++) for (xi = 0; xi < s; xi++) for (yi = 1; yi <= wa; yi++) {
                    if (!v) sess.setBlock(origin.add(x * (s + wa) + xi, z, y * (s + wa) - yi), BaseBlock(20));
                    else sess.setBlock(origin.add(x * (s + wa) + xi, y * (s + wa) - yi, -z), BaseBlock(20));
                }
            }

            if ((!visited[cell] || wrong[cell]) && visited[id(x, y - 1)] && !wrong[id(x, y - 1)] && noWallAbove[cell]) {
                for (z = 0; z < h; z++) for (xi = 0; xi < s; xi++) for (yi = 1; yi <= wa; yi++) {
                    if (!v) sess.setBlock(origin.add(x * (s + wa) + xi, z, y * (s + wa) - yi), BaseBlock(20));
                    else sess.setBlock(origin.add(x * (s + wa) + xi, y * (s + wa) - yi, -z), BaseBlock(20));
                }
            }
        }

        if (re) {
            if (wrong[cell]) {
                for (yi = 0; yi < s; yi++) for (xi = 0; xi < s; xi++) {
                    if (!v) sess.setBlock(origin.add(x * (s + wa) + xi, -1, y * (s + wa) + yi), BaseBlock(35, 14));
                    else sess.setBlock(origin.add(x * (s + wa) + xi, y * (s + wa) + yi, +1), BaseBlock(35, 14));
                }
            }

            if ((wrong[cell] || wrong[id(x - 1, y)]) && noWallLeft[cell]) {
                for (xi = 1; xi <= wa; xi++) for (yi = 0; yi < s; yi++) {
                    if (!v) sess.setBlock(origin.add(x * (s + wa) - xi, -1, y * (s + wa) + yi), BaseBlock(35, 14));
                    else sess.setBlock(origin.add(x * (s + wa) - xi, y * (s + wa) + yi, +1), BaseBlock(35, 14));
                }
            }

            if ((wrong[cell] || wrong[id(x, y - 1)]) && noWallAbove[cell]) {
                for (xi = 0; xi < s; xi++) for (yi = 1; yi <= wa; yi++) {
                    if (!v) sess.setBlock(origin.add(x * (s + wa) + xi, -1, y * (s + wa) - yi), BaseBlock(35, 14));
                    else sess.setBlock(origin.add(x * (s + wa) + xi, y * (s + wa) - yi, +1), BaseBlock(35, 14));
                }
            }
        }

        if (bl) {
            if (!visited[cell] && y < l && x < w) {
                for (yi = 0; yi < s; yi++) for (xi = 0; xi < s; xi++) {
                    if (!v) sess.setBlock(origin.add(x * (s + wa) + xi, -1, y * (s + wa) + yi), BaseBlock(35, 11));
                    else sess.setBlock(origin.add(x * (s + wa) + xi, y * (s + wa) + yi, +1), BaseBlock(35, 11));
                }
            }

            if ((!visited[cell] || !visited[id(x - 1, y)]) && noWallLeft[cell] && x > 0 && x < w) {
                for (xi = 1; xi <= wa; xi++) for (yi = 0; yi < s; yi++) {
                    if (!v) sess.setBlock(origin.add(x * (s + wa) - xi, -1, y * (s + wa) + yi), BaseBlock(35, 11));
                    else sess.setBlock(origin.add(x * (s + wa) - xi, y * (s + wa) + yi, +1), BaseBlock(35, 11));
                }
            }

            if ((!visited[cell] || !visited[id(x, y - 1)]) && noWallAbove[cell]) {
                for (xi = 0; xi < s; xi++) for (yi = 1; yi <= wa; yi++) {
                    if (!v) sess.setBlock(origin.add(x * (s + wa) + xi, -1, y * (s + wa) - yi), BaseBlock(35, 11));
                    else sess.setBlock(origin.add(x * (s + wa) + xi, y * (s + wa) - yi, +1), BaseBlock(35, 11));
                }
            }
        }
    }
}
