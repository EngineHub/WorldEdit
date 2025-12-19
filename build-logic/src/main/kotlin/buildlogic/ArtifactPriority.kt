package buildlogic

import org.gradle.api.Named
import org.gradle.api.attributes.Attribute

interface ArtifactPriority : Named {
    companion object {
        val ATTRIBUTE = Attribute.of(
            "org.enginehub.internal.artifact-priority",
            ArtifactPriority::class.java
        )

        const val PRIMARY = "primary"
    }
}
