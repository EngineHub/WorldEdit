import org.gradle.api.Project

object Versions {
    const val KYORI_ADVENTURE = "4.14.0"
    const val KYORI_PLATFORM_BUKKIT = "4.3.1"
    const val PISTON = "0.6.0-SNAPSHOT"
    const val AUTO_VALUE = "1.9"
    const val JUNIT = "5.8.1"
    const val MOCKITO = "4.3.1"
    const val FAST_UTIL = "8.5.9"
    const val GUAVA = "31.1-jre"
    const val GSON = "2.10"
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
