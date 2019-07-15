import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

dependencies {
    "compile"(project(":worldedit-core"))
    "compile"("org.apache.logging.log4j:log4j-core:2.8.1")
    "compile"( "org.apache.logging.log4j:log4j-slf4j-impl:2.8.1")
    "compile"( "commons-cli:commons-cli:1.4")

    "testCompile"("org.mockito:mockito-core:1.9.0-rc1")
}

tasks.named<Jar>("jar") {
    manifest {
        attributes("WorldEdit-Version" to project.version)
    }
}

tasks.named<ShadowJar>("shadowJar") {
    dependencies {
        relocate("org.slf4j", "com.sk89q.worldedit.slf4j")
        relocate("org.apache.logging.slf4j", "com.sk89q.worldedit.log4jbridge")

        include(dependency("org.slf4j:slf4j-api"))
        include(dependency("commons-cli:commons-cli:1.4"))
        include(dependency("org.apache.logging.log4j:log4j-core"))
        include(dependency("org.apache.logging.log4j:log4j-slf4j-impl"))
    }
}
