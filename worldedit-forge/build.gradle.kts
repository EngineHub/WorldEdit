import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecraftforge.gradle.common.util.RunConfig
import net.minecraftforge.gradle.userdev.UserDevExtension
import net.minecraftforge.gradle.userdev.tasks.GenerateSRG
import net.minecraftforge.gradle.userdev.tasks.RenameJarInPlace

plugins {
    id("net.minecraftforge.gradle")
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

val minecraftVersion = "1.14.4"
val mappingsMinecraftVersion = "1.14.3"
val forgeVersion = "28.1.0"

configurations.all {
    resolutionStrategy {
        force("com.google.guava:guava:21.0")
    }
}

dependencies {
    "compile"(project(":worldedit-core"))
    "compile"("org.apache.logging.log4j:log4j-slf4j-impl:2.11.2")

    "minecraft"("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")
}

configure<UserDevExtension> {
    mappings(mapOf(
            "channel" to "snapshot",
            "version" to "20190913-$mappingsMinecraftVersion"
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
    inputs.property("version", project.ext["internalVersion"])
    inputs.property("forgeVersion", forgeVersion)

    // replace stuff in mcmod.info, nothing else
    from(sourceSets["main"].resources.srcDirs) {
        include("META-INF/mods.toml")

        // replace version and mcversion
        expand(
                "version" to project.ext["internalVersion"],
                "forgeVersion" to forgeVersion
        )
    }

    // copy everything else except the mcmod.info
    from(sourceSets["main"].resources.srcDirs) {
        exclude("META-INF/mods.toml")
    }
}

tasks.named<Jar>("jar") {
    manifest {
        attributes("WorldEdit-Version" to project.version)
    }
}

tasks.named<ShadowJar>("shadowJar") {
    dependencies {
        relocate("org.slf4j", "com.sk89q.worldedit.slf4j")
        relocate("org.apache.logging.slf4j", "com.sk89q.worldedit.log4jbridge")

        include(dependency("org.slf4j:slf4j-api"))
        include(dependency("org.apache.logging.log4j:log4j-slf4j-impl"))
        include(dependency("de.schlichtherle:truezip"))
        include(dependency("org.mozilla:rhino"))
    }
    minimize {
        exclude(dependency("org.mozilla:rhino"))
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
