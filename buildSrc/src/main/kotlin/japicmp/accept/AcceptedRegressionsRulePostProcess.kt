package japicmp.accept

import me.champeau.gradle.japicmp.report.PostProcessViolationsRule
import me.champeau.gradle.japicmp.report.ViolationCheckContextWithViolations

class AcceptedRegressionsRulePostProcess : PostProcessViolationsRule {
    override fun execute(context: ViolationCheckContextWithViolations) {
        val changeParams = context.changeParams
        val seenApiChanges = context.seenApiChanges
        val left = HashSet(changeParams.changeToReason.keys)
        left.removeAll(seenApiChanges)
        if (!left.isEmpty()) {
            val formattedLeft: String = left.joinToString(separator = "\n") { it.toString() }
            throw RuntimeException("The following regressions are declared as accepted, but didn't match any rule:\n\n$formattedLeft")
        }
    }
}
