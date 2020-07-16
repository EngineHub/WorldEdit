import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

dependencies {
    "api"(project(":worldedit-core"))
    "implementation"("org.apache.logging.log4j:log4j-core:2.8.1")
    "implementation"("org.apache.logging.log4j:log4j-slf4j-impl:2.8.1")
    "implementation"("commons-cli:commons-cli:1.4")
    "implementation"("com.google.guava:guava:${Versions.GUAVA}")
    "implementation"("com.google.code.gson:gson:${Versions.GSON}")
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(
                "Implementation-Version" to project.version,
                "Main-Class" to "com.sk89q.worldedit.cli.CLIWorldEdit"
        )
    }
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
