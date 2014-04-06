Compiling
=========

You can (most easily) compile WorldEdit using one of the build scripts.

* **Maven** is for compiling **API**, **Bukkit**, and **Spout** versions
* **Gradle** is for the **Forge** version

The Java Development Kit is required.

* [Get JDK 7 and 8](http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html)
* [Get JDK 6](http://www.oracle.com/technetwork/java/javasebusiness/downloads/java-archive-downloads-javase6-419409.html)

WorldEdit is written and targetted for Java 6, though you can use newer
versions (with warnings). If you plan on compiling for Forge, you **need**
at least Java 7 to be your "default" Java. (You can install several versions
of the JDK.)

Dependencies
------------

Both build scripts will download dependencies automatically. If, for some
reason, compilation fails due to a missing dependency, please notify us.

Folder Structure
----------------

Two source directories are required for compilation:

* The main source files are *src/main/java*
* Files marked for deprecation and future removal are in *src/legacy/java*

Otheriwse, files for each platform are available in different folders:

* The Bukkit implementation is located in *src/bukkit/java*
* The Spout implementation is located in *src/spout/java*
* The Forge implementation is located in *src/forge/java*

Maven
-----

**Don't have Maven?** [Download Maven](http://maven.apache.org/download.cgi)
from the Maven website.

From WorldEdit's directory, execute the following command to compile a
Bukkit version:

    mvn clean package

Once done, the *target/* folder will contain a .jar file and release .zip
files.

### Other Variations

* `mvn clean package -P !bukkit` for just the API
* `mvn clean package -P !bukkit -P spout` for the Spout version

Gradle
------

**Note:** As mentioned previously, you need Java 7 (a recent version) to
execute the following steps successfully. Your `JAVA_HOME` environment
variable needs to be set to the path of JDK 7+.

**Don't have Gradle?** Replace `gradle` with `gradlew` below, which will
automatically download a copy of Gradle for you.

From WorldEdit's directory, clean the cache first with the following
command:

    gradle cleancache --refresh-dependencies
    
Build WorldEdit for Forge with:

    gradle build

Once complete, you will find the release .jar in the folder *build/libs*.

### Other Tasks

* `gradle setupDecompWorkspace idea` will generate an [IntelliJ IDEA](http://www.jetbrains.com/idea/) workspace
* `gradle setupDecompWorkspace eclipse` will generate an [Eclipse](https://www.eclipse.org/downloads/) workspace