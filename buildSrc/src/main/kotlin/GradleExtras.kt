import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.the

val Project.ext: ExtraPropertiesExtension
    get() = extensions.getByType()

val Project.sourceSets: SourceSetContainer
    get() = the<JavaPluginConvention>().sourceSets
