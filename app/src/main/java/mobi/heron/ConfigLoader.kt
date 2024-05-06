package mobi.heron

import android.content.Context
import android.os.Environment
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ConfigLoader @Inject constructor(@ApplicationContext private val context: Context) {
    fun load(): Config? {
        val externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val configFile = externalFilesDir?.resolve("config.json")
        if (configFile != null && configFile.exists()) {
            val text = configFile.readText()
            return try {
                Json.decodeFromString<Config>(text)
            } catch (e: Exception) {
                null
            }
        }

        return null
    }

    fun save(config: Config) {
        val externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val configFile = externalFilesDir?.resolve("config.json")
        configFile?.writeText(Json.encodeToString(config))
    }
}