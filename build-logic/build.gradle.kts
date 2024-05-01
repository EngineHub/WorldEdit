plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    maven {
        name = "EngineHub Repository"
        url = uri("https://maven.enginehub.org/repo/")
    }
}

dependencies {
    implementation(gradleApi())
    implementation(libs.licenser)
    implementation(libs.grgit)
    implementation(libs.japicmp)
    implementation(libs.shadow)
    implementation(libs.jfrog.buildinfo)
    implementation(libs.paperweight)
    implementation(libs.gson)
    constraints {
        val asmVersion = "[${libs.versions.minimumAsm.get()},)"
        implementation("org.ow2.asm:asm:$asmVersion") {
            because("Need Java 21 support in shadow")
        }
        implementation("org.ow2.asm:asm-commons:$asmVersion") {
            because("Need Java 21 support in shadow")
        }
        implementation("org.vafer:jdependency:[${libs.versions.minimumJdependency.get()},)") {
            because("Need Java 21 support in shadow")
        }
    }
}
