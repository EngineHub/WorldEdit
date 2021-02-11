import net.minecrell.gradle.licenser.LicenseExtension
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

    plugins.withId("java") {
        the<JavaPluginExtension>().toolchain {
            languageVersion.set(JavaLanguageVersion.of(8))
        }
    }

    dependencies {
        constraints {
            for (conf in configurations.names) {
                add(conf, "com.google.guava:guava") {
                    version { strictly(Versions.GUAVA) }
                    because("Mojang provides Guava")
                }
                add(conf, "com.google.code.gson:gson") {
                    version { strictly(Versions.GSON) }
                    because("Mojang provides Gson")
                }
                add(conf, "it.unimi.dsi:fastutil") {
                    version { strictly(Versions.FAST_UTIL) }
                    because("Mojang provides FastUtil")
                }
            }
        }
    }

    apply(plugin = "net.minecrell.licenser")
    configure<LicenseExtension> {
        header = rootProject.file("HEADER.txt")
        include("**/*.java")
        include("**/*.kt")
    }
}
