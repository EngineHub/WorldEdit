import buildlogic.addEngineHubRepository
import buildlogic.internalVersion
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.neoforged.gradle.dsl.common.runs.run.Run

plugins {
    alias(libs.plugins.neogradle.userdev)
    `java-library`
    id("buildlogic.platform")
}

platform {
    kind = buildlogic.WorldEditKind.Mod
}

val minecraftVersion = libs.versions.neoforge.minecraft.get()
val nextMajorMinecraftVersion: String = minecraftVersion.split('.').let { (useless, major) ->
    "$useless.${major.toInt() + 1}"
}

val apiClasspath = configurations.resolvable("apiClasspath") {
    extendsFrom(configurations.api.get())
}

jarJar.disableDefaultSources()

repositories {
    addEngineHubRepository()
    mavenCentral()
}

configurations {
    val coreResourcesScope = dependencyScope("coreResourcesScope")
    resolvable("coreResourcesResolvable") {
        extendsFrom(coreResourcesScope.get())
        attributes {
            attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class, Category.VERIFICATION))
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling::class, Bundling.EXTERNAL))
            attribute(VerificationType.VERIFICATION_TYPE_ATTRIBUTE, objects.named(VerificationType::class, "resources"))
        }
    }
}

dependencies {
    api(project(":worldedit-core"))

    implementation(libs.neoforge)
    implementation(libs.cuiProtocol.neoforge)
    jarJar(libs.cuiProtocol.neoforge)

    "coreResourcesScope"(project(":worldedit-core"))
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
            runtime(apiClasspath.get())
        }
    }
    register("client").configure(runConfig)
    register("server").configure(runConfig)
}

subsystems {
    parchment {
        minecraftVersion = libs.versions.parchment.minecraft.get()
        mappingsVersion = libs.versions.parchment.mappings.get()
        addRepository = false
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
        "version" to internalVersion,
        "neoVersion" to libs.neoforge.get().version,
        "minecraftVersion" to minecraftVersion,
        "nextMajorMinecraftVersion" to nextMajorMinecraftVersion
    )
    properties.forEach { (key, value) ->
        inputs.property(key, value)
    }

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(properties.mapValues {
            when (val v = it.value) {
                is Provider<*> -> v.get()
                else -> v
            }
        })
    }

    // copy from -core resources as well
    from(configurations.named("coreResourcesResolvable"))
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
