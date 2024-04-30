package japicmp.accept

import japicmp.model.*
import me.champeau.gradle.japicmp.report.Violation


class BinaryCompatRule() : AbstractAcceptingRule() {
    private val IGNORED_CHANGE_TYPES: List<JApiCompatibilityChange> = listOf(
        JApiCompatibilityChange.METHOD_REMOVED_IN_SUPERCLASS,  // the removal of the method will be reported
        JApiCompatibilityChange.INTERFACE_REMOVED,  // the removed methods will be reported
        JApiCompatibilityChange.INTERFACE_ADDED, // the added methods will be reported
        JApiCompatibilityChange.ANNOTATION_DEPRECATED_ADDED, // semver detection is broken
    )

    override fun maybeViolation(member: JApiCompatibility): Violation? {
        if (member.isBinaryCompatible) {
            return null
        }
        if (member is JApiClass && member.compatibilityChanges.isEmpty()) {
            // A member of the class breaks binary compatibility.
            // That will be handled when the member is passed to `maybeViolation`.
            return null
        }
        if (member is JApiImplementedInterface) {
            // The changes about the interface's methods will be reported already
            return null
        }
        for (change in member.compatibilityChanges) {
            if (IGNORED_CHANGE_TYPES.contains(change)) {
                return null
            }
        }
        return checkAcceptance(
            member,
            member.compatibilityChanges.map { it.name },
            Violation.notBinaryCompatible(member),
        )
    }
}
