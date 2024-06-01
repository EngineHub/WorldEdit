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

repositories {
    maven {
        name = "EngineHub Repository (Releases Only)"
        url = uri("https://maven.enginehub.org/artifactory/libs-release-local/")
    }
    maven {
        name = "EngineHub Repository (Snapshots Only)"
        url = uri("https://maven.enginehub.org/artifactory/libs-snapshot-local/")
        content {
            excludeGroup("com.sk89q.worldedit")
        }
    }
    mavenCentral()
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
for (projectFragment in listOf("bukkit", "core", "fabric", "neoforge")) {
    val capitalizedFragment =
        projectFragment.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    val proj = project(":worldedit-$projectFragment")
    evaluationDependsOn(proj.path)

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

    val conf = configurations.create("${projectFragment}OldJar") {
        isCanBeResolved = true
    }
    val projPublication = proj.the<PublishingExtension>().publications.getByName<MavenPublication>("maven")
    conf.dependencies.add(
        dependencies.create("${projPublication.groupId}:${projPublication.artifactId}:$baseVersion").apply {
            (this as? ModuleDependency)?.isTransitive = false
        }
    )
    val resolvedOldJar = files({
        try {
            conf.resolvedConfiguration.rethrowFailure()
            conf
        } catch (e: ResolveException) {
            if (e.cause is ModuleVersionNotFoundException) {
                logger.warn("Skipping check for $projectFragment API compatibility because there is no jar to compare against")
                setOf<File>()
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
        newClasspath.from(proj.tasks.named("jar"))
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
tasks.named<JapicmpTask>("checkFabricApiCompatibility") {
    // Need to check against the reobf JAR
    newClasspath.setFrom(project(":worldedit-fabric").tasks.named("remapJar"))
}
