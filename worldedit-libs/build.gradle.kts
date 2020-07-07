tasks.register("build") {
    dependsOn(subprojects.map { it.tasks.named("build") })
}
