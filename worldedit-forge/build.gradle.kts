import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecraftforge.gradle.common.util.RunConfig
import net.minecraftforge.gradle.mcp.task.GenerateSRG
import net.minecraftforge.gradle.userdev.UserDevExtension
import net.minecraftforge.gradle.userdev.tasks.RenameJarInPlace

plugins {
    id("net.minecraftforge.gradle")
    `java-library`
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

val minecraftVersion = "1.16.3"
val nextMajorMinecraftVersion: String = minecraftVersion.split('.').let { (useless, major) ->
    "$useless.${major.toInt() + 1}"
}
val mappingsMinecraftVersion = "1.16"
val forgeVersion = "34.0.0"

configurations.all {
    resolutionStrategy {
        force("com.google.guava:guava:21.0")
    }
}

dependencies {
    "api"(project(":worldedit-core"))
    "implementation"("org.apache.logging.log4j:log4j-slf4j-impl:2.11.2")

    "minecraft"("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")
}

configure<UserDevExtension> {
    mappings(mapOf(
            "channel" to "snapshot",
            "version" to "20200514-$mappingsMinecraftVersion"
    ))

    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

    runs {
        val runConfig = Action<RunConfig> {
            properties(mapOf(
                    "forge.logging.markers" to "SCAN,REGISTRIES,REGISTRYDUMP",
                    "forge.logging.console.level" to "debug"
            ))
            workingDirectory = project.file("run").canonicalPath
            source(sourceSets["main"])
        }
        create("client", runConfig)
        create("server", runConfig)
    }

}

configure<BasePluginConvention> {
    archivesBaseName = "$archivesBaseName-mc$minecraftVersion"
}

tasks.named<Copy>("processResources") {
    // this will ensure that this task is redone when the versions change.
    val properties = mapOf(
            "version" to project.ext["internalVersion"],
            "forgeVersion" to forgeVersion,
            "minecraftVersion" to minecraftVersion,
            "nextMajorMinecraftVersion" to nextMajorMinecraftVersion
    )
    properties.forEach { (key, value) ->
        inputs.property(key, value)
    }

    // replace stuff in mcmod.info, nothing else
    from(sourceSets["main"].resources.srcDirs) {
        include("META-INF/mods.toml")

        // replace version and mcversion
        expand(properties)
    }

    // copy everything else except the mcmod.info
    from(sourceSets["main"].resources.srcDirs) {
        exclude("META-INF/mods.toml")
    }

    // copy from -core resources as well
    from(project(":worldedit-core").tasks.named("processResources"))
}

addJarManifest(includeClasspath = false)

tasks.named<ShadowJar>("shadowJar") {
    dependencies {
        relocate("org.slf4j", "com.sk89q.worldedit.slf4j")
        relocate("org.apache.logging.slf4j", "com.sk89q.worldedit.log4jbridge")
        relocate("org.antlr.v4", "com.sk89q.worldedit.antlr4")

        include(dependency("org.slf4j:slf4j-api"))
        include(dependency("org.apache.logging.log4j:log4j-slf4j-impl"))
        include(dependency("org.antlr:antlr4-runtime"))
        include(dependency("de.schlichtherle:truezip"))
        include(dependency("net.java.truevfs:truevfs-profile-default_2.13"))
        include(dependency("org.mozilla:rhino-runtime"))
    }
    minimize {
        exclude(dependency("org.mozilla:rhino-runtime"))
    }
}

afterEvaluate {
    val reobf = extensions.getByName<NamedDomainObjectContainer<RenameJarInPlace>>("reobf")
    reobf.maybeCreate("shadowJar").run {
        mappings = tasks.getByName<GenerateSRG>("createMcpToSrg").output
    }
}

tasks.register<Jar>("deobfJar") {
    from(sourceSets["main"].output)
    archiveClassifier.set("dev")
}

artifacts {
    add("archives", tasks.named("deobfJar"))
}
