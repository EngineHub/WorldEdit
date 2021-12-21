import org.gradle.api.Project

object Versions {
    const val TEXT = "3.0.4"
    const val TEXT_EXTRAS = "3.0.6"
    const val PISTON = "0.5.7"
    const val AUTO_VALUE = "1.8.2"
    const val JUNIT = "5.8.1"
    const val MOCKITO = "4.0.0"
    const val FAST_UTIL = "8.5.6"
    const val GUAVA = "31.0.1-jre"
    const val GSON = "2.8.8"
    const val LOG4J = "2.15.0"
}

// Properties that need a project reference to resolve:
class ProjectVersions(project: Project) {
    val loom = project.rootProject.property("loom.version")
    val mixin = project.rootProject.property("mixin.version")
}

val Project.versions
    get() = ProjectVersions(this)
