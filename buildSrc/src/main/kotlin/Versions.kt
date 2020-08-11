import org.gradle.api.Project

object Versions {
    const val ADVENTURE = "4.0.0-SNAPSHOT"
    const val ADVENTURE_EXTRAS = "4.0.0-SNAPSHOT"
    const val PISTON = "0.5.6"
    const val AUTO_VALUE = "1.7"
    const val JUNIT = "5.6.1"
    const val MOCKITO = "3.3.3"
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
