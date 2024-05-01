plugins {
    id("java")
    id("maven-publish")
    id("buildlogic.common-java")
    id("buildlogic.artifactory-sub")
}

ext["internalVersion"] = "$version+${rootProject.ext["gitCommitHash"]}"

publishing {
    publications {
        register<MavenPublication>("maven") {
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
        }
    }
}
