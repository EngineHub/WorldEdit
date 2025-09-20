import io.papermc.paperweight.userdev.PaperweightUserDependenciesExtension

plugins {
    id("buildlogic.adapter")
}

dependencies {
    // https://repo.papermc.io/service/rest/repository/browse/maven-public/io/papermc/paper/dev-bundle/
    the<PaperweightUserDependenciesExtension>().paperDevBundle("1.21.9-pre2-R0.1-SNAPSHOT")
}
