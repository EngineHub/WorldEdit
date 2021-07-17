import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.LoomGradleExtension
import net.fabricmc.loom.task.RemapJarTask

buildscript {
    repositories {
        mavenCentral()
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
    }
    dependencies {
        classpath("net.fabricmc:fabric-loom:${versions.loom}")
    }
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

apply(plugin = "fabric-loom")
apply(plugin = "java-library")

configure<LoomGradleExtension> {
    accessWidener("src/main/resources/worldedit.accesswidener")
}

val minecraftVersion = "1.17.1"
val yarnMappings = "1.17.1+build.1:v2"
val loaderVersion = "0.11.6"

configurations.all {
    resolutionStrategy {
        force("com.google.guava:guava:21.0")
    }
}

val fabricApiConfiguration: Configuration = configurations.create("fabricApi")

repositories {
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
}

configurations.create("proguardOnly")

dependencies {
    "api"(project(":worldedit-core"))
    "implementation"(platform("org.apache.logging.log4j:log4j-bom:2.14.1") {
        because("Mojang provides Log4J at 2.14.1")
    })

    "minecraft"("com.mojang:minecraft:$minecraftVersion")
    "mappings"("net.fabricmc:yarn:$yarnMappings")
    "modImplementation"("net.fabricmc:fabric-loader:$loaderVersion")

    // [1] declare fabric-api dependency...
    "fabricApi"("net.fabricmc.fabric-api:fabric-api:0.36.1+1.17")

    // [2] Load the API dependencies from the fabric mod json...
    @Suppress("UNCHECKED_CAST")
    val fabricModJson = file("src/main/resources/fabric.mod.json").bufferedReader().use {
        groovy.json.JsonSlurper().parse(it) as Map<String, Map<String, *>>
    }
    val wantedDependencies = (fabricModJson["depends"] ?: error("no depends in fabric.mod.json")).keys
        .filter { it == "fabric-api-base" || it.contains(Regex("v\\d$")) }
        .map { "net.fabricmc.fabric-api:$it" }
    logger.lifecycle("Looking for these dependencies:")
    for (wantedDependency in wantedDependencies) {
        logger.lifecycle(wantedDependency)
    }
    // [3] and now we resolve it to pick out what we want :D
    val fabricApiDependencies = fabricApiConfiguration.incoming.resolutionResult.allDependencies
        .onEach {
            if (it is UnresolvedDependencyResult) {
                throw kotlin.IllegalStateException("Failed to resolve Fabric API", it.failure)
            }
        }
        .filterIsInstance<ResolvedDependencyResult>()
        // pick out transitive dependencies
        .flatMap {
            it.selected.dependencies
        }
        // grab the requested versions
        .map { it.requested }
        .filterIsInstance<ModuleComponentSelector>()
        // map to standard notation
        .associateByTo(
            mutableMapOf(),
            keySelector = { "${it.group}:${it.module}" },
            valueTransform = { "${it.group}:${it.module}:${it.version}" }
        )
    fabricApiDependencies.keys.retainAll(wantedDependencies)
    // sanity check
    for (wantedDep in wantedDependencies) {
        check(wantedDep in fabricApiDependencies) { "Fabric API library $wantedDep is missing!" }
    }

    fabricApiDependencies.values.forEach {
        "include"(it)
        "modImplementation"(it)
    }

    // No need for this at runtime
    "modCompileOnly"("me.lucko:fabric-permissions-api:0.1-SNAPSHOT")

    // Hook these up manually, because Fabric doesn't seem to quite do it properly.
    "compileOnly"("net.fabricmc:sponge-mixin:${project.versions.mixin}")
    "annotationProcessor"("net.fabricmc:sponge-mixin:${project.versions.mixin}")
    "annotationProcessor"("net.fabricmc:fabric-loom:${project.versions.loom}")

    "proguardOnly"("org.checkerframework:checker-qual:3.10.0")
}

configure<BasePluginConvention> {
    archivesBaseName = "$archivesBaseName-mc$minecraftVersion"
}
configure<PublishingExtension> {
    publications.named<MavenPublication>("maven") {
        artifactId = the<BasePluginConvention>().archivesBaseName
        artifact(tasks.named("jar")) {
            builtBy(tasks.named("remapJar"))
        }
        artifact(tasks.named("sourcesJar")) {
            builtBy(tasks.named("remapSourcesJar"))
        }
    }
}

tasks.named<Copy>("processResources") {
    // this will ensure that this task is redone when the versions change.
    inputs.property("version", project.ext["internalVersion"])
    filesMatching("fabric.mod.json") {
        this.expand("version" to project.ext["internalVersion"])
    }
}

addJarManifest(WorldEditKind.Mod, includeClasspath = true)

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("dist-dev")
    dependencies {
        relocate("org.antlr.v4", "com.sk89q.worldedit.antlr4")

        include(dependency("org.antlr:antlr4-runtime"))
    }
}

val proguardConfiguration = tasks.register("proguardConfiguration") {
    val output = project.layout.buildDirectory.file("proguard/config.pro")
    outputs.file(output)
    doLast {
        output.get().asFile.writeText("""
            -optimizationpasses 3
            -overloadaggressively
            -flattenpackagehierarchy worldedit

            -dontwarn java.lang.invoke.MethodHandle
            -dontwarn javax.inject.**

            -renamesourcefileattribute SourceFile
            -keepattributes Signature,Exceptions,*Annotation*,
                            InnerClasses,PermittedSubclasses,EnclosingMethod,
                            Deprecated,SourceFile,LineNumberTable

            -keep class com.sk89q.worldedit.fabric.FabricWorldEdit { !private *; }
            -keep class com.sk89q.worldedit.fabric.internal.MixinConfigPlugin { !private *; }
            -keep class com.sk89q.worldedit.fabric.mixin.** { *; }

            -keep class org.enginehub.piston.converter.Converter { !private *; }
            -keepclassmembers class org.enginehub.piston.converter.SuccessfulConversion { *** fromSingle(...); }
            -keepclassmembers class org.enginehub.piston.converter.FailedConversion { *** from(...); }

            -keepclassmembers class * { static ** REGISTRY; }

            -keepclassmembers @org.enginehub.piston.annotation.CommandContainer class * { !private *; }
            # Don't touch the special annotation hack methods
            -keepclassmembers class **Registration$* { java.lang.annotation.Annotation *(java.lang.Object); }
            # Keep event methods, but the names don't matter
            -keepclassmembers,allowobfuscation class * { @com.sk89q.worldedit.util.eventbus.Subscribe *; }
            # Allow legacy loading to work
            -keeppackagenames com.sk89q.worldedit.world.registry
            -keepclassmembers class com.sk89q.worldedit.world.registry.LegacyMapper${'$'}LegacyDataFile {
                *;
            }
            -keepclassmembers class com.sk89q.worldedit.world.registry.Bundled*Data${'$'}*Entry {
                *;
            }

            -keepclassmembers enum * {
                <fields>;
                static **[] values();
                static ** valueOf(java.lang.String);
            }
        """.trimIndent())
    }
}

val proguardJar = project.layout.buildDirectory.file("proguard-shadow-jar.jar")
tasks.register<proguard.gradle.ProGuardTask>("proguardShadowJar") {
    injars(tasks.named("shadowJar"))
    outjars(proguardJar)

    val javaHome = project.the<JavaToolchainService>().compilerFor(
        project.the<JavaPluginExtension>().toolchain
    ).get().metadata.installationPath.asFile
    for (bootMod in listOf("java.base", "java.desktop", "java.management", "java.logging", "java.scripting")) {
        libraryjars(
            mapOf("jarfilter" to "!**.jar", "filter" to "!module-info.class"),
            "$javaHome/jmods/$bootMod.jmod"
        )
    }
    libraryjars(
        project.files(
            configurations["compileClasspath"],
            configurations["proguardOnly"],
            project(":worldedit-core").configurations["compileClasspath"]
        )
    )

    printmapping(project.layout.buildDirectory.file("obf.map"))

    configuration(proguardConfiguration)
}

tasks.register<RemapJarTask>("remapShadowJar") {
    val shadowJar = tasks.getByName<ShadowJar>("shadowJar")
    dependsOn(tasks.named("proguardShadowJar"))
    input.set(proguardJar)
    archiveFileName.set(shadowJar.archiveFileName.get().replace(Regex("-dev\\.jar$"), ".jar"))
    addNestedDependencies.set(true)
    remapAccessWidener.set(true)
}

tasks.named("assemble").configure {
    dependsOn("remapShadowJar")
}

configure<PublishingExtension> {
    publications.named<MavenPublication>("maven") {
        // Remove when https://github.com/gradle/gradle/issues/16555 is fixed
        suppressPomMetadataWarningsFor("runtimeElements")
    }
}
