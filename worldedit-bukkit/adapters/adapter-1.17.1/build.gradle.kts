// TODO await https://github.com/PaperMC/paperweight/issues/116
//applyPaperweightAdapterConfiguration()
//
//dependencies {
//    paperDevBundle("1.17.1-R0.1-20211120.192557-194")
//}

// Until the above issue is resolved, we are bundling old versions using their last assembled JAR.
// Technically this means we cannot really update them, but that is is the price we pay for supporting older versions.

plugins {
    base
}

artifacts {
    add("default", file("./src/main/resources/worldedit-adapter-1.17.1.jar"))
}
