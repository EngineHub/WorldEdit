import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import kotlin.collections.set

fun Project.applyPlatformAndCoreConfiguration(javaRelease: Int = 17) {
    applyCommonConfiguration()
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "com.jfrog.artifactory")
    applyCommonJavaConfiguration(
        sourcesJar = name in setOf("worldedit-core", "worldedit-bukkit", "worldedit-fabric"),
        javaRelease = javaRelease,
        banSlf4j = name !in setOf("worldedit-fabric", "worldedit-forge"),
    )

    ext["internalVersion"] = "$version+${rootProject.ext["gitCommitHash"]}"

    configure<PublishingExtension> {
        publications {
            register<MavenPublication>("maven") {
                versionMapping {
                    usage("java-api") {
                        fromResolutionOf("runtimeClasspath")
                    }
                    usage("java-runtime") {
                        fromResolutionResult()
                    }
                }
            }
        }
    }

    applyCommonArtifactoryConfig()
}

fun Project.applyShadowConfiguration() {
    apply(plugin = "com.github.johnrengelman.shadow")
    tasks.named<ShadowJar>("shadowJar") {
        archiveClassifier.set("dist")
        dependencies {
            include(project(":worldedit-libs:core"))
            include(project(":worldedit-libs:${project.name.replace("worldedit-", "")}"))
            include(project(":worldedit-core"))
            exclude("com.google.code.findbugs:jsr305")
        }
        exclude("GradleStart**")
        exclude(".cache")
        exclude("LICENSE*")
        exclude("META-INF/maven/**")
        minimize()
    }
    val javaComponent = components["java"] as AdhocComponentWithVariants
    // I don't think we want this published (it's the shadow jar)
    javaComponent.withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) {
        skip()
    }
}

private val CLASSPATH = listOf("truezip", "truevfs", "js")
    .map { "$it.jar" }
    .flatMap { listOf(it, "WorldEdit/$it") }
    .joinToString(separator = " ")

sealed class WorldEditKind(
    val name: String,
    val mainClass: String = "com.sk89q.worldedit.internal.util.InfoEntryPoint"
) {
    class Standalone(mainClass: String) : WorldEditKind("STANDALONE", mainClass)
    object Mod : WorldEditKind("MOD")
    object Plugin : WorldEditKind("PLUGIN")
}

fun Project.addJarManifest(kind: WorldEditKind, includeClasspath: Boolean = false, extraAttributes: Map<String, String> = mapOf()) {
    tasks.named<Jar>("jar") {
        val version = project(":worldedit-core").version
        inputs.property("version", version)
        val attributes = mutableMapOf(
            "Implementation-Version" to version,
            "WorldEdit-Version" to version,
            "WorldEdit-Kind" to kind.name,
            "Main-Class" to kind.mainClass
        )
        if (includeClasspath) {
            attributes["Class-Path"] = CLASSPATH
        }
        attributes.putAll(extraAttributes)
        manifest.attributes(attributes)
    }
}
