import net.minecrell.gradle.licenser.LicenseExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.repositories

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

    apply(plugin = "net.minecrell.licenser")
    configure<LicenseExtension> {
        header = rootProject.file("HEADER.txt")
        include("**/*.java")
        include("**/*.kt")
    }
}
