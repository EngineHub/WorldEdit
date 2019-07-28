import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("org.spongepowered.plugin")
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

repositories {
    maven { url = uri("https://repo.codemc.org/repository/maven-public") }
}

dependencies {
    compile(project(":worldedit-core"))
    compile(project(":worldedit-libs:sponge"))
    compile("org.spongepowered:spongeapi:7.1.0")
    compile("org.bstats:bstats-sponge:1.5")
    testCompile("org.mockito:mockito-core:1.9.0-rc1")
}

sponge {
    plugin {
        id = "worldedit"
    }
}

tasks.named<Jar>("jar") {
    manifest {
        attributes("Class-Path" to "truezip.jar WorldEdit/truezip.jar js.jar WorldEdit/js.jar",
                "WorldEdit-Version" to project.version)
    }
}

tasks.named<ShadowJar>("shadowJar") {
    dependencies {
        relocate ("org.bstats", "com.sk89q.worldedit.sponge.bstats") {
            include(dependency("org.bstats:bstats-sponge:1.5"))
        }
    }
}

if (project.hasProperty("signing")) {
    apply(plugin = "signing")

    configure<SigningExtension> {
        sign("shadowJar")
    }

    tasks.named("build").configure {
        dependsOn("signShadowJar")
    }
}