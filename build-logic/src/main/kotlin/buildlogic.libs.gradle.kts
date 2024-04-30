plugins {
    id("java-base")
    id("maven-publish")
    id("com.github.johnrengelman.shadow")
    id("com.jfrog.artifactory")
    id("buildlogic.common")
    id("buildlogic.artifactory-sub")
}

// A horrible hack because `softwareComponentFactory` has to be gotten via plugin
// gradle why
internal open class LibsConfigPluginHack @Inject constructor(
    private val softwareComponentFactory: SoftwareComponentFactory
) : Plugin<Project> {
    override fun apply(project: Project) {
        val libsComponents = softwareComponentFactory.adhoc("libs")
        project.components.add(libsComponents)
    }
}

configurations {
    create("shade")
}

group = "${rootProject.group}.worldedit-libs"

val relocations = mapOf(
    "net.kyori.text" to "com.sk89q.worldedit.util.formatting.text",
    "net.kyori.minecraft" to "com.sk89q.worldedit.util.kyori",
)

tasks.register<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("jar") {
    configurations = listOf(project.configurations["shade"])
    archiveClassifier.set("")

    // Yeet module-info's
    exclude("module-info.class")

    dependencies {
        exclude(dependency("com.google.guava:guava"))
        exclude(dependency("com.google.code.gson:gson"))
        exclude(dependency("com.google.errorprone:error_prone_annotations"))
        exclude(dependency("com.google.guava:failureaccess"))
        exclude(dependency("org.checkerframework:checker-qual"))
        exclude(dependency("org.jetbrains:annotations"))
        exclude(dependency("org.apache.logging.log4j:log4j-api"))
        exclude(dependency("com.google.code.findbugs:jsr305"))
        exclude {
            it.moduleGroup == "org.jetbrains.kotlin"
        }
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
            val category = dependency.attributes.getAttribute(Category.CATEGORY_ATTRIBUTE)?.name
            if (category == Category.REGULAR_PLATFORM || category == Category.ENFORCED_PLATFORM) {
                return@map dependency
            }
            try {
                dependency.artifact {
                    name = dependency.name
                    type = artifactType
                    extension = "jar"
                    classifier = artifactType
                }
            } catch (e: Exception) {
                throw RuntimeException("Failed to add artifact to dependency: $dependency", e)
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

    // Yeet module-info's
    exclude("module-info.java")

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

project.apply<LibsConfigPluginHack>()

val libsComponent = project.components["libs"] as AdhocComponentWithVariants

val apiElements = project.configurations.register("apiElements") {
    isVisible = false
    description = "API elements for libs"
    isCanBeResolved = false
    isCanBeConsumed = true
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage.JAVA_API))
        attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category.LIBRARY))
        attribute(Bundling.BUNDLING_ATTRIBUTE, project.objects.named(Bundling.SHADOWED))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements.JAR))
        attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 16)
    }
    outgoing.artifact(tasks.named("jar"))
}

val runtimeElements = project.configurations.register("runtimeElements") {
    isVisible = false
    description = "Runtime elements for libs"
    isCanBeResolved = false
    isCanBeConsumed = true
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage.JAVA_RUNTIME))
        attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category.LIBRARY))
        attribute(Bundling.BUNDLING_ATTRIBUTE, project.objects.named(Bundling.SHADOWED))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements.JAR))
        attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 16)
    }
    outgoing.artifact(tasks.named("jar"))
}

val sourcesElements = project.configurations.register("sourcesElements") {
    isVisible = false
    description = "Source elements for libs"
    isCanBeResolved = false
    isCanBeConsumed = true
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage.JAVA_RUNTIME))
        attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category.DOCUMENTATION))
        attribute(Bundling.BUNDLING_ATTRIBUTE, project.objects.named(Bundling.SHADOWED))
        attribute(DocsType.DOCS_TYPE_ATTRIBUTE, project.objects.named(DocsType.SOURCES))
    }
    outgoing.artifact(tasks.named("sourcesJar"))
}

libsComponent.addVariantsFromConfiguration(apiElements.get()) {
    mapToMavenScope("compile")
}

libsComponent.addVariantsFromConfiguration(runtimeElements.get()) {
    mapToMavenScope("runtime")
}

libsComponent.addVariantsFromConfiguration(sourcesElements.get()) {
    mapToMavenScope("runtime")
}

configure<PublishingExtension> {
    publications {
        register<MavenPublication>("maven") {
            from(libsComponent)
        }
    }
}

if (project != project(":worldedit-libs:core")) {
    evaluationDependsOn(":worldedit-libs:core")
    configurations["shade"].shouldResolveConsistentlyWith(project(":worldedit-libs:core").configurations["shade"])
}
