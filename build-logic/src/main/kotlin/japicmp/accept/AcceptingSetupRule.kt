package japicmp.accept

import me.champeau.gradle.japicmp.report.SetupRule
import me.champeau.gradle.japicmp.report.ViolationCheckContext
import java.nio.file.Path
import java.nio.file.Paths

class AcceptingSetupRule(private val params: Map<String, String>) : SetupRule {
    companion object {
        fun createParams(changeFile: Path): Map<String, String> {
            return mapOf(
                "changeFile" to changeFile.toAbsolutePath().toString(),
                "fileName" to changeFile.fileName.toString()
            )
        }
    }

    override fun execute(t: ViolationCheckContext) {
        @Suppress("UNCHECKED_CAST")
        val userData = t.userData as MutableMap<String, Any>
        userData["changeParams"] = ChangeParams(
            changeToReason = loadAcceptedApiChanges(Paths.get(params["changeFile"]!!)),
            changeFileName = params["fileName"]!!,
        )
        userData["seenApiChanges"] = HashSet<ApiChange>()
    }

}
