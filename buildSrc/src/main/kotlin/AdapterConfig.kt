import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

// For specific version pinning, see
// https://repo.papermc.io/service/rest/repository/browse/maven-public/io/papermc/paper/dev-bundle/
fun Project.applyPaperweightAdapterConfiguration() {
    applyCommonConfiguration()
    apply(plugin = "java-library")
    applyCommonJavaConfiguration(
        sourcesJar = true,
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
