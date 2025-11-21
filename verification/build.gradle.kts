import japicmp.accept.AcceptingSetupRule
import japicmp.accept.BinaryCompatRule
import me.champeau.gradle.japicmp.JapicmpTask
import org.gradle.internal.resolve.ModuleVersionNotFoundException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*

plugins {
    base
    id("me.champeau.gradle.japicmp")
}

val resetAcceptedApiChangesFiles by tasks.registering {
    group = "API Compatibility"
    description = "Resets ALL the accepted API changes files"
}

val checkApiCompatibility by tasks.registering {
    group = "API Compatibility"
    description = "Checks ALL API compatibility"
}

tasks.check {
    dependsOn(checkApiCompatibility)
}

// Generic setup for all tasks
// Pull the version before our current version.
val baseVersion = "(,${rootProject.version.toString().substringBefore("-SNAPSHOT")}["
// TODO Re-add neoforge when it's fixed
for (projectFragment in listOf("bukkit", "cli", "core", "fabric", "sponge")) {
    val capitalizedFragment =
        projectFragment.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    val changeFile = project.file("src/changes/accepted-$projectFragment-public-api-changes.json").toPath()

    val resetChangeFileTask = tasks.register("reset${capitalizedFragment}AcceptedApiChangesFile") {
        group = "API Compatibility"
        description = "Reset the accepted API changes file for $projectFragment"
        outputs.file(changeFile)

        doFirst {
            Files.newBufferedWriter(changeFile, StandardCharsets.UTF_8).use {
                it.write("{\n}")
            }
        }
    }
    resetAcceptedApiChangesFiles {
        dependsOn(resetChangeFileTask)
    }

    val oldJarScope = configurations.dependencyScope("${projectFragment}OldJarScope")
    val oldJarConf = configurations.resolvable("${projectFragment}OldJar") {
        extendsFrom(oldJarScope.get())
    }
    val newJarScope = configurations.dependencyScope("${projectFragment}NewJarScope")
    val newJarConf = configurations.resolvable("${projectFragment}NewJar") {
        extendsFrom(newJarScope.get())
    }
    dependencies {
        (oldJarScope.name)("com.sk89q.worldedit:worldedit-$projectFragment:$baseVersion") {
            isTransitive = false
        }
        (newJarScope.name)(dependencies.project(":worldedit-$projectFragment")) {
            isTransitive = false
        }
    }
    val resolvedOldJar = files({
        try {
            val confRealized = oldJarConf.get()
            confRealized.resolvedConfiguration.rethrowFailure()
            confRealized
        } catch (e: ResolveException) {
            if (e.cause is ModuleVersionNotFoundException) {
                logger.warn("Skipping check for $projectFragment API compatibility because there is no jar to compare against")
                logger.info("API compatibility exception details: ", e)
                setOf()
            } else {
                throw e
            }
        }
    })
    val checkApi = tasks.register<JapicmpTask>("check${capitalizedFragment}ApiCompatibility") {
        group = "API Compatibility"
        description = "Check API compatibility for $capitalizedFragment API"
        inputs.file(changeFile)
        richReport {
            addSetupRule(
                AcceptingSetupRule::class.java, AcceptingSetupRule.createParams(
                    changeFile,
                )
            )
            addRule(BinaryCompatRule::class.java)
            reportName.set("api-compatibility-$projectFragment.html")
        }

        onlyIf {
            // Only check if we have a jar to compare against
            !resolvedOldJar.isEmpty
        }

        oldClasspath.from(resolvedOldJar)
        newClasspath.from(newJarConf)
        onlyModified.set(false)
        failOnModification.set(false) // report does the failing (so we can accept)
        ignoreMissingClasses.set(true)

        // Internals are not API
        packageExcludes.add("com.sk89q.worldedit*.internal*")
        // Mixins are not API
        packageExcludes.add("com.sk89q.worldedit*.mixin*")
        // Experimental is not API
        packageExcludes.add("com.sk89q.worldedit*.experimental*")
    }

    checkApiCompatibility {
        dependsOn(checkApi)
    }
}

// Specific project overrides
tasks.named<JapicmpTask>("checkCoreApiCompatibility") {
    // Commands are not API
    packageExcludes.add("com.sk89q.worldedit.command*")
}
tasks.named<JapicmpTask>("checkBukkitApiCompatibility") {
    // Internal Adapters are not API
    packageExcludes.add("com.sk89q.worldedit.bukkit.adapter*")
}
