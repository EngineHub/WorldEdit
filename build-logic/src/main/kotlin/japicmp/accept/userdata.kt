package japicmp.accept

import me.champeau.gradle.japicmp.report.ViolationCheckContext

val ViolationCheckContext.changeParams
    get() = userData["changeParams"] as ChangeParams

@Suppress("UNCHECKED_CAST")
val ViolationCheckContext.seenApiChanges
    get() = userData["seenApiChanges"] as MutableSet<ApiChange>
