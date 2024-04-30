plugins {
    id("com.github.johnrengelman.shadow")
    id("buildlogic.core-and-platform")
}

val platform = extensions.create<buildlogic.PlatformExtension>("platform")
platform.includeClasspath.convention(false)
platform.extraAttributes.convention(mapOf())

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier.set("dist")
    dependencies {
        include(project(":worldedit-libs:core"))
        include(project(":worldedit-libs:${project.name.replace("worldedit-", "")}"))
        include(project(":worldedit-core"))
        exclude("com.google.code.findbugs:jsr305")
    }
    exclude("GradleStart**")
    exclude(".cache")
    exclude("LICENSE*")
    exclude("META-INF/maven/**")
    minimize()
}
val javaComponent = components["java"] as AdhocComponentWithVariants
// I don't think we want this published (it's the shadow jar)
javaComponent.withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) {
    skip()
}

afterEvaluate {
    tasks.named<Jar>("jar") {
        val kind = platform.kind.get()
        val includeClasspath = platform.includeClasspath.get()
        val extraAttributes = platform.extraAttributes.get()

        val version = project(":worldedit-core").version
        inputs.property("version", version)
        val attributes = mutableMapOf(
            "Implementation-Version" to version,
            "WorldEdit-Version" to version,
            "WorldEdit-Kind" to kind.name,
            "Main-Class" to kind.mainClass
        )
        if (includeClasspath) {
            attributes["Class-Path"] = listOf("truezip", "truevfs", "js")
                .map { "$it.jar" }
                .flatMap { listOf(it, "WorldEdit/$it") }
                .joinToString(separator = " ")
        }
        attributes.putAll(extraAttributes)
        manifest.attributes(attributes)
    }
}
