WorldEdit
=========

WorldEdit is a voxel and block manipulation library for Minecraft. It is
primarily a library but bindings to Bukkit (included) and SPC (external)
are available.

Compiling
---------

Some dependencies are required:

- [TrueZip](http://java.net/projects/truezip) provides snapshot reading
- [Bukkit](http://bukkit.org/) is a SMP plugin API
- [Rhino](http://www.mozilla.org/rhino/) provides a JavaScript engine
- [GroupUsers](http://forums.bukkit.org/threads/639/) provides a
    permission system for Bukkit
- [Permissions](http://forums.bukkit.org/threads/1403/) provides a
    permission system for Bukkit
- worldeditsunrhino.jar is included

For links to downloads, check out
[http://wiki.sk89q.com/wiki/WorldEdit/Development](http://wiki.sk89q.com/wiki/WorldEdit/Development)

To compile a .jar, use the Ant build file with the 'jar' target.

    ant jar

Contributing
------------

We happily accept contributions. The best way to do this is to fork
WorldEdit on GitHub, add your changes, and then submit a pull request. We'll
look at it, make comments, and merge it into WorldEdit if everything
works out.

Your submissions have to be licensed under the GNU General Public License v3.

General Concepts
----------------

The entry point for all of WorldEdit is in `com.sk89q.worldedit.WorldEdit`.
This is where all the events and chat commands are handled. The commands
themselves are found in the `com.sk89q.worldedit.commands` package.

Each user has a _session_ that stores session-related data, including
history and clipboard. The class that handles session data is
`com.sk89q.worldedit.LocalSession`. A copy of it is created when needed
by the `getSession` method of `WorldEdit` and it's also stored on
`WorldEdit` in a hash map. The history is merely a list of
`com.sk89q.worldedit.EditSession`s, while the clipboard is a copy of
`com.sk89q.worldedit.CuboidClipboard`.

Now, one of the most important classes in WorldEdit is
`com.sk89q.worldedit.EditSession`. Nearly all block sets and gets are routed
through it because it automatically records a log of operations (for undo),
handles block placement order, and does a lot of magic to make sure things
come out the way it is intended. However, to make sure that block placement
order is adhered, remember to call `EditSession.enableQueue()` and later
`EditSession.flushQueue()`. Also, to actually an edit session in a player's
history, it has to be passed to `LocalSession.remember(EditSession)`.

Blocks in WorldEdit are entirely abstracted. Block types and block data not
simply passed around; rather, because blocks can contain a lot more data
(such as with signs and such), all blocks are an instance of
`com.sk89q.worldedit.blocks.BaseBlock`. For special block types, there's
a `SignBlock`, a `ChestBlock`, etc. Blocks are __detached__ from the world,
meaning they don't know where they are. You can pass them around freely
if you want (this is why syntax like `//set sign:3|Hi|there!` can work).

If you are making a command, you need to add the new command to `plugin.yml`
if you are using Bukkit. However,
`com.sk89q.worldedit.dev.DocumentationPrinter` is a program that will
generate `plugin.yml` by using Java reflection on the command classes.
Commands are given an edit session automatically (with queue
enabled) and so there's not much to set up. If you want to add a new class
altogether that contains commands, you need to update the constructor of
`com.sk89q.worldedit.WorldEdit` load your class.

### Core Routines ###

`com.sk89q.worldedit.WorldEdit.getBlock` handles the block syntax
(such as `sign:3|Hi|there!`).
`com.sk89q.worldedit.WorldEdit.getBlockPattern` handles the pattern
syntax (such as `90%rock,10%brick` or `#clipboard`).

Package Summary
---------------

WorldEdit is well organized and uses abstraction heavily to make adding new
things easy. An explanation of WorldEdit's package layout is as follows:

* `com.sk89q.bukkit.migration` has classes to handle permissions for
  Bukkit plugins until Bukkit attains built-in permissions support
* `com.sk89q.util` has some utility classes
* `com.sk89q.util.commands` has some base command handling code
  (commands in WorldEdit are defined using Java annotations)
* `com.sk89q.worldedit` has core WorldEdit classes
* `com.sk89q.worldedit.bags` has support for block sources and sinks
  such as inventory (which allows blocks to be taken from a player's
  inventory)
* `com.sk89q.worldedit.blocks` abstracts blocks from the game
  (such as chest blocks, etc.) and has support for all block data
* `com.sk89q.worldedit.bukkit` contains the implementation of WorldEdit
  for Bukkit as a plugin
* `com.sk89q.worldedit.commands` has all of WorldEdit's chat commands
* `com.sk89q.worldedit.data` contains classes to read Minecraft's world
  files directly from disk
* `com.sk89q.worldedit.dev` contains a class to generate documentation
  and other development-related files
* `com.sk89q.worldedit.filters` contains filters used for the smoothing
  algorithm
* `com.sk89q.worldedit.patterns` contains the pattern support
  (such as for `//set 90%rock,10%air` or `//set #clipboard`)
* `com.sk89q.worldedit.regions` contains the selection regions for
  WorldEdit; there's only one at the moment (a cuboid), but different
  region shapes can be added easily
* `com.sk89q.worldedit.scripting` contains scripting engines
* `com.sk89q.worldedit.snapshots` contains snapshot loading code (but
  actual world file reading code is in `com.sk89q.worldedit.data`)
* `com.sk89q.worldedit.superpickaxe` contains the code for the different
  super pickaxe modes
* `com.sk89q.worldedit.superpickaxe.brush` contains the different brush
  shapes for the brush super pickaxe tools
* `com.sk89q.worldedit.util` has some utility classes
* `org.jnbt` is the JNBT library to read JNBT formatted files

Task Tutorials
--------------

### How to Add a Command ###

1. If you want to add your command to an existing category of commands
   (check out the classes in `com.sk89q.worldedit.commands`) then you
   can just re-use one. If you want to create a new class, create a new
   class (it does not have to inherit or implement anything) and add it
   to the constructor of `com.sk89q.worldedit.WorldEdit`.
2. Add a new method, named anything.
3. Add the `@Command` annotation to signify that it is a command. The
    `aliases` property contains a list of command aliases and the first
    one in the list is the main alias. `usage` contains parameter usage
    information. `desc` is a short description. `flags` is an optional
    string of flags (each flag is only one character long).
    `min` is the minimum number of arguments. `max` is the maximum number of
    arguments and it can be -1 to allow an unlimited number.
4. Adding `@CommandPermissions` causes permissions to be checked for the
   command. Only one permission needs to be satisfied in the list.
5. Write the command.
6. If using Bukkit, update `plugin.yml` or run
   `com.sk89q.worldedit.dev.DocumentationPrinter` to generate it for you.
7. Compile and test!
