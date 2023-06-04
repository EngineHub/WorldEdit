import io.papermc.paperweight.userdev.PaperweightUserDependenciesExtension

applyPaperweightAdapterConfiguration()

dependencies {
    // https://repo.papermc.io/service/rest/repository/browse/maven-public/io/papermc/paper/dev-bundle/
    the<PaperweightUserDependenciesExtension>().paperDevBundle("1.18.2-R0.1-20220304.102823-4")
}
