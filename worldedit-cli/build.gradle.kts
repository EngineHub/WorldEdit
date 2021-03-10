import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()
addJarManifest(WorldEditKind.Standalone("com.sk89q.worldedit.cli.CLIWorldEdit"))

dependencies {
    "api"(project(":worldedit-core"))
    "implementation"(platform("org.apache.logging.log4j:log4j-bom:2.14.0"))
    "implementation"("org.apache.logging.log4j:log4j-core")
    "implementation"("org.apache.logging.log4j:log4j-slf4j-impl")
    "implementation"("commons-cli:commons-cli:1.4")
    "implementation"("com.google.guava:guava")
    "implementation"("com.google.code.gson:gson")
}

tasks.named<ShadowJar>("shadowJar") {
    dependencies {
        include { true }
    }
    minimize {
        exclude(dependency("org.apache.logging.log4j:log4j-core"))
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
