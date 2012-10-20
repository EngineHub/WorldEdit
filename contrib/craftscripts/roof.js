// $Id$
/*
 * Copyright (c) 2011 Bentech
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

importPackage(Packages.java.io);
importPackage(Packages.java.awt);
importPackage(Packages.com.sk89q.worldedit);
importPackage(Packages.com.sk89q.worldedit.blocks);

var blocks = context.remember();
var session = context.getSession();
var region = session.getRegion();

context.checkArgs(1, 1, "<type>");

var blocktype = context.getBlock(argv[1]);

var cycles = region.getLength()

if (region.getWidth() > cycles){
    cycles = region.getWidth();
}

cycles = cycles / 2;

for (var c = 0; c < cycles; c++) {
    for (var w = 0; w < region.getWidth() - (c * 2); w++) {
        for (var l = 0; l < region.getLength() - (c * 2); l++) {
            if (w == 0 || w == (region.getWidth() - (c * 2)) - 1 || l == 0 || l == (region.getLength() - (c * 2)) - 1) {
                var vec = new Vector(
                    region.getMinimumPoint().getX() + (w + c),
                    region.getMaximumPoint().getY() + c,
                    region.getMinimumPoint().getZ() + (l + c));
                
                blocks.setBlock(vec, blocktype);
            }
        }
    }
}