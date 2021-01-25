import org.gradle.api.Project

object Versions {
    const val TEXT = "3.0.4"
    const val TEXT_EXTRAS = "3.0.6"
    const val PISTON = "0.5.6"
    const val AUTO_VALUE = "1.7.4"
    const val JUNIT = "5.7.0"
    const val MOCKITO = "3.7.7"
    const val SLF4J = "1.7.30"
    const val LOGBACK = "1.2.3"
    const val FAST_UTIL = "8.2.1"
    const val GUAVA = "21.0"
    const val GSON = "2.8.0"
}

// Properties that need a project reference to resolve:
class ProjectVersions(project: Project) {
    val loom = project.rootProject.property("loom.version")
    val mixin = project.rootProject.property("mixin.version")
}

val Project.versions
    get() = ProjectVersions(this)
