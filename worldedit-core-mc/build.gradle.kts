import buildlogic.CuiProtocolCommonIsNotFabricSpecificRule
import buildlogic.addEngineHubRepository

plugins {
    alias(libs.plugins.fabric.loom)
    `java-library`
    id("buildlogic.core-and-platform")
    id("buildlogic.expose-resources")
}

description = "The Minecraft-specific implementation of WorldEdit's core module." +
        " This code is shared by all platforms that rely on access without an API."

repositories {
    addEngineHubRepository()
}

dependencies {
    api(project(":worldedit-core"))
    api(project(":worldedit-libs:core-mc"))

    minecraft(libs.fabric.minecraft)

    // Provided by platforms.
    compileOnly(libs.fabric.mixin)

    implementation(libs.cuiProtocol.common)
    components {
        withModule<CuiProtocolCommonIsNotFabricSpecificRule>(libs.cuiProtocol.common.get().module)
    }

    // Silence some warnings, since apparently this isn't on the compile classpath like it should be.
    compileOnly(libs.errorprone.annotations)
}

base {
    archivesName.set("${project.name}-mc${libs.fabric.minecraft.get().version}")
}

publishing {
    publications.named<MavenPublication>("maven") {
        artifactId = base.archivesName.get()
        from(components["java"])
    }
}
