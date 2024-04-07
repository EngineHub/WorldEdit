import org.cadixdev.gradle.licenser.LicenseExtension
import org.gradle.plugins.ide.idea.model.IdeaModel

plugins {
    `java-library`
    antlr
}

applyPlatformAndCoreConfiguration()

repositories {
    ivy {
        url = uri("https://repo.enginehub.org/language-files/")
        name = "EngineHub Language Files"
        patternLayout {
            artifact("[organisation]/[module]/[revision]/[artifact]-[revision](+[classifier])(.[ext])")
            setM2compatible(true)
        }
        metadataSources {
            artifact()
        }
    }
}

configurations {
    register("languageFiles")
}

dependencies {
    constraints {
        "implementation"( "org.yaml:snakeyaml") {
            version { require("2.0") }
            because("Bukkit provides SnakeYaml")
        }
    }

    "api"(project(":worldedit-libs:core"))
    "compileOnly"("de.schlichtherle:truezip:6.8.4")
    "implementation"("org.mozilla:rhino-runtime:1.7.13")
    "implementation"("org.yaml:snakeyaml:2.0")
    "implementation"("com.google.guava:guava")
    "compileOnlyApi"("com.google.code.findbugs:jsr305:1.3.9")
    "implementation"("com.google.code.gson:gson")

    "implementation"("com.sk89q:jchronic:0.2.4a") {
        exclude(group = "junit", module = "junit")
    }
    "implementation"("com.thoughtworks.paranamer:paranamer:2.6")
    "implementation"("com.sk89q.lib:jlibnoise:1.0.0")
    "api"(platform("org.enginehub.lin-bus:lin-bus-bom:${Versions.LIN_BUS}"))
    "api"("org.enginehub.lin-bus:lin-bus-tree")
    "api"("org.enginehub.lin-bus.format:lin-bus-format-snbt")

    "implementation"("org.apache.logging.log4j:log4j-api:${Versions.LOG4J}") {
        because("Mojang provides Log4J")
    }

    "implementation"("it.unimi.dsi:fastutil")

    val antlrVersion = "4.13.1"
    "antlr"("org.antlr:antlr4:$antlrVersion")
    "implementation"("org.antlr:antlr4-runtime:$antlrVersion")

    "compileOnly"(project(":worldedit-libs:core:ap"))
    "annotationProcessor"(project(":worldedit-libs:core:ap"))
    // ensure this is on the classpath for the AP
    "annotationProcessor"("com.google.guava:guava:${Versions.GUAVA}")
    "compileOnly"("com.google.auto.value:auto-value-annotations:${Versions.AUTO_VALUE}")
    "annotationProcessor"("com.google.auto.value:auto-value:${Versions.AUTO_VALUE}")

    "compileOnly"("com.google.auto.service:auto-service:1.1.1") {
        because("Needed to resolve annotations in Piston")
    }

    "languageFiles"("${project.group}:worldedit-lang:7.3.1:1309@zip")

    "testRuntimeOnly"("org.apache.logging.log4j:log4j-core:${Versions.LOG4J}")
}

tasks.test {
    maxHeapSize = "1G"
}

tasks.compileJava {
    dependsOn(":worldedit-libs:build")
    options.compilerArgs.add("-Aarg.name.key.prefix=")
}

tasks.generateGrammarSource {
    val pkg = "com.sk89q.worldedit.antlr"
    outputDirectory = file("build/generated-src/antlr/main/${pkg.replace('.', '/')}")
    arguments = listOf(
        "-visitor", "-package", pkg,
        "-Xexact-output-dir"
    )
}

tasks.named("sourcesJar") {
    mustRunAfter("generateGrammarSource")
}

configure<LicenseExtension> {
    exclude {
        it.file.startsWith(project.layout.buildDirectory.get().asFile)
    }
}
tasks.withType<Checkstyle>().configureEach {
    exclude("com/sk89q/worldedit/antlr/**/*.java")
}

// Give intellij info about where ANTLR code comes from
plugins.withId("idea") {
    configure<IdeaModel> {
        afterEvaluate {
            module.sourceDirs.add(file("src/main/antlr"))
            module.sourceDirs.add(file("build/generated-src/antlr/main"))
            module.generatedSourceDirs.add(file("build/generated-src/antlr/main"))
        }
    }
}

sourceSets.named("main") {
    java {
        srcDir("src/legacy/java")
    }
}

tasks.named<Copy>("processResources") {
    from(configurations.named("languageFiles")) {
        rename {
            "i18n.zip"
        }
        into("lang")
    }
}

configure<PublishingExtension> {
    publications.named<MavenPublication>("maven") {
        artifactId = the<BasePluginExtension>().archivesName.get()
        from(components["java"])
    }
}
