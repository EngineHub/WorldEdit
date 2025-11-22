package buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.VersionConstraint
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.registerIfAbsent

val Project.ext: ExtraPropertiesExtension
    get() = extensions.getByType()

val Project.stringyLibs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

val Project.internalVersion: Provider<String>
    get() = gradle.sharedServices.registerIfAbsent("git", GitBuildService::class) {}
        .flatMap { service -> service.computeInternalVersion(project.version as String) }

fun VersionCatalog.getLibrary(name: String): Provider<MinimalExternalModuleDependency> = findLibrary(name).orElseThrow {
    error("Library $name not found in version catalog")
}

fun VersionCatalog.getVersion(name: String): VersionConstraint = findVersion(name).orElseThrow {
    error("Version $name not found in version catalog")
}
