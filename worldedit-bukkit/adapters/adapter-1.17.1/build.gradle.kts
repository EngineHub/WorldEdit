import io.papermc.paperweight.userdev.PaperweightUserDependenciesExtension

applyPaperweightAdapterConfiguration(javaRelease = 17)

dependencies {
    the<PaperweightUserDependenciesExtension>().paperDevBundle("1.17.1-R0.1-20220414.034903-210")
}
