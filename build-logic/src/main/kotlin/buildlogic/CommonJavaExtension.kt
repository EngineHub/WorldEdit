package buildlogic

import org.gradle.api.provider.Property

interface CommonJavaExtension {
    val banSlf4j: Property<Boolean>
}
