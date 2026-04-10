import io.papermc.paperweight.userdev.PaperweightUserDependenciesExtension

plugins {
    id("buildlogic.adapter-mojmap")
}

dependencies {
    // https://artifactory.papermc.io/ui/native/universe/io/papermc/paper/dev-bundle/
    the<PaperweightUserDependenciesExtension>().paperDevBundle("26.1.1.build.29-alpha")
}
