![WorldEdit](worldedit-logo.png)
=========

**A Minecraft Map Editor... that runs in-game!**

* With selections, schematics, copy and paste, brushes, and scripting!
* Use it in creative, survival in single player or on your server.
* Use it on your Minecraft server to fix grieving and mistakes.

Java Edition required. WorldEdit is compatible with Forge, Fabric, Bukkit, Spigot, Paper, and Sponge.

## Download WorldEdit

This place contains the Java code for WorldEdit, but if you want to just use WorldEdit, get the mod or plugin from these pages:

* For the mod: https://www.curseforge.com/minecraft/mc-mods/worldedit
* For the server plugin: https://dev.bukkit.org/projects/worldedit

Edit the Code
---------

Want to add new features to WorldEdit yourself? Follow these somewhat-easy steps:

1. Download WorldEdit's source code and put it somewhere. We recommend you use something called Git, but [you can also just download a .zip file](https://github.com/EngineHub/WorldEdit/archive/master.zip).
2. Install the [Java Development Kit (JDK) v8](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html) and restart your computer for safe measure.
3. Open terminal / command prompt / bash and navigate to the directory where you put the source code:
4. Run one of the following commands:
   * Mac OS X / Linux: `./gradlew :worldedit-fabric:runClient`
   * Windows - Command Prompt: `gradlew :worldedit-fabric:runClient`
   * Windows - PowerShell: `.\gradlew :worldedit-fabric:runClient`

That's it. 🎉 It takes a long time to compile all the code for WorldEdit. If it succeeds, **the Minecraft game will open and you can create a single player world with WorldEdit**.

⚠ However, if you get a `Type javax.tools.JavaCompiler not present` error, you need to edit your environmental variables and add a new `JAVA_HOME` variable. The value of the variable needs to be the path to where JDK 8 was installed.

For additional information about compiling WorldEdit, see [COMPILING.md](COMPILING.md).

Submitting Your Changes
------------

We happily accept contributions, especially through pull requests on GitHub. Submissions must be licensed under the GNU General Public License v3.

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for important guidelines to follow.

Links
-----

* [Visit our website](https://enginehub.org/)
* [Discord](https://discord.gg/enginehub)
* [Issue tracker](https://github.com/EngineHub/WorldEdit/issues)
* [Continuous integration](https://builds.enginehub.org) [![Build Status](https://ci.enginehub.org/app/rest/builds/buildType:bt10,branch:master/statusIcon.svg)](https://ci.enginehub.org/viewType.html?buildTypeId=bt10&guest=1)
* [End-user documentation](https://worldedit.enginehub.org/en/latest/)
