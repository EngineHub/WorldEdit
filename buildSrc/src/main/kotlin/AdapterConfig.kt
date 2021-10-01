import io.papermc.paperweight.util.constants.REOBF_CONFIG
import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.attributes.Attribute
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named

// TODO https://github.com/PaperMC/paperweight/pull/87
interface Obfuscation : Named {
    companion object {
        val OBFUSCATION_ATTRIBUTE = Attribute.of(
            "org.enginehub.obfuscation",
            Obfuscation::class.java
        )
        const val NONE = "none"
        const val REOBFUSCATED = "reobfuscated"
    }
}

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

    configurations[REOBF_CONFIG].attributes {
        attribute(Obfuscation.OBFUSCATION_ATTRIBUTE, objects.named(Obfuscation.REOBFUSCATED))
    }
}
