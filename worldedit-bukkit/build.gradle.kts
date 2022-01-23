import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.papermc.paperweight.userdev.attribute.Obfuscation

plugins {
    `java-library`
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

repositories {
    maven { url = uri("https://hub.spigotmc.org/nexus/content/groups/public") }
    maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
}

val localImplementation = configurations.create("localImplementation") {
    description = "Dependencies used locally, but provided by the runtime Bukkit implementation"
    isCanBeConsumed = false
    isCanBeResolved = false
}
configurations["compileOnly"].extendsFrom(localImplementation)
configurations["testImplementation"].extendsFrom(localImplementation)

val adapters = configurations.create("adapters") {
    description = "Adapters to include in the JAR"
    isCanBeConsumed = false
    isCanBeResolved = true
    shouldResolveConsistentlyWith(configurations["runtimeClasspath"])
    attributes {
        attribute(Obfuscation.OBFUSCATION_ATTRIBUTE, objects.named(Obfuscation.OBFUSCATED))
    }
}

dependencies {
    "api"(project(":worldedit-core"))
    "api"(project(":worldedit-libs:bukkit"))
    // Technically this is api, but everyone should already have some form of the bukkit API
    // Avoid pulling in another one, especially one so outdated.
    "localImplementation"("org.spigotmc:spigot-api:1.17-R0.1-SNAPSHOT") {
        exclude("junit", "junit")
    }

    "localImplementation"(platform("org.apache.logging.log4j:log4j-bom:${Versions.LOG4J}") {
        because("Spigot provides Log4J (sort of, not in API, implicitly part of server)")
    })
    "localImplementation"("org.apache.logging.log4j:log4j-api")

    "compileOnly"("org.jetbrains:annotations:20.1.0")
    "compileOnly"("io.papermc.paper:paper-api:1.17-R0.1-SNAPSHOT") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    "implementation"("io.papermc:paperlib:1.0.7")
    "compileOnly"("com.sk89q:dummypermscompat:1.10")
    "implementation"("org.bstats:bstats-bukkit:2.1.0")
    "implementation"("it.unimi.dsi:fastutil")
    "testImplementation"("org.mockito:mockito-core:1.9.0-rc1")

    project.project(":worldedit-bukkit:adapters").subprojects.forEach {
        "adapters"(project(it.path))
    }
}

tasks.named<Copy>("processResources") {
    val internalVersion = project.ext["internalVersion"]
    inputs.property("internalVersion", internalVersion)
    filesMatching("plugin.yml") {
        expand("internalVersion" to internalVersion)
    }
}

addJarManifest(WorldEditKind.Plugin, includeClasspath = true)

tasks.named<ShadowJar>("shadowJar") {
    dependsOn(project.project(":worldedit-bukkit:adapters").subprojects.map { it.tasks.named("assemble") })
    from(Callable {
        adapters.resolve()
            .map { f ->
                zipTree(f).matching {
                    exclude("META-INF/")
                }
            }
    })
    dependencies {
        // In tandem with not bundling log4j, we shouldn't relocate base package here.
        // relocate("org.apache.logging", "com.sk89q.worldedit.log4j")
        relocate("org.antlr.v4", "com.sk89q.worldedit.antlr4")
        include(dependency(":worldedit-core"))
        // Purposefully not included, we assume (even though no API exposes it) that Log4J will be present at runtime
        // If it turns out not to be true for Spigot/Paper, our only two official platforms, this can be uncommented.
        // include(dependency("org.apache.logging.log4j:log4j-api"))
        include(dependency("org.antlr:antlr4-runtime"))
        relocate("org.bstats", "com.sk89q.worldedit.bstats") {
            include(dependency("org.bstats:"))
        }
        relocate("io.papermc.lib", "com.sk89q.worldedit.bukkit.paperlib") {
            include(dependency("io.papermc:paperlib"))
        }
        relocate("it.unimi.dsi.fastutil", "com.sk89q.worldedit.bukkit.fastutil") {
            include(dependency("it.unimi.dsi:fastutil"))
        }
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}

configure<PublishingExtension> {
    publications.named<MavenPublication>("maven") {
        from(components["java"])
    }
}
