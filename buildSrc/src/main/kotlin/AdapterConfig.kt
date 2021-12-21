import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

// For specific version pinning, see
// https://papermc.io/repo/service/rest/repository/browse/maven-public/io/papermc/paper/dev-bundle/
fun Project.applyPaperweightAdapterConfiguration(javaRelease: Int = 17) {
    applyCommonConfiguration()
    apply(plugin = "java-library")
    applyCommonJavaConfiguration(
        sourcesJar = true,
        javaRelease = javaRelease,
        banSlf4j = false,
    )
    apply(plugin = "io.papermc.paperweight.userdev")

    dependencies {
        "implementation"(project(":worldedit-bukkit"))
    }

    tasks.named("assemble") {
        dependsOn("reobfJar")
    }
}
