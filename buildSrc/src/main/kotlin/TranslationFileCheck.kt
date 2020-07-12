import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileType
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import java.io.File
import java.text.MessageFormat

@Suppress("UnstableApiUsage")
abstract class TranslationFileCheck : DefaultTask() {

    @get: [
    PathSensitive(PathSensitivity.NONE)
    InputFile
    ]
    abstract val sourceFile: RegularFileProperty

    @get:[
    Incremental
    PathSensitive(PathSensitivity.NONE)
    InputFiles
    ]
    abstract val translationFiles: ConfigurableFileCollection

    @TaskAction
    fun checkTranslationFiles(inputChanges: InputChanges) {
        val sourceEntries = loadMessageFormats(sourceFile.asFile.get())

        inputChanges.getFileChanges(translationFiles)
            .asSequence()
            .filter { it.fileType != FileType.DIRECTORY }
            .filter { it.changeType != ChangeType.REMOVED }
            .forEach { change ->
                val compareEntries = loadMessageFormats(change.file)
                for ((key, format) in compareEntries) {
                    val sourceFormat = sourceEntries[key]
                        ?: error("Entry '$key' in ${change.file} has no matching format in the source")
                    val expectedFormatsSize = sourceFormat.formats.size
                    val actualFormatsSize = format.formats.size
                    check(expectedFormatsSize == actualFormatsSize) {
                        "Entry '$key' in ${change.file} has $actualFormatsSize formats instead of $expectedFormatsSize\n" +
                            "Literal expected: ${sourceFormat.toPattern()}\n" +
                            "Literal actual: ${format.toPattern()}"
                    }
                }
            }
    }

    private fun loadMessageFormats(file: File): Map<String, MessageFormat> =
        entries(file)
            .filterValues { it.isNotEmpty() }
            .mapValues { (_, value) -> value.replace("'", "''") }
            .mapValues { (key, value) ->
                try {
                    MessageFormat(value)
                } catch (e: IllegalArgumentException) {
                    error("Entry '$key' in $file is an invalid translation file: ${e.message}")
                }
            }

    @Suppress("UNCHECKED_CAST")
    private fun entries(file: File) = JsonSlurper().parse(file) as Map<String, String>

}
