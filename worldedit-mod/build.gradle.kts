import net.fabricmc.loom.task.RemapJarTask

plugins {
    base
}

applyCommonConfiguration()

val mergeJarContents = tasks.register<Copy>("mergeJarContents") {
    val remapFabric = project(":worldedit-fabric").tasks.named<RemapJarTask>("remapShadowJar")
    dependsOn(
        remapFabric,
        project(":worldedit-forge").tasks.named("reobfShadowJar")
    )
    from(zipTree({remapFabric.get().archiveFile}))
    from(zipTree({project(":worldedit-forge").tasks.getByName("shadowJar").outputs.files.singleFile}))
    into(project.layout.buildDirectory.dir("mergedFabricForgeJars"))
}

tasks.register<Jar>("jar") {
    archiveClassifier.set("dist")
    from(mergeJarContents)
}

tasks.named("assemble") {
    dependsOn("jar")
}
