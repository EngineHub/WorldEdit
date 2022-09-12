<h1>
    <img src="worldedit-logo.svg" alt="WorldEdit" width="400" /> 
</h1>

Table of Contents
-----------

<!--ts-->
   * [About WorldEdit](#about-worldedit)
      * [Download WorldEdit](#download-worldedit)
   * [Edit the Code](#edit-the-code)
      * [Using a Java IDE](#using-a-java-ide)
      * [Speeding up the Edit-Test-Edit-Test Cycle](#speeding-up-the-edit-test-edit-test-cycle)
   * [Submitting Your Changes](#submitting-your-changes)
   * [Links](#links)

<!--te-->

About WorldEdit
---------

**A Minecraft Map Editor... that runs in-game!**


WorldEdit is a world editing tool for the popular game "Minecraft" Java Edition that released in 2010, 
and has since amassed more than 20 million downloads worldwide!

* With selections, schematics, copy and paste, brushes, and scripting!
* Use it in creative, survival in single player or on your server.
* Use it on your Minecraft server to fix grieving and mistakes.

Java Edition is required. WorldEdit does not run on Bedrock/Windows 10 Edition.
WorldEdit is compatible with Forge, Fabric, Bukkit, Spigot, Paper, and Sponge.
WorldEdit supports all versions of Minecraft Java Edition.

**NOTE**: For versions 1.12 and earlier, WorldEdit version 6 is required.


## Download WorldEdit

This place contains the Java code for WorldEdit, but if you want to just use WorldEdit, get the mod or plugin from these pages:

* For the mod: https://www.curseforge.com/minecraft/mc-mods/worldedit
* For the server plugin: https://dev.bukkit.org/projects/worldedit

Edit the Code
---------

Want to add new features to WorldEdit or fix bugs yourself? You can get the game running, with WorldEdit, from the code here, without any additional outside steps, by doing the following *four* things:

1. Download WorldEdit's source code and put it somewhere with easy access. We recommend you use something called Git if you already know how to use it, but [you can also just download a .zip file](https://github.com/EngineHub/WorldEdit/archive/master.zip). If you plan on contributing changes, however, you will need to know how to use Git.
   * Using Git:
     * Fork this project repository
     * Clone the fork
     * Start making your changes and commit
     * Push your changes to your forked main branch
   * For more information on how Git works, please refer to the following website: https://product.hubspot.com/blog/git-and-github-tutorial-for-beginners
3. Install any version of Java greater than or equal to 16.
   * Note that if you do _not_ install JDK 16 exactly, Gradle will download it for you on first run. However, it is still required to have some form of Java installed for Gradle to start at all.
4. Open terminal / command prompt / bash and navigate to the directory where you put the source code.
5. Run **one** of these following commands:
   * Mac OS X / Linux: `./gradlew :worldedit-fabric:runClient`
   * Windows - Command Prompt: `gradlew :worldedit-fabric:runClient`
   * Windows - PowerShell: `.\gradlew :worldedit-fabric:runClient`

🎉 That's it. 🎉 It takes a long time to actually transform WorldEdit into a mod. If it succeeds, **the Minecraft game will open and you can create a single player world with WorldEdit**.

When you make changes to the code, you have to restart the game by re-running the command for your changes to take effect. If there are errors in your Java syntax, the command will fail.

For additional information about compiling WorldEdit, see [COMPILING.md](COMPILING.md).

### Using a Java IDE

To edit WorldEdit, you may use a Java IDE. While IntelliJ IDEA Community Edition is preferred, you may use any of the following IDEs:
* IntelliJ IDEA Community Edition
* Eclipse IDE for Java EE Developers
* Apache Netbeans (Latest Version)
You may also use an IDE of your own preference.

To edit WorldEdit in your seleceted IDE, follow these steps:

1. Download and install one of the following: 
* [IntelliJ IDEA Community Edition](https://www.jetbrains.com/idea/download/).
* [Eclipse IDE for Java EE Developers](https://www.eclipse.org/downloads/packages/release/neon/3/eclipse-ide-java-ee-developers).
* [Apache Netbeans](https://netbeans.apache.org/download/index.html).
2. In the IDE, open the folder that you saved WorldEdit's code in. This creates a new project in your IDE.

That's pretty much it.

If you want to be able to run the game also, follow these instructions:

1. Go to Run -> Edit Configurations.
2. Add a Gradle task:
   1. Choose `worldedit-fabric` for the project.
   2. For the tasks, type in `runClient`
3. Click OK
4. Under the Run menu again, go to "Debug [your new task]".

### Speeding up the Edit-Test-Edit-Test Cycle

It's a little annoying have to restart the game to test your changes. The best way to reduce the time is to run the server instead (using `runServer` instead of `runClient`) and then reconnect to the server after restarting it.

Submitting Your Changes
------------

WorldEdit is open source (specifically licensed under GPL v3), so note that your contributions will also be open source. The best way to submit a change is to create a fork on GitHub, put your changes there, and then create a "pull request" on our WorldEdit repository.

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for important guidelines to follow.

Links
-----

* [Visit our website](https://enginehub.org/)
* [Discord](https://discord.gg/enginehub)
* [Issue tracker](https://github.com/EngineHub/WorldEdit/issues)
* [Continuous integration](https://builds.enginehub.org) [![Build Status](https://ci.enginehub.org/app/rest/builds/buildType:bt10,branch:master/statusIcon.svg)](https://ci.enginehub.org/viewType.html?buildTypeId=bt10&guest=1)
* [End-user documentation](https://worldedit.enginehub.org/en/latest/)
