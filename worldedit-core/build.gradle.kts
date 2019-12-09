import net.minecrell.gradle.licenser.LicenseExtension
import org.gradle.plugins.ide.idea.model.IdeaModel
import com.mendhak.gradlecrowdin.DownloadTranslationsTask
import com.mendhak.gradlecrowdin.UploadSourceFileTask

plugins {
    id("java-library")
    id("net.ltgt.apt-eclipse")
    id("net.ltgt.apt-idea")
    id("antlr")
    id("com.mendhak.gradlecrowdin")
}

applyPlatformAndCoreConfiguration()

configurations.all {
    resolutionStrategy {
        force("com.google.guava:guava:21.0")
    }
}

dependencies {
    "compile"(project(":worldedit-libs:core"))
    "compile"("de.schlichtherle:truezip:6.8.3")
    "compile"("net.java.truevfs:truevfs-profile-default_2.13:0.12.1")
    "compile"("org.mozilla:rhino:1.7.11")
    "compile"("org.yaml:snakeyaml:1.9")
    "compile"("com.google.guava:guava:21.0")
    "compile"("com.google.code.findbugs:jsr305:1.3.9")
    "compile"("com.google.code.gson:gson:2.8.0")
    "compile"("org.slf4j:slf4j-api:1.7.26")
    "compile"("it.unimi.dsi:fastutil:8.2.1")

    val antlrVersion = "4.7.2"
    "antlr"("org.antlr:antlr4:$antlrVersion")
    "implementation"("org.antlr:antlr4-runtime:$antlrVersion")

    "compileOnly"(project(":worldedit-libs:core:ap"))
    "annotationProcessor"(project(":worldedit-libs:core:ap"))
    // ensure this is on the classpath for the AP
    "annotationProcessor"("com.google.guava:guava:21.0")
    "compileOnly"("com.google.auto.value:auto-value-annotations:${Versions.AUTO_VALUE}")
    "annotationProcessor"("com.google.auto.value:auto-value:${Versions.AUTO_VALUE}")
    "testImplementation"("ch.qos.logback:logback-core:${Versions.LOGBACK}")
    "testImplementation"("ch.qos.logback:logback-classic:${Versions.LOGBACK}")
}

tasks.withType<JavaCompile>().configureEach {
    dependsOn(":worldedit-libs:build")
    options.compilerArgs.add("-Aarg.name.key.prefix=")
}

tasks.named<AntlrTask>("generateGrammarSource").configure {
    val pkg = "com.sk89q.worldedit.antlr"
    outputDirectory = file("build/generated-src/antlr/main/${pkg.replace('.', '/')}")
    arguments = listOf(
        "-visitor", "-package", pkg,
        "-Xexact-output-dir"
    )
}

configure<LicenseExtension> {
    exclude {
        it.file.startsWith(project.buildDir)
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

sourceSets {
    main {
        java {
            srcDir("src/main/java")
            srcDir("src/legacy/java")
        }
        resources {
            srcDir("src/main/resources")
        }
    }
}

val crowdinApiKey = "crowdin_apikey"

if (project.hasProperty(crowdinApiKey)) {
    tasks.named<UploadSourceFileTask>("crowdinUpload") {
        apiKey = "${project.property(crowdinApiKey)}"
        projectId = "worldedit-core"
        files = arrayOf(
            object {
                var name = "strings.json"
                var source = "${file("src/main/resources/lang/strings.json")}"
            }
        )
    }

    tasks.named<DownloadTranslationsTask>("crowdinDownload") {
        apiKey = "${project.property(crowdinApiKey)}"
        destination = "${file("build/resources/main/lang")}"
        projectId = "worldedit-core"
    }

    tasks.named("classes").configure {
        dependsOn("crowdinDownload")
    }
}
