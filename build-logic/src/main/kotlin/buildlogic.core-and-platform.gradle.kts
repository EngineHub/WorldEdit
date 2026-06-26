plugins {
    id("maven-publish")
    id("buildlogic.common-java-library")
    id("org.enginehub.crankcase.publishing")
}

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
