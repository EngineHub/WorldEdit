import net.minecrell.gradle.licenser.LicenseExtension
import org.gradle.plugins.ide.idea.model.IdeaModel

plugins {
    id("java-library")
    id("net.ltgt.apt-eclipse")
    id("net.ltgt.apt-idea")
    id("antlr")
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
            version { strictly("1.26") }
            because("Bukkit provides SnakeYaml")
        }
    }

    "api"(project(":worldedit-libs:core"))
    "implementation"("de.schlichtherle:truezip:6.8.4")
    "implementation"("net.java.truevfs:truevfs-profile-default_2.13:0.12.1")
    "implementation"("org.mozilla:rhino-runtime:1.7.13")
    "implementation"("org.yaml:snakeyaml")
    "implementation"("com.google.guava:guava")
    "implementation"("com.google.code.findbugs:jsr305:1.3.9")
    "implementation"("com.google.code.gson:gson")
    "implementation"("org.slf4j:slf4j-api:${Versions.SLF4J}")
    "implementation"("it.unimi.dsi:fastutil")

    val antlrVersion = "4.9.1"
    "antlr"("org.antlr:antlr4:$antlrVersion")
    "implementation"("org.antlr:antlr4-runtime:$antlrVersion")

    "compileOnly"(project(":worldedit-libs:core:ap"))
    "annotationProcessor"(project(":worldedit-libs:core:ap"))
    // ensure this is on the classpath for the AP
    "annotationProcessor"("com.google.guava:guava:21.0")
    "compileOnly"("com.google.auto.value:auto-value-annotations:${Versions.AUTO_VALUE}")
    "annotationProcessor"("com.google.auto.value:auto-value:${Versions.AUTO_VALUE}")

    "languageFiles"("${project.group}:worldedit-lang:7.2.1:68@zip")

    "testImplementation"("ch.qos.logback:logback-core:${Versions.LOGBACK}")
    "testImplementation"("ch.qos.logback:logback-classic:${Versions.LOGBACK}")
}

tasks.named<Test>("test") {
    maxHeapSize = "1G"
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

sourceSets.named("main") {
    java {
        srcDir("src/main/java")
        srcDir("src/legacy/java")
    }
    resources {
        srcDir("src/main/resources")
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
