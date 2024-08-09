import buildlogic.getLibrary
import buildlogic.stringyLibs
import org.gradle.plugins.ide.idea.model.IdeaModel

plugins {
    id("org.cadixdev.licenser")
}

group = rootProject.group
version = rootProject.version

configurations.all {
    resolutionStrategy {
        cacheChangingModulesFor(1, TimeUnit.DAYS)
    }
}

plugins.withId("java") {
    the<JavaPluginExtension>().toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

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

license {
    header(rootProject.file("HEADER.txt"))
    include("**/*.java")
    include("**/*.kt")
}

plugins.withId("idea") {
    configure<IdeaModel> {
        module {
            isDownloadSources = true
            isDownloadJavadoc = true
        }
    }
}
