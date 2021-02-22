import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

repositories {
    maven { url = uri("https://hub.spigotmc.org/nexus/content/groups/public") }
    maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
}

configurations.all {
    resolutionStrategy {
        force("com.google.guava:guava:21.0")
    }
}

dependencies {
    "api"(project(":worldedit-core"))
    "api"(project(":worldedit-libs:bukkit"))
    "api"("org.spigotmc:spigot-api:1.16.1-R0.1-SNAPSHOT") {
        exclude("junit", "junit")
    }
    "compileOnly"("org.jetbrains:annotations:20.1.0")
    "compileOnly"("com.destroystokyo.paper:paper-api:1.16.1-R0.1-SNAPSHOT")
    "implementation"("io.papermc:paperlib:1.0.6")
    "compileOnly"("com.sk89q:dummypermscompat:1.10")
    "implementation"("org.slf4j:slf4j-jdk14:${Versions.SLF4J}")
    "implementation"("org.bstats:bstats-bukkit:2.1.0")
    "implementation"("it.unimi.dsi:fastutil")
    "testImplementation"("org.mockito:mockito-core:1.9.0-rc1")
}

tasks.named<Copy>("processResources") {
    val internalVersion = project.ext["internalVersion"]
    inputs.property("internalVersion", internalVersion)
    filesMatching("plugin.yml") {
        expand("internalVersion" to internalVersion)
    }
    // exclude adapters entirely from this JAR, they should only be in the shadow JAR
    exclude("**/worldedit-adapters.jar")
}

addJarManifest(WorldEditKind.Plugin, includeClasspath = true)

tasks.named<ShadowJar>("shadowJar") {
    from(zipTree("src/main/resources/worldedit-adapters.jar").matching {
        exclude("META-INF/")
    })
    dependencies {
        relocate("org.slf4j", "com.sk89q.worldedit.slf4j")
        relocate("org.antlr.v4", "com.sk89q.worldedit.antlr4")
        include(dependency(":worldedit-core"))
        include(dependency("org.slf4j:slf4j-api"))
        include(dependency("org.slf4j:slf4j-jdk14"))
        include(dependency("org.antlr:antlr4-runtime"))
        relocate("org.bstats", "com.sk89q.worldedit.bstats") {
            include(dependency("org.bstats:"))
        }
        relocate("io.papermc.lib", "com.sk89q.worldedit.bukkit.paperlib") {
            include(dependency("io.papermc:paperlib"))
        }
        relocate("it.unimi.dsi.fastutil", "com.sk89q.worldedit.bukkit.fastutil") {
            include(dependency("it.unimi.dsi:fastutil"))
        }
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
