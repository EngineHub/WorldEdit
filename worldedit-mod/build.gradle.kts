import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    base
}

applyCommonConfiguration()

val mergeJarContents = tasks.register<Copy>("mergeJarContents") {
    dependsOn(
        project(":worldedit-fabric").tasks.named("remapShadowJar"),
        project(":worldedit-forge").tasks.named("reobfShadowJar")
    )
    from(zipTree({project(":worldedit-fabric").tasks.getByName("shadowJar").outputs.files.singleFile}))
    from(zipTree({project(":worldedit-forge").tasks.getByName("shadowJar").outputs.files.singleFile}))
    into(project.layout.buildDirectory.dir("mergedFabricForgeJars"))
}

tasks.register<Jar>("jar") {
    from(mergeJarContents)
}

tasks.named("assemble") {
    dependsOn("jar")
}
