import groovy.lang.Closure
import org.cadixdev.gradle.licenser.LicenseExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.ide.idea.model.IdeaModel
import java.util.concurrent.TimeUnit

fun Project.applyCommonConfiguration() {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral {
            mavenContent {
                releasesOnly()
            }
        }
        maven { url = uri("https://maven.enginehub.org/repo/") }
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
            mavenContent {
                snapshotsOnly()
            }
        }
    }

    configurations.all {
        resolutionStrategy {
            cacheChangingModulesFor(1, TimeUnit.DAYS)
        }
    }

    plugins.withId("java") {
        the<JavaPluginExtension>().toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    dependencies {
        for (conf in listOf("implementation", "api")) {
            if (!configurations.names.contains(conf)) {
                continue
            }
            add(conf, enforcedPlatform(stringyLibs.getLibrary("log4j-bom")).map {
                val dep = create(it)
                dep.because("Mojang provides Log4j")
                dep
            })
            constraints {
                add(conf, stringyLibs.getLibrary("guava")) {
                    because("Mojang provides Guava")
                }
                add(conf, stringyLibs.getLibrary("gson")) {
                    because("Mojang provides Gson")
                }
                add(conf, stringyLibs.getLibrary("fastutil")) {
                    because("Mojang provides FastUtil")
                }
            }
        }
    }

    apply(plugin = "org.cadixdev.licenser")
    configure<LicenseExtension> {
        header(rootProject.file("HEADER.txt"))
        include("**/*.java")
        include("**/*.kt")
    }

    plugins.withId("idea") {
        configure<IdeaModel> {
            module {
                isDownloadSources = true
                isDownloadJavadoc = true
            }
        }
    }
}
