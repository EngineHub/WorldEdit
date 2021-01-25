Compiling
=========

You can compile WorldEdit as long as you have some version of Java greater than or equal to 8 installed. Gradle will download JDK 8 specifically if needed,
but it needs some version of Java to bootstrap from.

The build process uses Gradle, which you do *not* need to download. WorldEdit is a multi-module project with four modules:

* `worldedit-core` contains the WorldEdit API
* `worldedit-bukkit` is the Bukkit plugin
* `worldedit-sponge` is the Sponge plugin
* `worldedit-forge` is the Forge mod
* `worldedit-fabric` is the Fabric mod

## To compile...

### On Windows

1. Shift + right click the folder with WorldEdit's files and click "Open command prompt".
2. `gradlew build`

### On Linux, BSD, or Mac OS X

1. In your terminal, navigate to the folder with WorldEdit's files (`cd /folder/of/worldedit/files`)
2. `./gradlew build`

## Then you will find...

You will find:

* The core WorldEdit API in **worldedit-core/build/libs**
* WorldEdit for Bukkit in **worldedit-bukkit/build/libs**
* WorldEdit for Sponge in **worldedit-sponge/build/libs**
* WorldEdit for Forge in **worldedit-forge/build/libs**
* WorldEdit for Fabric in **worldedit-fabric/build/libs**

If you want to use WorldEdit, use the `-dist` version.

(The -dist version includes WorldEdit + necessary libraries.)

## Other commands

* `gradlew idea` will generate an [IntelliJ IDEA](http://www.jetbrains.com/idea/) module for each folder.
* `gradlew eclipse` will generate an [Eclipse](https://www.eclipse.org/downloads/) project for each folder.
