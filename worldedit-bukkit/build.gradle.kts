import buildlogic.internalVersion
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.papermc.paperweight.userdev.attribute.Obfuscation

plugins {
    `java-library`
    id("buildlogic.platform")
}

platform {
    kind = buildlogic.WorldEditKind.Plugin
    includeClasspath = true
}

val localImplementation = configurations.dependencyScope("localImplementation") {
    description = "Dependencies used locally, but provided by the runtime Bukkit implementation"
}
configurations.named("compileOnly") {
    extendsFrom(localImplementation.get())
}
configurations.named("testImplementation") {
    extendsFrom(localImplementation.get())
}

val adaptersScope = configurations.dependencyScope("adaptersScope") {
    description = "Adapters to include in the JAR"
}
val adaptersReobfScope = configurations.dependencyScope("adaptersReobfScope") {
    description = "Reobfuscated adapters to include in the JAR"
}

val adapters = configurations.resolvable("adapters") {
    extendsFrom(adaptersScope.get())
    description = "Adapters to include in the JAR (resolvable)"
    shouldResolveConsistentlyWith(configurations["runtimeClasspath"])
    attributes {
        attribute(Obfuscation.OBFUSCATION_ATTRIBUTE, objects.named(Obfuscation.NONE))
    }
}

val adaptersReobf = configurations.resolvable("adaptersReobf") {
    extendsFrom(adaptersReobfScope.get())
    description = "Reobfuscated adapters to include in the JAR (resolvable)"
    shouldResolveConsistentlyWith(configurations["runtimeClasspath"])
    attributes {
        attribute(Obfuscation.OBFUSCATION_ATTRIBUTE, objects.named(Obfuscation.OBFUSCATED))
    }
}

dependencies {
    "api"(project(":worldedit-core"))
    "api"(project(":worldedit-libs:bukkit"))

    "localImplementation"(libs.paperApi) {
        exclude("junit", "junit")
    }
    "localImplementation"(platform(libs.log4j.bom)) {
        because("Spigot provides Log4J (sort of, not in API, implicitly part of server)")
    }
    "localImplementation"(libs.log4j.api)

    "compileOnly"(libs.jetbrains.annotations) {
        because("Resolving Spigot annotations")
    }
    "testCompileOnly"(libs.jetbrains.annotations) {
        because("Resolving Spigot annotations")
    }
    "implementation"(libs.paperLib)
    "compileOnly"(libs.dummypermscompat)
    "implementation"(libs.bstats.bukkit)
    "implementation"(libs.fastutil)

    project.project(":worldedit-bukkit:adapters").subprojects.forEach {
        "adaptersScope"(project(it.path))
    }
    listOf("1.21.4", "1.21.5", "1.21.6", "1.21.9", "1.21.11").forEach {
        "adaptersReobfScope"(project(":worldedit-bukkit:adapters:adapter-$it"))
    }
}

tasks.named<Copy>("processResources") {
    // Avoid carrying project reference into task execution
    val internalVersion = project.internalVersion
    inputs.property("internalVersion", internalVersion)
    filesMatching("plugin.yml") {
        expand(mapOf("internalVersion" to internalVersion.get()))
    }
}

tasks.register<ShadowJar>("shadeReobfAdapters") {
    archiveClassifier.set("reobf-adapters")
    configurations.add(adaptersReobf.get())

    relocate("com.sk89q.worldedit.bukkit.adapter.impl", "com.sk89q.worldedit.bukkit.adapter.impl.reobf")
}

tasks.named<ShadowJar>("shadowJar") {
    from(tasks.named("shadeReobfAdapters"))
    configurations.add(adapters.get())
    dependencies {
        // In tandem with not bundling log4j, we shouldn't relocate base package here.
        // relocate("org.apache.logging", "com.sk89q.worldedit.log4j")
        relocate("org.antlr.v4", "com.sk89q.worldedit.antlr4")
        // Purposefully not included, we assume (even though no API exposes it) that Log4J will be present at runtime
        // If it turns out not to be true for Spigot/Paper, our only two official platforms, this can be uncommented.
        // include(dependency("org.apache.logging.log4j:log4j-api"))
        include(dependency("org.antlr:antlr4-runtime"))
        include(dependency("org.bstats:"))
        include(dependency("io.papermc:paperlib"))
        include(dependency("it.unimi.dsi:fastutil"))
        include(dependency("com.sk89q.lib:jlibnoise"))

        exclude(dependency("$group:$name"))

        relocate("org.bstats", "com.sk89q.worldedit.bstats")
        relocate("io.papermc.lib", "com.sk89q.worldedit.bukkit.paperlib")
        relocate("it.unimi.dsi.fastutil", "com.sk89q.worldedit.bukkit.fastutil")
        relocate("net.royawesome.jlibnoise", "com.sk89q.worldedit.jlibnoise")
    }
    project.project(":worldedit-bukkit:adapters").subprojects.forEach {
        dependencies {
            include(dependency("${it.group}:${it.name}"))
        }
        minimize {
            exclude(dependency("${it.group}:${it.name}"))
        }
    }
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}

configure<PublishingExtension> {
    publications.named<MavenPublication>("maven") {
        from(components["java"])
    }
}
