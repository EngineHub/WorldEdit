import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.neoforged.gradle.dsl.common.runs.run.Run

plugins {
    alias(libs.plugins.neogradle.userdev)
    `java-library`
    id("buildlogic.platform")
}

commonJava {
    // Not easy to do, because it's in a bunch of separate configurations
    banSlf4j = false
}

platform {
    kind = buildlogic.WorldEditKind.Mod
}

val minecraftVersion = libs.versions.neoforge.minecraft.get()
val nextMajorMinecraftVersion: String = minecraftVersion.split('.').let { (useless, major) ->
    "$useless.${major.toInt() + 1}"
}

val apiClasspath = configurations.create("apiClasspath") {
    isCanBeResolved = true
    extendsFrom(configurations.api.get())
}
jarJar.disableDefaultSources()

repositories {
    val toRemove = mutableListOf<MavenArtifactRepository>()
    for (repo in project.repositories) {
        if (repo is MavenArtifactRepository && repo.url.toString() == "https://maven.neoforged.net/releases/") {
            toRemove.add(repo)
        } else if (repo is MavenArtifactRepository && repo.url.toString() == "https://libraries.minecraft.net/") {
            exclusiveContent {
                forRepositories(repo).filter {
                    includeGroupAndSubgroups("org.lwjgl")
                }
            }
        }
    }
    toRemove.forEach { remove(it) }
    maven {
        name = "EngineHub"
        url = uri("https://maven.enginehub.org/repo/")
    }
}

dependencies {
    "api"(project(":worldedit-core"))

    "implementation"(libs.neoforge)
    "implementation"(libs.cuiProtocol.neoforge)
    jarJar(libs.cuiProtocol.neoforge)
}

minecraft {
    accessTransformers {
        file("src/main/resources/META-INF/accesstransformer.cfg")
    }
}

runs {
    val runConfig = Action<Run> {
        systemProperties(mapOf(
            "forge.logging.markers" to "SCAN,REGISTRIES,REGISTRYDUMP",
            "forge.logging.console.level" to "debug"
        ))
        workingDirectory(project.file("run").canonicalPath)
        modSources(sourceSets["main"])
        dependencies {
            runtime(apiClasspath)
        }
    }
    create("client", runConfig)
    create("server", runConfig)
}

subsystems {
    parchment {
        minecraftVersion = libs.versions.parchment.minecraft.get()
        mappingsVersion = libs.versions.parchment.mappings.get()
    }
    decompiler {
        maxMemory("3G")
    }
}

configure<BasePluginExtension> {
    archivesName.set("${archivesName.get()}-mc$minecraftVersion")
}

configure<PublishingExtension> {
    publications.named<MavenPublication>("maven") {
        artifactId = the<BasePluginExtension>().archivesName.get()
        from(components["java"])
    }
}

tasks.named<Copy>("processResources") {
    // this will ensure that this task is redone when the versions change.
    val properties = mapOf(
        "version" to project.ext["internalVersion"],
        "neoVersion" to libs.neoforge.get().version,
        "minecraftVersion" to minecraftVersion,
        "nextMajorMinecraftVersion" to nextMajorMinecraftVersion
    )
    properties.forEach { (key, value) ->
        inputs.property(key, value)
    }

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(properties)
    }

    // copy from -core resources as well
    from(project(":worldedit-core").tasks.named("processResources"))
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier = "dist-slim"
    dependencies {
        relocate("org.antlr.v4", "com.sk89q.worldedit.antlr4")
        relocate("net.royawesome.jlibnoise", "com.sk89q.worldedit.jlibnoise")

        include(dependency("org.antlr:antlr4-runtime"))
        include(dependency("org.mozilla:rhino-runtime"))
        include(dependency("com.sk89q.lib:jlibnoise"))
    }
    minimize {
        exclude(dependency("org.mozilla:rhino-runtime"))
    }
}

tasks.jarJar {
    archiveClassifier = "dist"
    val shadowJar = tasks.shadowJar.get()
    dependsOn(shadowJar)
    manifest.inheritFrom(shadowJar.manifest)
    from(project.zipTree(shadowJar.archiveFile).matching {
        exclude("META-INF/MANIFEST.MF")
    })
}
