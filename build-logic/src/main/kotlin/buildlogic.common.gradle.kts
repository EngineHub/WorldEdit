import buildlogic.getLibrary
import buildlogic.stringyLibs

plugins {
    id("org.enginehub.crankcase.common")
    id("org.enginehub.crankcase.licensing")
    id("org.enginehub.crankcase.git")
}

group = rootProject.group
version = rootProject.version

dependencies {
    for (conf in listOf("implementation", "api")) {
        if (!configurations.names.contains(conf)) {
            continue
        }
        add(conf, platform(stringyLibs.getLibrary("log4j-bom")).map {
            val dep = create(it)
            dep.because("Mojang provides Log4j")
            dep
        })
        constraints {
            add(conf, stringyLibs.getLibrary("guava")) {
                because("Mojang provides Guava")
            }
            add(conf, stringyLibs.getLibrary("gson")) {
                because("Mojang provides Gson")
            }
            add(conf, stringyLibs.getLibrary("fastutil")) {
                because("Mojang provides FastUtil")
            }
        }
    }
}

levelHeadered {
    sourceMatchPatterns {
        // Exclude overrides for text formatting
        exclude("com/sk89q/worldedit/util/formatting/text/")
    }
}
