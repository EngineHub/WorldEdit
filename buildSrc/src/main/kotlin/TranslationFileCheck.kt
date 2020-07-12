import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileType
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import java.text.MessageFormat

@Suppress("UnstableApiUsage")
abstract class TranslationFileCheck : DefaultTask() {

    @get:[
    Incremental
    PathSensitive(PathSensitivity.NONE)
    InputFiles
    ]
    abstract val translationFiles: ConfigurableFileCollection

    @TaskAction
    fun checkTranslationFiles(inputChanges: InputChanges) {
        inputChanges.getFileChanges(translationFiles)
            .asSequence()
            .filter { it.fileType != FileType.DIRECTORY }
            .filter { it.changeType != ChangeType.REMOVED }
            .forEach { change ->
                @Suppress("UNCHECKED_CAST")
                val entries = JsonSlurper().parse(change.file) as Map<String, String>
                for ((key, value) in entries) {
                    try {
                        MessageFormat(value)
                    } catch (e: IllegalArgumentException) {
                        error("Entry '$key' in ${change.file} is an invalid translation file: ${e.message}")
                    }
                }
            }
    }

}
