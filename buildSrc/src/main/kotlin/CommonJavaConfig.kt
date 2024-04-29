import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType

fun Project.applyCommonJavaConfiguration(sourcesJar: Boolean, javaRelease: Int = 17, banSlf4j: Boolean = true) {
    applyCommonConfiguration()
    apply(plugin = "eclipse")
    apply(plugin = "idea")
    apply(plugin = "checkstyle")

    tasks
        .withType<JavaCompile>()
        .matching { it.name == "compileJava" || it.name == "compileTestJava" }
        .configureEach {
            // TODO: re-enable this-escape when ANTLR suppresses it properly
            val disabledLint = listOf(
                "processing", "path", "fallthrough", "serial", "overloads", "this-escape",
            )
            options.release.set(javaRelease)
            options.compilerArgs.addAll(listOf("-Xlint:all") + disabledLint.map { "-Xlint:-$it" })
            options.isDeprecation = true
            options.encoding = "UTF-8"
            options.compilerArgs.add("-parameters")
            options.compilerArgs.add("-Werror")
        }

    configure<CheckstyleExtension> {
        configFile = rootProject.file("config/checkstyle/checkstyle.xml")
        toolVersion = "9.1"
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    dependencies {
        "compileOnly"(stringyLibs.getLibrary("jsr305"))
        "testImplementation"(platform(stringyLibs.getLibrary("junit-bom")))
        "testImplementation"(stringyLibs.getLibrary("junit-jupiter-api"))
        "testImplementation"(stringyLibs.getLibrary("junit-jupiter-params"))
        "testImplementation"(platform(stringyLibs.getLibrary("mockito-bom")))
        "testImplementation"(stringyLibs.getLibrary("mockito-core"))
        "testImplementation"(stringyLibs.getLibrary("mockito-junit-jupiter"))
        "testRuntimeOnly"(stringyLibs.getLibrary("junit-jupiter-engine"))
    }

    // Java 8 turns on doclint which we fail
    tasks.withType<Javadoc>().configureEach {
        options.encoding = "UTF-8"
        (options as StandardJavadocDocletOptions).apply {
            addBooleanOption("Werror", true)
            addBooleanOption("Xdoclint:all", true)
            addBooleanOption("Xdoclint:-missing", true)
            tags(
                "apiNote:a:API Note:",
                "implSpec:a:Implementation Requirements:",
                "implNote:a:Implementation Note:"
            )
        }
    }

    configure<JavaPluginExtension> {
        withJavadocJar()
        if (sourcesJar) {
            withSourcesJar()
        }
    }

    if (banSlf4j) {
        configurations["compileClasspath"].apply {
            resolutionStrategy.componentSelection {
                withModule("org.slf4j:slf4j-api") {
                    reject("No SLF4J allowed on compile classpath")
                }
            }
        }
    }

    tasks.named("check").configure {
        dependsOn("checkstyleMain", "checkstyleTest")
    }
}
