// Marks all blocks that have at least 3 exposed sides (adjacent to an air block)
// and then deletes those blocks.
// Based on jsa's MCEdit Filter clearBlocks.py

importClass(Packages.com.sk89q.worldedit.world.block.BlockTypes)

context.checkArgs(0, -1, "")

var blocks = context.remember()
var region = context.getSession().getRegionSelector(player.getWorld()).getRegion()

var min = region.getMinimumPoint()
var xi = region.getWidth()
var yi = region.getHeight()
var zi = region.getLength()

function sidesExposed(block) {
  if (blocks.getBlock(block).getBlockType().equals(BlockTypes.AIR)) {
    return 0
  }
  var sides = 0
  if (blocks.getBlock(block.add(0, 0, 1)).getBlockType().equals(BlockTypes.AIR)) {
    sides += 1
  }
  if (blocks.getBlock(block.add(0, 1, 0)).getBlockType().equals(BlockTypes.AIR)) {
    sides += 1
  }
  if (blocks.getBlock(block.add(1, 0, 0)).getBlockType().equals(BlockTypes.AIR)) {
    sides += 1
  }
  if (blocks.getBlock(block.add(0, 0, -1)).getBlockType().equals(BlockTypes.AIR)) {
    sides += 1
  }
  if (blocks.getBlock(block.add(0, -1, 0)).getBlockType().equals(BlockTypes.AIR)) {
    sides += 1
  }
  if (blocks.getBlock(block.add(-1, 0, 0)).getBlockType().equals(BlockTypes.AIR)) {
    sides += 1
  }
  return sides
}

var destroyThese = []

for (var i = 0; i < xi; i += 1) {
  for (var j = 0; j < zi; j += 1) {
    for (var k = 0; k < yi; k += 1) {
      var block = min.add(i, k, j)
      if (sidesExposed(block) >= 3) {
        destroyThese.push(block)
      }
    }
  }
}

for (var i = 0; i < destroyThese.length; i += 1) {
  blocks.setBlock(destroyThese[i], BlockTypes.AIR.getDefaultState())
}

context.print("[Clear Blocks] " + destroyThese.length + " blocks cleared.")
