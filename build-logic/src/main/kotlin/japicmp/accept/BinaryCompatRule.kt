package japicmp.accept

import japicmp.model.*
import me.champeau.gradle.japicmp.report.Violation

private val IGNORED_CHANGE_TYPES: List<JApiCompatibilityChangeType> = listOf(
    JApiCompatibilityChangeType.METHOD_REMOVED_IN_SUPERCLASS,  // the removal of the method will be reported
    JApiCompatibilityChangeType.INTERFACE_REMOVED,  // the removed methods will be reported
    JApiCompatibilityChangeType.INTERFACE_ADDED, // the added methods will be reported
    JApiCompatibilityChangeType.ANNOTATION_DEPRECATED_ADDED, // semver detection is broken
)

class BinaryCompatRule() : AbstractAcceptingRule() {

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
            if (IGNORED_CHANGE_TYPES.contains(change.type)) {
                return null
            }
        }
        return checkAcceptance(
            member,
            member.compatibilityChanges.map { it.type.name },
            Violation.notBinaryCompatible(member),
        )
    }
}
