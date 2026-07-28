import buildlogic.VerifyMinecraftLibraries
import buildlogic.getLibrary
import buildlogic.stringyLibs

val mojangProvidedLibs = listOf("guava", "gson", "fastutil", "log4j-api", "log4j-core")
    .map { stringyLibs.getLibrary(it) }

val mojangProvided = configurations.dependencyScope("mojangProvided")
val mojangProvidedClasspath = configurations.resolvable("mojangProvidedClasspath") {
    extendsFrom(mojangProvided.get())
}

dependencies {
    add(mojangProvided.name, platform(stringyLibs.getLibrary("log4j-bom")))
    for (lib in mojangProvidedLibs) {
        add(mojangProvided.name, lib)
    }
}

val verifyMinecraftLibraries = tasks.register<VerifyMinecraftLibraries>("verifyMinecraftLibraries") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Checks the Mojang-provided library versions we resolve against the ones Minecraft ships"

    declaredLibraries.set(
        mojangProvidedClasspath.flatMap { it.incoming.resolutionResult.rootComponent }
    )
    minecraftLibraries.set(
        configurations.named("minecraftLibraries").flatMap { it.incoming.resolutionResult.rootComponent }
    )
    expectVersions(mojangProvidedLibs)
}

tasks.named("check") {
    dependsOn(verifyMinecraftLibraries)
}
