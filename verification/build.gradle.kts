import me.champeau.gradle.japicmp.JapicmpTask
import java.util.Locale

plugins {
    base
    id("org.enginehub.crankcase.japicmp")
}

// Pull the version before our current version.
val baseVersion = "(,${rootProject.version.toString().substringBefore("-SNAPSHOT")}["

// TODO Re-add neoforge when it's fixed
for (projectFragment in listOf("bukkit", "core", "fabric", "sponge")) {
    japicmp.addCheck(projectFragment) {
        // Skip the check when there is no prior published jar to compare against
        skipWhenOldClasspathMissing = true
        dependencies {
            oldClasspath("com.sk89q.worldedit:worldedit-$projectFragment:$baseVersion") {
                isTransitive = false
            }
            newClasspath(project(":worldedit-$projectFragment")) {
                isTransitive = false
            }
        }
        // Internals are not API
        packageExcludes.add("com.sk89q.worldedit*.internal*")
        // Mixins are not API
        packageExcludes.add("com.sk89q.worldedit*.mixin*")
        // Experimental is not API
        packageExcludes.add("com.sk89q.worldedit*.experimental*")
        when (projectFragment) {
            // Commands are not API
            "core" -> packageExcludes.add("com.sk89q.worldedit.command*")
            // Internal Adapters are not API
            "bukkit" -> packageExcludes.add("com.sk89q.worldedit.bukkit.adapter*")
        }
    }

    val capitalizedFragment = projectFragment.replaceFirstChar { it.titlecase(Locale.ROOT) }
    tasks.named<JapicmpTask>("check${capitalizedFragment}ApiCompatibility") {
        ignoreMissingClasses = true
    }
}
