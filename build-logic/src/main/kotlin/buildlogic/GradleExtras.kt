package buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.VersionConstraint
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.enginehub.crankcase.git.GitExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.the
import java.net.URI

val Project.ext: ExtraPropertiesExtension
    get() = extensions.getByType()

val Project.stringyLibs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

val Project.internalVersion: Provider<String>
    get() {
        val version = version.toString()
        return the<GitExtension>().commitHash.map { "$version+$it" }
    }

fun VersionCatalog.getLibrary(name: String): Provider<MinimalExternalModuleDependency> = findLibrary(name).orElseThrow {
    error("Library $name not found in version catalog")
}

fun VersionCatalog.getVersion(name: String): VersionConstraint = findVersion(name).orElseThrow {
    error("Version $name not found in version catalog")
}

fun RepositoryHandler.addEngineHubRepository() {
    maven {
        name = "EngineHub (Non-Mirrored)"
        url = URI.create("https://repo.enginehub.org/libs-release/")
    }
}
