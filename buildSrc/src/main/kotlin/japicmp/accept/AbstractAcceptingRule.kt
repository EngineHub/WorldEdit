package japicmp.accept

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonWriter
import japicmp.model.JApiCompatibility
import me.champeau.gradle.japicmp.report.AbstractContextAwareViolationRule
import me.champeau.gradle.japicmp.report.Violation
import java.io.StringWriter

abstract class AbstractAcceptingRule : AbstractContextAwareViolationRule() {
    fun checkAcceptance(
        member: JApiCompatibility,
        changes: List<String>,
        rejection: Violation
    ): Violation {
        val changeParams = context.changeParams
        val seenApiChanges = context.seenApiChanges
        val change = ApiChange(
            context.className,
            Violation.describe(member),
            changes
        )
        val reason = changeParams.changeToReason.get(change)
        if (reason != null) {
            seenApiChanges.add(change)
            return Violation.accept(
                member,
                "${rejection.humanExplanation}. Reason for accepting this: <b>$reason</b>"
            )
        }
        return Violation.error(
            member,
            rejection.humanExplanation + """.
                <br>
                <p>
                In order to accept this change add the following to <code>verification/src/changes/${changeParams.changeFileName}</code>:
                <pre>${prettyPrintJson(mapOf("[REASON CHANGE IS OKAY]" to listOf(change)))}</pre>
                </p>
            """.trimIndent()
        )
    }

    private fun prettyPrintJson(acceptanceJson: Any): String {
        val stringWriter = StringWriter()
        JsonWriter(stringWriter).use {
            it.setIndent("    ")
            Gson().toJson(acceptanceJson, object : TypeToken<ApiChangesDiskFormat>() {}.type, it)
        }
        return stringWriter.toString()
    }
}
