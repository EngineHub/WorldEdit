package buildlogic

import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

interface PlatformExtension {
    val kind: Property<WorldEditKind>
    val includeClasspath: Property<Boolean>
    val extraAttributes: MapProperty<String, String>
}

sealed class WorldEditKind(
    val name: String,
    val mainClass: String = "com.sk89q.worldedit.internal.util.InfoEntryPoint"
) {
    class Standalone(mainClass: String) : WorldEditKind("STANDALONE", mainClass)
    object Mod : WorldEditKind("MOD")
    object Plugin : WorldEditKind("PLUGIN")
}
