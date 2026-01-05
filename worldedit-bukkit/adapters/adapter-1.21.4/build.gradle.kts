import io.papermc.paperweight.userdev.PaperweightUserDependenciesExtension

plugins {
    id("buildlogic.adapter-reobf")
}

dependencies {
    // https://artifactory.papermc.io/ui/native/universe/io/papermc/paper/dev-bundle/
    the<PaperweightUserDependenciesExtension>().paperDevBundle("1.21.4-R0.1-20250925.065901-228")
}
