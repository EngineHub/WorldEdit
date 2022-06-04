import net.fabricmc.loom.task.RemapJarTask

plugins {
    base
}

applyCommonConfiguration()

tasks.register<Jar>("jar") {
    val remapFabric = project(":worldedit-fabric").tasks.named<RemapJarTask>("remapShadowJar")
    dependsOn(
        remapFabric,
        project(":worldedit-forge").tasks.named("reobfShadowJar")
    )
    from(zipTree({remapFabric.get().archiveFile}))
    from(zipTree({project(":worldedit-forge").tasks.getByName("shadowJar").outputs.files.singleFile})) {
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
        // Exclude worldedit-core
        exclude {
            val pathString = it.relativePath.pathString
            pathString.startsWith("com/sk89q/worldedit/") && !pathString.startsWith("com/sk89q/worldedit/forge/")
        }
        // Questionable excludes. So far the two files from each jar are the same.
        exclude("defaults/worldedit.properties")
        exclude("pack.mcmeta")
    }

    duplicatesStrategy = DuplicatesStrategy.FAIL
    archiveClassifier.set("dist")
}

tasks.named("assemble") {
    dependsOn("jar")
}
