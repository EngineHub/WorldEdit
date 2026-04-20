package buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.CacheableRule
import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataRule
import org.gradle.api.artifacts.ModuleIdentifier
import org.gradle.kotlin.dsl.withModule
import javax.inject.Inject

/**
 * CUI protocol jars depend on the common protocol jar, but currently don't declare that properly.
 * This rule adds the common protocol as a dependency to all CUI protocol variants, so that Gradle can properly resolve
 * it.
 */
@CacheableRule
abstract class CuiProtocolDependsOnCommonRule @Inject constructor(val cuiProtocolCommonDependency: String) : ComponentMetadataRule {
    override fun execute(context: ComponentMetadataContext) {
        context.details.allVariants {
            withDependencies {
                add(cuiProtocolCommonDependency)
            }
        }
    }
}

/**
 * The common CUI protocol jar has a fabric-loader dependency, for whatever reason.
 * This rule removes it because the common protocol should be platform-agnostic and not depend on a specific loader.
 */
@CacheableRule
abstract class CuiProtocolCommonIsNotFabricSpecificRule @Inject constructor() : ComponentMetadataRule {
    override fun execute(context: ComponentMetadataContext) {
        context.details.allVariants {
            withDependencies {
                removeIf { it.group == "net.fabricmc" && it.name == "fabric-loader" }
            }
        }
    }
}

fun Project.withCuiProtocolDependsOnCommonRule(module: ModuleIdentifier) {
    dependencies.components {
        val cuiProtoCommon = stringyLibs.getLibrary("cuiProtocol-common").get()
        withModule<CuiProtocolDependsOnCommonRule>(module) {
            params(cuiProtoCommon.toString())
        }
        withModule<CuiProtocolCommonIsNotFabricSpecificRule>(cuiProtoCommon.module)
    }
}
