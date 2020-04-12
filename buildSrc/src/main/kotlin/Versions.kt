import org.gradle.api.Project

object Versions {
    const val TEXT = "3.0.3"
    const val TEXT_EXTRAS = "3.0.3"
    const val PISTON = "0.5.5"
    const val AUTO_VALUE = "1.7"
    const val JUNIT = "5.6.1"
    const val MOCKITO = "3.3.3"
    const val LOGBACK = "1.2.3"
}

// Properties that need a project reference to resolve:
class ProjectVersions(project: Project) {
    val loom = project.rootProject.property("loom.version")
    val mixin = project.rootProject.property("mixin.version")
}

val Project.versions
    get() = ProjectVersions(this)
