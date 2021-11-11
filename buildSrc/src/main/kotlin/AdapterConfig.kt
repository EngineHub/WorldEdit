import io.papermc.paperweight.util.constants.REOBF_CONFIG
import io.papermc.paperweight.userdev.attribute.Obfuscation
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named

// For specific version pinning, see
// https://papermc.io/repo/service/rest/repository/browse/maven-public/io/papermc/paper/dev-bundle/
fun Project.applyPaperweightAdapterConfiguration(
    paperVersion: String
) {
    applyCommonConfiguration()
    apply(plugin = "java-library")
    applyCommonJavaConfiguration(
        sourcesJar = true,
        banSlf4j = false,
    )
    apply(plugin = "io.papermc.paperweight.userdev")

    dependencies {
        paperDevBundle(paperVersion)
        "implementation"(project(":worldedit-bukkit"))
    }

    tasks.named("assemble") {
        dependsOn("reobfJar")
    }
}
