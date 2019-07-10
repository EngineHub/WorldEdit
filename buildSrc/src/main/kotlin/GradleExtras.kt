import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.kotlin.dsl.getByType

val Project.ext: ExtraPropertiesExtension
    get() = extensions.getByType()
