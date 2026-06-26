import buildlogic.stringyLibs
import buildlogic.getLibrary

plugins {
    id("org.enginehub.crankcase.java-library")
    id("buildlogic.common-java")
}

dependencies {
    "compileOnlyApi"(stringyLibs.getLibrary("errorprone-annotations"))
}
