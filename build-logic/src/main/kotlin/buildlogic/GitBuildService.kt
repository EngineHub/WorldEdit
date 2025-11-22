package buildlogic

import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import javax.inject.Inject

/**
 * Git-related extension.
 */
internal abstract class GitBuildService @Inject constructor(
    private val providers: ProviderFactory,
) : BuildService<BuildServiceParameters.None> {
    private val gitCommitHash by lazy {
        val probedCommitHash = providers.exec {
            commandLine("git", "rev-parse", "--short", "HEAD")
        }.standardOutput.asText.map { it.trim() }

        providers.gradleProperty("gitCommitHash").orElse(probedCommitHash)
    }

    fun computeInternalVersion(version: String): Provider<String> =
        gitCommitHash.map { hash -> "$version+$hash" }
}
