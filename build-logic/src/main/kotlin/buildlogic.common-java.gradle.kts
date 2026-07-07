import buildlogic.stringyLibs
import buildlogic.getLibrary

plugins {
    id("eclipse")
    id("idea")
    id("org.enginehub.crankcase.java")
    id("org.enginehub.crankcase.checkstyle")
    id("buildlogic.common")
}

crankcaseJava {
    javaRelease = 25
    disabledLints = listOf("processing", "path", "fallthrough", "serial", "overloads")
    disabledErrorprone = listOf(
        // We use reference equality intentionally in several places
        // Perhaps we should consider testing the performance impact of using .equals() instead?
        // Especially for the types that are only compared by reference equality, we could consider
        // removing their .equals() implementations to avoid confusion.
        "ReferenceEquality",
        // We're on JDK 21, so System.console() can still be null
        "SystemConsoleNull",
    )
}

crankcaseCheckstyle {
    suppressionsFile = rootDir.resolve("config/checkstyle/checkstyle-suppression.xml")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform {
        includeEngines("junit-jupiter", "jqwik")
    }
}

dependencies {
    "compileOnly"(stringyLibs.getLibrary("jsr305"))
    "testImplementation"(stringyLibs.getLibrary("jqwik"))
    "testImplementation"(platform(stringyLibs.getLibrary("mockito-bom")))
    "testImplementation"(stringyLibs.getLibrary("mockito-core"))
    "testImplementation"(stringyLibs.getLibrary("mockito-junit-jupiter"))
}
