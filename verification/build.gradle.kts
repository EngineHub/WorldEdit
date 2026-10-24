import me.champeau.gradle.japicmp.JapicmpTask
import java.util.Locale

plugins {
    base
    id("org.enginehub.crankcase.japicmp")
}

// Pull the version before our current version, within the same major version.
val currentVersion = version.toString().substringBefore("-SNAPSHOT")
val baseVersion = "[${currentVersion.substringBefore('.')},$currentVersion["

for (projectFragment in listOf("bukkit", "core", "fabric", "neoforge", "sponge")) {
    japicmp.addCheck(projectFragment) {
        // Skip the check when there is no prior published jar to compare against
        skipWhenOldClasspathMissing = true
        dependencies {
            val artifactId = when (projectFragment) {
                "fabric" -> "worldedit-fabric-mc${libs.fabric.minecraft.get().version}"
                "neoforge" -> "worldedit-neoforge-mc${libs.versions.neoforge.minecraft.get()}"
                else -> "worldedit-$projectFragment"
            }
            oldClasspath("com.sk89q.worldedit:$artifactId:$baseVersion") {
                isTransitive = false
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_API))
                }
            }
            newClasspath(project(":worldedit-$projectFragment")) {
                isTransitive = false
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_API))
                }
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
