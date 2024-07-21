import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.logging.Logging

// The primary point of this is repository up-time. We replace most other repositories with EngineHub's repository.
// This is because we have stronger up-time guarantees for our repository. However, Maven Central and Sonatype are
// clearly even better, so we allow those as well. We also allow Gradle's plugin repository.
private val ALLOWED_PREFIXES = listOf(
    "https://maven.enginehub.org",
    "https://repo.maven.apache.org/maven2/",
    "https://s01.oss.sonatype.org/content/repositories/snapshots/",
    "https://plugins.gradle.org",
    "file:"
)
private val LOGGER = Logging.getLogger("repositoriesHelper")

fun RepositoryHandler.killNonEngineHubRepositories() {
    val toRemove = mutableListOf<MavenArtifactRepository>()
    for (repo in this) {
        if (repo is MavenArtifactRepository && !ALLOWED_PREFIXES.any { repo.url.toString().startsWith(it) }) {
            LOGGER.info("Removing non-EngineHub repository: {}", repo.url)
            toRemove.add(repo)
        }
    }
    toRemove.forEach { remove(it) }
}

fun RepositoryHandler.verifyEngineHubRepositories() {
    for (repo in this) {
        if (repo is MavenArtifactRepository) {
            val urlString = repo.url.toString()
            check(ALLOWED_PREFIXES.any { urlString.startsWith(it) }) {
                "Only EngineHub/Central repositories are allowed: ${repo.url} found"
            }
        }
    }
}
