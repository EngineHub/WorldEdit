import org.gradle.api.Project

object Versions {
    const val TEXT = "3.0.4"
    const val TEXT_EXTRAS = "3.0.6"
    const val PISTON = "0.5.8"
    const val AUTO_VALUE = "1.10.4"
    const val JUNIT = "5.10.2"
    const val MOCKITO = "5.11.0"
    const val FAST_UTIL = "8.5.12"
    const val GUAVA = "32.1.3-jre"
    const val GSON = "2.10.1"
    const val LOG4J = "2.19.0"
    const val LIN_BUS = "0.1.0-SNAPSHOT"
}

// Properties that need a project reference to resolve:
class ProjectVersions(project: Project) {
    val loom = project.rootProject.property("loom.version")
    val mixin = project.rootProject.property("mixin.version")
}

val Project.versions
    get() = ProjectVersions(this)
