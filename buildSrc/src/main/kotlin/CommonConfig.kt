import org.cadixdev.gradle.licenser.LicenseExtension
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.the

fun Project.applyCommonConfiguration() {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
        maven { url = uri("https://maven.enginehub.org/repo/") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
    }

    configurations.all {
        resolutionStrategy {
            cacheChangingModulesFor(5, "MINUTES")
        }
    }

    configurations.findByName("compileClasspath")?.apply {
        resolutionStrategy.componentSelection {
            withModule("org.slf4j:slf4j-api") {
                reject("No SLF4J allowed on compile classpath")
            }
        }
    }

    plugins.withId("java") {
        the<JavaPluginExtension>().toolchain {
            languageVersion.set(JavaLanguageVersion.of(8))
        }
    }

    dependencies {
        constraints {
            for (conf in configurations) {
                if (conf.isCanBeConsumed || conf.isCanBeResolved) {
                    // dependencies don't get declared in these
                    continue
                }
                add(conf.name, "com.google.guava:guava") {
                    version { strictly(Versions.GUAVA) }
                    because("Mojang provides Guava")
                }
                add(conf.name, "com.google.code.gson:gson") {
                    version { strictly(Versions.GSON) }
                    because("Mojang provides Gson")
                }
                add(conf.name, "it.unimi.dsi:fastutil") {
                    version { strictly(Versions.FAST_UTIL) }
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
}
