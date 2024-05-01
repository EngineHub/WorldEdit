import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("buildlogic.platform")
}

platform {
    kind = buildlogic.WorldEditKind.Standalone("com.sk89q.worldedit.cli.CLIWorldEdit")
    extraAttributes = mapOf(
        // We don't have any multi-release stuff, but Log4J does.
        "Multi-Release" to "true",
    )
}

dependencies {
    "compileOnly"(project(":worldedit-libs:core:ap"))
    "annotationProcessor"(project(":worldedit-libs:core:ap"))
    "annotationProcessor"(libs.guava)
    "api"(project(":worldedit-core"))
    "implementation"(platform(libs.log4j.bom))
    "implementation"(libs.log4j.api)
    "implementation"(libs.log4j.core)
    "implementation"(libs.commonsCli)
    "implementation"(libs.guava)
    "implementation"(libs.gson)
}

tasks.named<ShadowJar>("shadowJar") {
    dependencies {
        include { true }
    }
    minimize {
        exclude(dependency("org.apache.logging.log4j:log4j-core"))
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}

configure<PublishingExtension> {
    publications.named<MavenPublication>("maven") {
        artifactId = the<BasePluginExtension>().archivesName.get()
        from(components["java"])
    }
}
