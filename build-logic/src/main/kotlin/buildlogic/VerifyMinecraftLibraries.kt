package buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class VerifyMinecraftLibraries : DefaultTask() {
    @get:Input
    abstract val modules: SetProperty<String>

    @get:Input
    abstract val declaredLibraries: Property<ResolvedComponentResult>

    @get:Input
    abstract val minecraftLibraries: Property<ResolvedComponentResult>

    fun expectVersions(dependencies: List<Provider<MinimalExternalModuleDependency>>) {
        for (dependency in dependencies) {
            modules.add(dependency.map { "${it.module.group}:${it.module.name}" })
        }
    }

    @TaskAction
    fun verify() {
        val declared = collectVersions(declaredLibraries.get())
        val minecraft = collectVersions(minecraftLibraries.get())

        val problems = modules.get().sorted().mapNotNull { module ->
            val ours = declared[module]
                ?: return@mapNotNull "$module: not resolved, so it cannot be checked against Minecraft"
            when (val theirs = minecraft[module]) {
                ours -> null
                null -> "$module: resolved $ours, but Minecraft does not provide this library"
                else -> "$module: resolved $ours, but Minecraft provides $theirs"
            }
        }

        if (problems.isNotEmpty()) {
            throw GradleException(
                problems.joinToString(
                    separator = "\n",
                    prefix = "Mojang-provided library versions do not match Minecraft:\n",
                    postfix = "\n\nUpdate gradle/libs.versions.toml to match.",
                ) { "  - $it" }
            )
        }
    }

    private fun collectVersions(root: ResolvedComponentResult): Map<String, String> {
        val versions = mutableMapOf<String, String>()
        val seen = mutableSetOf<ResolvedComponentResult>()
        val queue = ArrayDeque(listOf(root))
        while (queue.isNotEmpty()) {
            val component = queue.removeFirst()
            if (!seen.add(component)) {
                continue
            }
            component.moduleVersion?.let { versions["${it.group}:${it.name}"] = it.version }
            component.dependencies.filterIsInstance<ResolvedDependencyResult>()
                .mapTo(queue) { it.selected }
        }
        return versions
    }
}
