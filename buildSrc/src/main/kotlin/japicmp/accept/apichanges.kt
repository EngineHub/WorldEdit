package japicmp.accept

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.nio.file.Files
import java.nio.file.Path

typealias ApiChangesDiskFormat = Map<String, List<ApiChange>>;
fun loadAcceptedApiChanges(path: Path): Map<ApiChange, String> {
    val fromDisk: ApiChangesDiskFormat = Files.newBufferedReader(path).use {
        Gson().fromJson(it, object : TypeToken<ApiChangesDiskFormat>() {}.type)
    }
    return fromDisk.asSequence().flatMap { (key, value) ->
        value.asSequence().map { it to key }
    }.toMap()
}

data class ApiChange(
    val type: String,
    val member: String,
    val changes: List<String>,
)
