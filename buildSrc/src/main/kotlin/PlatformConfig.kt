import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

fun Project.applyPlatformAndCoreConfiguration() {
    applyCommonConfiguration()
    apply(plugin = "java")
    apply(plugin = "eclipse")
    apply(plugin = "idea")
    apply(plugin = "maven")
    apply(plugin = "checkstyle")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "com.jfrog.artifactory")

    ext["internalVersion"] = "$version+${rootProject.ext["gitCommitHash"]}"

    tasks
        .withType<JavaCompile>()
        .matching { it.name == "compileJava" || it.name == "compileTestJava" }
        .configureEach {
            val disabledLint = listOf(
                "processing", "path", "fallthrough", "serial"
            )
            options.compilerArgs.addAll(listOf("-Xlint:all") + disabledLint.map { "-Xlint:-$it" })
            options.isDeprecation = true
            options.encoding = "UTF-8"
            options.compilerArgs.add("-parameters")
        }

    configure<CheckstyleExtension> {
        configFile = rootProject.file("config/checkstyle/checkstyle.xml")
        toolVersion = "8.34"
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    dependencies {
        "testImplementation"("org.junit.jupiter:junit-jupiter-api:${Versions.JUNIT}")
        "testImplementation"("org.junit.jupiter:junit-jupiter-params:${Versions.JUNIT}")
        "testImplementation"("org.mockito:mockito-core:${Versions.MOCKITO}")
        "testImplementation"("org.mockito:mockito-junit-jupiter:${Versions.MOCKITO}")
        "testRuntime"("org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT}")
    }

    // Java 8 turns on doclint which we fail
    tasks.withType<Javadoc>().configureEach {
        (options as StandardJavadocDocletOptions).apply {
            addStringOption("Xdoclint:none", "-quiet")
            tags(
                "apiNote:a:API Note:",
                "implSpec:a:Implementation Requirements:",
                "implNote:a:Implementation Note:"
            )
        }
    }

    tasks.register<Jar>("javadocJar") {
        dependsOn("javadoc")
        archiveClassifier.set("javadoc")
        from(tasks.getByName<Javadoc>("javadoc").destinationDir)
    }

    tasks.named("assemble").configure {
        dependsOn("javadocJar")
    }

    artifacts {
        add("archives", tasks.named("jar"))
        add("archives", tasks.named("javadocJar"))
    }

    if (name == "worldedit-core" || name == "worldedit-bukkit") {
        tasks.register<Jar>("sourcesJar") {
            dependsOn("classes")
            archiveClassifier.set("sources")
            from(sourceSets["main"].allSource)
        }

        artifacts {
            add("archives", tasks.named("sourcesJar"))
        }
        tasks.named("assemble").configure {
            dependsOn("sourcesJar")
        }
    }

    tasks.named("check").configure {
        dependsOn("checkstyleMain", "checkstyleTest")
    }

    applyCommonArtifactoryConfig()
}

fun Project.applyShadowConfiguration() {
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

fun Project.addJarManifest(kind: WorldEditKind, includeClasspath: Boolean = false) {
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
        manifest.attributes(attributes)
    }
}
