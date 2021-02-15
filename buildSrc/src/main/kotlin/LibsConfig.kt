import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.internal.HasConvention
import org.gradle.api.plugins.MavenRepositoryHandlerConvention
import org.gradle.api.tasks.Upload
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getPlugin
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register

fun Project.applyLibrariesConfiguration() {
    applyCommonConfiguration()
    apply(plugin = "java-base")
    apply(plugin = "maven")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "com.jfrog.artifactory")

    configurations {
        create("shade")
        getByName("archives").extendsFrom(getByName("default"))
    }

    group = "${rootProject.group}.worldedit-libs"

    val relocations = mapOf(
        "net.kyori.text" to "com.sk89q.worldedit.util.formatting.text",
        "net.kyori.minecraft" to "com.sk89q.worldedit.util.kyori",
        "net.kyori.adventure.nbt" to "com.sk89q.worldedit.util.nbt"
    )

    tasks.register<ShadowJar>("jar") {
        configurations = listOf(project.configurations["shade"])
        archiveClassifier.set("")

        dependencies {
            exclude(dependency("com.google.guava:guava"))
            exclude(dependency("com.google.code.gson:gson"))
            exclude(dependency("org.checkerframework:checker-qual"))
            exclude(dependency("org.slf4j:slf4j-api"))
        }

        relocations.forEach { (from, to) ->
            relocate(from, to)
        }
    }
    val altConfigFiles = { artifactType: String ->
        val deps = configurations["shade"].incoming.dependencies
            .filterIsInstance<ModuleDependency>()
            .map { it.copy() }
            .map { dependency ->
                dependency.artifact {
                    name = dependency.name
                    type = artifactType
                    extension = "jar"
                    classifier = artifactType
                }
                dependency
            }

        files(configurations.detachedConfiguration(*deps.toTypedArray())
            .resolvedConfiguration.lenientConfiguration.artifacts
            .filter { it.classifier == artifactType }
            .map { zipTree(it.file) })
    }
    tasks.register<Jar>("sourcesJar") {
        from({
            altConfigFiles("sources")
        })
        relocations.forEach { (from, to) ->
            val filePattern = Regex("(.*)${from.replace('.', '/')}((?:/|$).*)")
            val textPattern = Regex.fromLiteral(from)
            eachFile {
                filter {
                    it.replaceFirst(textPattern, to)
                }
                path = path.replaceFirst(filePattern, "$1${to.replace('.', '/')}$2")
            }
        }
        archiveClassifier.set("sources")
    }

    tasks.named("assemble").configure {
        dependsOn("jar", "sourcesJar")
    }

    artifacts {
        val jar = tasks.named("jar")
        add("default", jar) {
            builtBy(jar)
        }
        val sourcesJar = tasks.named("sourcesJar")
        add("archives", sourcesJar) {
            builtBy(sourcesJar)
        }
    }

    tasks.register<Upload>("install") {
        configuration = configurations["archives"]
        (repositories as HasConvention).convention.getPlugin<MavenRepositoryHandlerConvention>().mavenInstaller {
            pom.version = project.version.toString()
            pom.artifactId = project.name
        }
    }

    applyCommonArtifactoryConfig()
}

fun Project.constrainDependenciesToLibsCore() {
    evaluationDependsOn(":worldedit-libs:core")
    val coreDeps = project(":worldedit-libs:core").configurations["shade"].dependencies
        .filterIsInstance<ExternalModuleDependency>()
    dependencies.constraints {
        for (coreDep in coreDeps) {
            add("shade", "${coreDep.group}:${coreDep.name}:${coreDep.version}") {
                because("libs should align with libs:core")
            }
        }
    }
}
