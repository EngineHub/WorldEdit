import org.ajoberstar.grgit.Grgit

plugins {
    alias(libs.plugins.codecov)
    jacoco
    id("buildlogic.common")
    id("buildlogic.artifactory-root")
}

if (!project.hasProperty("gitCommitHash")) {
    apply(plugin = "org.ajoberstar.grgit")
    ext["gitCommitHash"] = try {
        extensions.getByName<Grgit>("grgit").head()?.abbreviatedId
    } catch (e: Exception) {
        logger.warn("Error getting commit hash", e)

        "no.git.id"
    }
}

val totalReport = tasks.register<JacocoReport>("jacocoTotalReport") {
    for (proj in subprojects) {
        proj.apply(plugin = "jacoco")
        proj.plugins.withId("java") {
            executionData(
                    fileTree(proj.layout.buildDirectory).include("**/jacoco/*.exec")
            )
            sourceSets(proj.the<JavaPluginExtension>().sourceSets["main"])
            reports {
                xml.required.set(true)
                xml.outputLocation.set(rootProject.layout.buildDirectory.file("reports/jacoco/report.xml"))
                html.required.set(true)
            }
            dependsOn(proj.tasks.named("test"))
        }
    }
}
afterEvaluate {
    totalReport.configure {
        classDirectories.setFrom(classDirectories.files.map {
            fileTree(it).apply {
                exclude("**/*AutoValue_*")
                exclude("**/*Registration.*")
            }
        })
    }
}

codecov {
    reportTask.set(totalReport)
}
