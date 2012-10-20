// $Id$
/*
 * Very bad image drawer
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

importPackage(Packages.java.io);
importPackage(Packages.java.awt);
importPackage(Packages.javax.imageio);
importPackage(Packages.com.sk89q.worldedit);
importPackage(Packages.com.sk89q.worldedit.blocks);

function makeColor(r, g, b) {
    return new Color(r / 255, g / 255, b / 255);
}

var clothColors = [
    makeColor(254, 254, 254), // White - fixed so white gets picked over pink for white pixels
    makeColor(255, 100, 0), // Orange
    makeColor(200, 0, 200), // Magenta
    makeColor(87, 132, 223), // Light blue
    makeColor(255, 255, 0), // Yellow
    makeColor(0, 255, 0), // Green
    makeColor(255, 180, 200), // Pink
    makeColor(72, 72, 72), // Gray
    makeColor(173, 173, 173), // Light grey
    makeColor(0, 100, 160), // Cyan
    makeColor(120, 0, 200), // Purple
    makeColor(0, 0, 175), // Blue
    makeColor(100, 60, 0), // Brown
    makeColor(48, 80, 0), // Cactus green
    makeColor(255, 0, 0), // Red
    makeColor(0, 0, 0), // Black
]
var clothColorsOpt = [
	makeColor(178, 178, 178), // White
	makeColor(187, 102, 44), // Orange
	makeColor(152, 61, 161), // Magenta
	makeColor(84, 111, 170), // Light blue
	makeColor(156, 145, 23), // Yellow
	makeColor(47, 151, 38), // Green
	makeColor(174, 105, 124), // Pink
	makeColor(53, 53, 53), // Gray
	makeColor(127, 133, 133), // Light grey
	makeColor(32, 94, 120), // Cyan
	makeColor(103, 43, 156), // Purple
	makeColor(31, 41, 123), // Blue
	makeColor(68, 41, 22), // Brown
	makeColor(44, 61, 19), // Cactus green
	makeColor(131, 36, 32), // Red
	makeColor(21, 18, 18), // Black
]
var clothColorsOptHD = [
	makeColor(168, 168, 168), // White
	makeColor(143, 59, 0), // Orange 
	makeColor(152, 0, 67), // Magenta
	makeColor(0, 153, 153), // Light blue
	makeColor(150, 150, 0), // Yellow
	makeColor(59, 143, 0), // Green
	makeColor(167, 83, 125), // Pink
	makeColor(64, 64, 64), // Gray
	makeColor(101, 101, 101), // Light grey
	makeColor(0, 83, 83), // Cyan
	makeColor(43, 12, 75), // Purple
	makeColor(0, 38, 77), // Blue
	makeColor(52, 25, 0), // Brown
	makeColor(10, 76, 10), // Cactus green
	makeColor(127, 9, 9), // Red
	makeColor(17, 17, 17), // Black
]

// http://stackoverflow.com/questions/2103368/color-logic-algorithm/2103608#2103608
function colorDistance(c1, c2) {
    var rmean = (c1.getRed() + c2.getRed()) / 2;
    var r = c1.getRed() - c2.getRed();
    var g = c1.getGreen() - c2.getGreen();
    var b = c1.getBlue() - c2.getBlue();
    var weightR = 2 + rmean/256;
    var weightG = 4.0;
    var weightB = 2 + (255-rmean)/256
    return Math.sqrt(weightR*r*r + weightG*g*g + weightB*b*b);
}

function findClosestWoolColor(col, clothColors) {
	var closestId = 0;
	var closestDistance = colorDistance(col, clothColors[0]);
	
	for(var i = 1; i < clothColors.length; i++) {
		var dist = colorDistance(col, clothColors[i]);
		
		if(dist < closestDistance) {
			closestId = i;
			closestDistance = dist;
		}
	}
	
	return closestId;
}


context.checkArgs(1, 3, "<image> <orientation> <palette>");

var f = context.getSafeFile("drawings", argv[1]);
var sess = context.remember();
var upright = argv[2] == "v";
var colors = clothColors;
if(argv[3] == "opt") {
	colors = clothColorsOpt;
	player.print("Using optimized palette");
} else if(argv[3] == "optHD") {
	colors = clothColorsOptHD;
	player.print("Using optimized HD palette");
}
if (!f.exists()) {
    player.printError("Specified file doesn't exist.");
} else {
    var img = ImageIO.read(f);

    var width = img.getWidth();
    var height = img.getHeight();

    var origin = player.getBlockIn();

    for (var x = 0; x < width; x++) {
        for (var y = 0; y < height; y++) {
            var c = new Color(img.getRGB(x, y));
            var data = findClosestWoolColor(c,colors);
			// Added this to enable the user to create images upright
            // rather than flat on the ground
			if (!upright) {
                sess.setBlock(origin.add(x, 0, y), new BaseBlock(35, data));
			} else {
                sess.setBlock(origin.add(x, height - y, 0), new BaseBlock(35, data));
			}
        }
    }
}