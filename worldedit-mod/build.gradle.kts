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
    from(zipTree({project(":worldedit-forge").tasks.getByName("shadowJar").outputs.files.singleFile}))

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveClassifier.set("dist")
}

tasks.named("assemble") {
    dependsOn("jar")
}
