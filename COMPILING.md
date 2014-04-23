Compiling
=========

You can (most easily) compile WorldEdit using one of the build scripts.

* **Maven** is for compiling **API**, **Bukkit**, and **Spout** versions
* **Gradle** is for the **Forge** version

WorldEdit is written and targetted for Java 6, but you can use newer
versions of Java to compile WorldEdit, though the compiler will give
you warnings. However, if you plan on compiling for Forge, you have
no choice in the matter and you **must** use Java 7 (make sure that it
is set as your 'default' Java too, if you have multiple versions).

The Java Development Kit is required, so install the latest version
either from Oracle's website or, if you're a Linux/BSD user, you
can also try using OpenJDK.

* [Get Oracle JDK 7 and 8](http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html)
* [Get Oracle JDK 6](http://www.oracle.com/technetwork/java/javasebusiness/downloads/java-archive-downloads-javase6-419409.html)

Dependencies
------------

WorldEdit requires several other dependencies, but thankfully, the build
scripts provided will automatically download these for you. If, for some
reason, compilation fails due to a missing dependency, please notify us.

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

By default, the *Bukkit* profile is enabled, but you can disable it
and enable other profiles.

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
