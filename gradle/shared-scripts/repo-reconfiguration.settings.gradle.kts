// This file is responsible for reconfiguring repositories to use EngineHub's mirrors
// In addition, it configures content filtering to speed up dependency resolution

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.UrlArtifactRepository
import org.gradle.api.logging.Logging
import java.net.URI

data class RepositoryReconfiguration(
    val newUri: URI,
    val contentConfiguration: (MavenRepositoryContentDescriptor.() -> Unit)? = null,
) {
    constructor(newUri: String, contentConfiguration: (MavenRepositoryContentDescriptor.() -> Unit)? = null) :
            this(URI.create(newUri), contentConfiguration)
}

data class ModuleDeclaration(
    val group: String,
    val name: String,
    val version: String? = null,
)

// Isolate everything from the buildscript so it's serializable
object Isolated {
    private val ALLOWED_PREFIXES = listOf(
        "https://repo.enginehub.org",
        "file:",
    )

    // Until https://github.com/gradle/gradle/issues/31888 is fixed in 9.4.0
    // we need to explicitly exclude unique snapshots
    private val UNIQUE_SNAPSHOT_MODULES = listOf(
        ModuleDeclaration("org.spongepowered", "spongeapi"),
        ModuleDeclaration("org.spongepowered", "vanillagradle"),
        ModuleDeclaration("me.lucko", "spark-api"),
    )

    private fun MavenRepositoryContentDescriptor.enhancedReleasesOnly() {
        releasesOnly()
        UNIQUE_SNAPSHOT_MODULES.forEach {
            if (it.version != null) {
                excludeVersion(it.group, it.name, it.version)
            } else {
                excludeModule(it.group, it.name)
            }
        }
    }

    private val REPO_RECONFIGURATIONS = listOf(
        "https://repo.maven.apache.org/maven2/" to
                RepositoryReconfiguration("https://repo.enginehub.org/internal/maven-central-proxy/") {
                    enhancedReleasesOnly()
                },
        "https://plugins.gradle.org/m2" to
                RepositoryReconfiguration("https://repo.enginehub.org/internal/plugin-portal-proxy/") {
                    enhancedReleasesOnly()
                },
        "https://libraries.minecraft.net/" to
                RepositoryReconfiguration("https://repo.enginehub.org/internal/minecraft/") {
                    enhancedReleasesOnly()
                },
        "https://maven.neoforged.net/releases/" to
                RepositoryReconfiguration("https://repo.enginehub.org/internal/neoforged/") {
                    enhancedReleasesOnly()
                    includeGroupAndSubgroups("net.minecraftforge")
                    includeGroupAndSubgroups("net.neoforged")
                },
        "https://maven.minecraftforge.net/" to
                RepositoryReconfiguration("https://repo.enginehub.org/internal/forge/") {
                    enhancedReleasesOnly()
                    includeGroupAndSubgroups("net.minecraftforge")
                },
        "https://maven.parchmentmc.org/" to
                RepositoryReconfiguration("https://repo.enginehub.org/internal/parchment/") {
                    enhancedReleasesOnly()
                    includeGroup("org.parchmentmc.data")
                },
        "https://repo.papermc.io/repository/maven-public/" to
                RepositoryReconfiguration("https://repo.enginehub.org/internal/papermc-proxy/") {
                    includeGroupAndSubgroups("io.papermc")
                    includeGroupAndSubgroups("com.velocitypowered")
                    includeGroupAndSubgroups("ca.spottedleaf")
                    includeGroupAndSubgroups("me.lucko")
                    includeModule("net.md-5", "bungeecord-chat")
                },
        "https://maven.fabricmc.net/" to
                RepositoryReconfiguration("https://repo.enginehub.org/internal/fabricmc/") {
                    enhancedReleasesOnly()
                    includeGroupAndSubgroups("fabric-loom")
                    includeGroupAndSubgroups("net.fabricmc")
                    excludeModule("net.fabricmc", "yarn")
                },
        "https://maven.fabricmc.net/#yarn-only" to
                RepositoryReconfiguration("https://repo.enginehub.org/internal/fabricmc-yarn/") {
                    enhancedReleasesOnly()
                    includeModule("net.fabricmc", "yarn")
                },
        "https://repo.spongepowered.org/repository/maven-releases/" to
                RepositoryReconfiguration("https://repo.enginehub.org/internal/spongepowered-releases/") {
                    enhancedReleasesOnly()
                    includeGroupAndSubgroups("org.spongepowered")
                },
        "https://repo.spongepowered.org/repository/maven-snapshots/" to
                RepositoryReconfiguration("https://repo.enginehub.org/internal/spongepowered-snapshots/") {
                    // Cannot actually call snapshotsOnly() because it excludes unique snapshots right now
                    includeGroupAndSubgroups("org.spongepowered")
                },
        "https://repo.enginehub.org/libs-release/" to
                RepositoryReconfiguration("https://repo.enginehub.org/libs-release/") {
                    enhancedReleasesOnly()
                    includeGroupAndSubgroups("com.sk89q")
                    includeGroupAndSubgroups("org.enginehub")
                },
    ).associate { (k, v) -> URI.create(k) to v }
    private val LOGGER = Logging.getLogger("enginehub-reconfiguring-repositories")

    fun RepositoryHandler.mirrorNonEngineHubRepositories() {
        configureEach {
            val repo = this
            if (!(repo is UrlArtifactRepository)) {
                return@configureEach
            }
            val reconfiguration = REPO_RECONFIGURATIONS[repo.url]
            val mustReplaceUrl = !ALLOWED_PREFIXES.any { repo.url.toString().startsWith(it) }
            if (mustReplaceUrl) {
                check(reconfiguration != null) {
                    "No replacement found for non-EngineHub repository: ${repo.name} ${repo.url}"
                }
                LOGGER.info(
                    "Replacing non-EngineHub repository: {} {} -> {}",
                    repo.name,
                    repo.url,
                    reconfiguration.newUri
                )
                repo.url = reconfiguration.newUri
            }
            if (reconfiguration?.contentConfiguration != null) {
                if (!(repo is MavenArtifactRepository)) {
                    error("Cannot configure content on non-Maven repository: ${repo.name} ${repo.url}")
                }
                repo.mavenContent {
                    reconfiguration.contentConfiguration.invoke(this)
                }
            }
        }
    }
}

with(Isolated) {
    gradle.lifecycle.beforeProject {
        buildscript.repositories.mirrorNonEngineHubRepositories()
        repositories.mirrorNonEngineHubRepositories()
    }

    settings.buildscript.repositories.mirrorNonEngineHubRepositories()
    settings.pluginManagement.repositories.mirrorNonEngineHubRepositories()
    settings.dependencyResolutionManagement.repositories.mirrorNonEngineHubRepositories()
}
