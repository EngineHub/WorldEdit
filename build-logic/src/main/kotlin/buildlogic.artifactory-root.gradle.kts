import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention
import org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask

plugins {
    id("com.jfrog.artifactory")
}

val ARTIFACTORY_CONTEXT_URL = "artifactory_contextUrl"
val ARTIFACTORY_USER = "artifactory_user"
val ARTIFACTORY_PASSWORD = "artifactory_password"

if (!project.hasProperty(ARTIFACTORY_CONTEXT_URL)) ext[ARTIFACTORY_CONTEXT_URL] = "http://localhost"
if (!project.hasProperty(ARTIFACTORY_USER)) ext[ARTIFACTORY_USER] = "guest"
if (!project.hasProperty(ARTIFACTORY_PASSWORD)) ext[ARTIFACTORY_PASSWORD] = ""

configure<ArtifactoryPluginConvention> {
    setContextUrl("${project.property(ARTIFACTORY_CONTEXT_URL)}")
    clientConfig.publisher.run {
        repoKey = when {
            "${project.version}".contains("SNAPSHOT") -> "libs-snapshot-local"
            else -> "libs-release-local"
        }
        username = "${project.property(ARTIFACTORY_USER)}"
        password = "${project.property(ARTIFACTORY_PASSWORD)}"
        isMaven = true
        isIvy = false
    }
}

tasks.named<ArtifactoryTask>("artifactoryPublish") {
    isSkip = true
}
