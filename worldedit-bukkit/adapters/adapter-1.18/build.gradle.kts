applyPaperweightAdapterConfiguration()

repositories {
    // For now, dev-bundle comes from mavenLocal.
    mavenLocal {
        content {
            includeGroup("io.papermc.paper")
        }
    }
}

dependencies {
    paperDevBundle("1.18-rc3-R0.1-SNAPSHOT")
}
