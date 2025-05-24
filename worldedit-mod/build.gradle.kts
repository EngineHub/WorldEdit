import net.fabricmc.loom.task.RemapJarTask
import java.util.jar.Attributes
import java.util.jar.Manifest

plugins {
    base
    id("buildlogic.common")
}

open class MergeManifests : DefaultTask() {
    @InputFiles
    val inputManifests: ConfigurableFileCollection = project.objects.fileCollection()

    @OutputFile
    val outputManifest: RegularFileProperty = project.objects.fileProperty()

    companion object {
        private fun assertEqual(old: Any?, new: Any?, key: Attributes.Name): Any? {
            assert(old == new) { "$key mismatch: $old != $new" }
            return old
        }

        private fun throwException(old: Any?, new: Any?, key: Attributes.Name) {
            throw IllegalStateException("Duplicate $key: was $old, trying to add $new")
        }

        private val MERGE_LOGIC = mapOf(
            Attributes.Name.MANIFEST_VERSION to ::assertEqual,
            Attributes.Name.IMPLEMENTATION_VERSION to ::assertEqual,
            Attributes.Name.MAIN_CLASS to ::assertEqual,
            Attributes.Name("WorldEdit-Version") to ::assertEqual,
            Attributes.Name("WorldEdit-Kind") to ::assertEqual,
        )
    }

    private fun mergeAttributes(aggregate: Attributes, input: Attributes) {
        input.forEach { (key, value) ->
            aggregate.merge(key, value) { old, new ->
                val mergeLogic = MERGE_LOGIC[key] ?: ::throwException
                mergeLogic(old, new, key as Attributes.Name)
            }
        }
    }

    @TaskAction
    fun merge() {
        val manifest = Manifest()
        inputManifests.forEach { manifestFile ->
            val inputManifest = manifestFile.inputStream().use { Manifest(it) }
            mergeAttributes(manifest.mainAttributes, inputManifest.mainAttributes)
            inputManifest.entries.forEach { (key, value) ->
                val aggregate = manifest.entries.computeIfAbsent(key) { Attributes() }
                mergeAttributes(aggregate, value)
            }
        }
        outputManifest.asFile.get().outputStream().use {
            manifest.write(it)
        }
    }
}

val fabricZipTree = zipTree(
    project(":worldedit-fabric").tasks.named<RemapJarTask>("remapShadowJar").flatMap { it.archiveFile }
)

val mergeManifests = tasks.register<MergeManifests>("mergeManifests") {
    // TODO Extract forgeZipTree outside of this task when possible
    val forgeZipTree = zipTree(
        project(":worldedit-neoforge").tasks.named("jarJar").map { it.outputs.files.singleFile }
    )

    dependsOn(
        project(":worldedit-fabric").tasks.named<RemapJarTask>("remapShadowJar"),
        project(":worldedit-neoforge").tasks.named("jarJar")
    )
    inputManifests.from(
        fabricZipTree.matching { include("META-INF/MANIFEST.MF") },
        forgeZipTree.matching { include("META-INF/MANIFEST.MF") }
    )
    outputManifest.set(project.layout.buildDirectory.file("mergeManifests/MANIFEST.MF"))
}

tasks.register<Jar>("jar") {
    val forgeZipTree = zipTree(
        project(":worldedit-neoforge").tasks.named("jarJar").map { it.outputs.files.singleFile }
    )

    dependsOn(
        project(":worldedit-fabric").tasks.named<RemapJarTask>("remapShadowJar"),
        project(":worldedit-neoforge").tasks.named("jarJar"),
        mergeManifests
    )
    from(fabricZipTree) {
        exclude("META-INF/MANIFEST.MF")
    }
    from(forgeZipTree) {
        exclude("META-INF/MANIFEST.MF")
        // Duplicated first-party files
        exclude("META-INF/services/org.enginehub.piston.CommandManagerService")
        exclude("lang/")
        // No-brainer library excludes
        exclude("com/sk89q/jchronic/")
        exclude("com/sk89q/jnbt/")
        exclude("com/sk89q/minecraft/")
        exclude("com/sk89q/util/")
        exclude("com/thoughtworks/")
        exclude("net/royawesome/")
        exclude("org/enginehub/piston/")
        exclude("org/enginehub/linbus/")
        exclude("net/kyori/examination/")
        // Exclude worldedit-core
        exclude {
            val pathString = it.relativePath.pathString
            pathString.startsWith("com/sk89q/worldedit/") && !pathString.startsWith("com/sk89q/worldedit/neoforge/")
        }
        // Questionable excludes. So far the two files from each jar are the same.
        exclude("defaults/worldedit.properties")
        exclude("pack.mcmeta")
    }
    manifest {
        from(mergeManifests.flatMap { it.outputManifest })
    }

    duplicatesStrategy = DuplicatesStrategy.FAIL
    archiveClassifier.set("dist")
}

tasks.named("assemble") {
    dependsOn("jar")
}
