import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("org.spongepowered.gradle.plugin")
    id("org.spongepowered.gradle.vanilla")
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

// I can't believe sponge sets this in a base plugin with no opt-out
convention.getPlugin(JavaPluginConvention::class.java).apply {
    setSourceCompatibility(null)
    setTargetCompatibility(null)
}

repositories {
    maven { url = uri("https://repo.codemc.org/repository/maven-public") }
    maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }
}

dependencies {
    "api"(project(":worldedit-core"))
    "api"(project(":worldedit-libs:sponge"))
    "api"("org.spongepowered:spongeapi:8.0.0-SNAPSHOT")
//    "implementation"("org.bstats:bstats-sponge:1.7")
    "implementation"("org.apache.logging.log4j:log4j-slf4j-impl:2.11.2")
    "implementation"("it.unimi.dsi:fastutil:${Versions.FAST_UTIL}")
    "testImplementation"("org.mockito:mockito-core:1.9.0-rc1")
}

minecraft {
    version("1.16.5")
    injectRepositories(false)
}

//sponge {
//    plugin {
//        id = "worldedit"
//    }
//}

addJarManifest(includeClasspath = true)

tasks.named<ShadowJar>("shadowJar") {
    dependencies {
        relocate("org.slf4j", "com.sk89q.worldedit.slf4j")
        relocate("org.apache.logging.slf4j", "com.sk89q.worldedit.log4jbridge")
        relocate("org.antlr.v4", "com.sk89q.worldedit.antlr4")

        include(dependency(":worldedit-core"))
        include(dependency("org.slf4j:slf4j-api"))
        include(dependency("org.apache.logging.log4j:log4j-slf4j-impl"))
        include(dependency("org.antlr:antlr4-runtime"))
//        relocate ("org.bstats", "com.sk89q.worldedit.sponge.bstats") {
//            include(dependency("org.bstats:bstats-sponge:1.7"))
//        }
        relocate("it.unimi.dsi.fastutil", "com.sk89q.worldedit.sponge.fastutil") {
            include(dependency("it.unimi.dsi:fastutil"))
        }
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
