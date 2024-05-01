plugins {
    id("com.jfrog.artifactory")
}

// Artifactory eagerly evaluates publications, so this must run after all changes to artifacts are done
afterEvaluate {
    tasks.named<org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask>("artifactoryPublish") {
        publications("maven")
    }
}
