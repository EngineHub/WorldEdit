Compiling
=========

You can compile WorldEdit as long as you have the [Java Development Kit (JDK)](http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html) for Java 7 or newer. You only need one version of JDK installed.

The build process uses Gradle, which you do *not* need to download. WorldEdit is a multi-module project with three modules:

* `worldedit-core` contains WorldEdit
* `worldedit-bukkit` is the Bukkit plugin
* `worldedit-forge` is the Forge mod

## To compile...

### On Windows

1. Shift + right click the folder with WorldEdit's files and click "Open command prompt".
2. `gradlew clean setupDecompWorkspace`
3. `gradlew build`

### On Linux, BSD, or Mac OS X

1. In your terminal, navigate to the folder with WorldEdit's files (`cd /folder/of/worldedit/files`)
2. `./gradlew clean setupDecompWorkspace`
3. `./gradlew build`

## Then you will find...

You will find:

* The core WorldEdit API in **worldedit-core/build/libs**
* WorldEdit for Bukkit in **worldedit-bukkit/build/libs**
* WorldEdit for Forge in **worldedit-forge/build/libs**

If you want to use WorldEdit, use the `-shadow` version.

(The -shadow version includes WorldEdit + necessary libraries.)

## Other commands

* `gradlew idea` will generate an [IntelliJ IDEA](http://www.jetbrains.com/idea/) module for the Forge module.
* `gradlew eclipse` will generate an [Eclipse](https://www.eclipse.org/downloads/) project for the Forge version.
* Use `setupCIWorkspace` instead of `setupDecompWorkspace` if you are doing this on a CI server.
