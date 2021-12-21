import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecraftforge.gradle.common.util.RunConfig
import net.minecraftforge.gradle.userdev.UserDevExtension
import net.minecraftforge.gradle.userdev.tasks.RenameJarInPlace

plugins {
    id("net.minecraftforge.gradle")
    `java-library`
}

applyPlatformAndCoreConfiguration(javaRelease = 17)
applyShadowConfiguration()

val minecraftVersion = "1.18.1"
val nextMajorMinecraftVersion: String = minecraftVersion.split('.').let { (useless, major) ->
    "$useless.${major.toInt() + 1}"
}
val forgeVersion = "39.0.0"

val apiClasspath = configurations.create("apiClasspath") {
    isCanBeResolved = true
    extendsFrom(configurations.api.get())
}

dependencies {
    "api"(project(":worldedit-core"))
    "implementation"(platform("org.apache.logging.log4j:log4j-bom:${Versions.LOG4J}") {
        because("Mojang provides Log4J")
    })

    "minecraft"("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")
}

configure<UserDevExtension> {
    mappings("official", minecraftVersion)

    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

    runs {
        val runConfig = Action<RunConfig> {
            properties(mapOf(
                "forge.logging.markers" to "SCAN,REGISTRIES,REGISTRYDUMP",
                "forge.logging.console.level" to "debug"
            ))
            workingDirectory = project.file("run").canonicalPath
            source(sourceSets["main"])
            lazyToken("minecraft_classpath") {
                apiClasspath.resolve().joinToString(File.pathSeparator) { it.absolutePath }
            }
        }
        create("client", runConfig)
        create("server", runConfig)
    }

}

configure<BasePluginConvention> {
    archivesBaseName = "$archivesBaseName-mc$minecraftVersion"
}

val javaComponent = components["java"] as AdhocComponentWithVariants
javaComponent.withVariantsFromConfiguration(configurations["apiElements"]) {
    skip()
}

javaComponent.withVariantsFromConfiguration(configurations["runtimeElements"]) {
    skip()
}

tasks.register<Jar>("deobfJar") {
    from(sourceSets["main"].output)
    archiveClassifier.set("dev")
}

val reobfApiElements = configurations.register("reobfApiElements") {
    isVisible = false
    description = "Re-obfuscated API elements for libs"
    isCanBeResolved = false
    isCanBeConsumed = true
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage.JAVA_API))
        attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category.LIBRARY))
        attribute(Bundling.BUNDLING_ATTRIBUTE, project.objects.named(Bundling.EXTERNAL))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements.JAR))
        attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
    }
    outgoing.artifact(tasks.named("jar")) {
        builtBy(project.provider { tasks.named("reobfJar") })
    }
    extendsFrom(configurations["api"])
}

javaComponent.addVariantsFromConfiguration(reobfApiElements.get()) {
    mapToMavenScope("compile")
}

val reobfRuntimeElements = configurations.register("reobfRuntimeElements") {
    isVisible = false
    description = "Re-obfuscated runtime elements for libs"
    isCanBeResolved = false
    isCanBeConsumed = true
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage.JAVA_RUNTIME))
        attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category.LIBRARY))
        attribute(Bundling.BUNDLING_ATTRIBUTE, project.objects.named(Bundling.EXTERNAL))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements.JAR))
        attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
    }
    outgoing.artifact(tasks.named("jar")) {
        builtBy(project.provider { tasks.named("reobfJar") })
    }
    extendsFrom(configurations["reobfApiElements"])
    extendsFrom(configurations["runtimeClasspath"].copy { d -> d.group != "net.minecraftforge" })
}

javaComponent.addVariantsFromConfiguration(reobfRuntimeElements.get()) {
    mapToMavenScope("runtime")
}

configure<PublishingExtension> {
    publications.named<MavenPublication>("maven") {
        artifactId = the<BasePluginConvention>().archivesBaseName
        from(components["java"])
    }
}

tasks.named<Copy>("processResources") {
    // this will ensure that this task is redone when the versions change.
    val properties = mapOf(
        "version" to project.ext["internalVersion"],
        "forgeVersion" to forgeVersion,
        "minecraftVersion" to minecraftVersion,
        "nextMajorMinecraftVersion" to nextMajorMinecraftVersion
    )
    properties.forEach { (key, value) ->
        inputs.property(key, value)
    }

    filesMatching("META-INF/mods.toml") {
        expand(properties)
    }

    // copy from -core resources as well
    from(project(":worldedit-core").tasks.named("processResources"))
}

addJarManifest(WorldEditKind.Mod, includeClasspath = false)

tasks.named<ShadowJar>("shadowJar") {
    dependencies {
        relocate("org.antlr.v4", "com.sk89q.worldedit.antlr4")

        include(dependency("org.antlr:antlr4-runtime"))
        include(dependency("org.mozilla:rhino-runtime"))
    }
    minimize {
        exclude(dependency("org.mozilla:rhino-runtime"))
    }
}

afterEvaluate {
    val reobf = extensions.getByName<NamedDomainObjectContainer<RenameJarInPlace>>("reobf")
    reobf.create("shadowJar")
}
