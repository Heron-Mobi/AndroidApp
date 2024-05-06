package mobi.heron.ui.config

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mobi.heron.Config
import mobi.heron.ConfigLoader
import javax.inject.Inject

@HiltViewModel
class DownloadConfigViewModel @Inject constructor(
    private val configLoader: ConfigLoader,
    private val httpClient: HttpClient
) : ViewModel() {
    private val _progress = MutableStateFlow(0.0)

    val progress: StateFlow<Double>
        get() = _progress

    enum class DownloadState {
        Idle,
        Loading,
        Success,
        Error
    }

    private val _downloadState = MutableStateFlow(DownloadState.Idle)

    val downloadState: StateFlow<DownloadState>
        get() = _downloadState

    fun downloadConfig(domain: String, onDownloadSuccess: (config: Config) -> Unit) {
        viewModelScope.launch {
            val config = downloadConfigInternal(domain)
            if (config != null) {
                onDownloadSuccess(config)
            }
        }
    }

    private suspend fun downloadConfigInternal(domain: String): Config? {
        _downloadState.value = DownloadState.Loading
        _progress.value = 0.0
        val url = "https://dashboard.$domain/config.json"

        try {
            val response = httpClient.get {
                url(url)
                onDownload { bytesSentTotal, contentLength ->
                    _progress.value = bytesSentTotal.toDouble() / contentLength
                }
            }

            val config = response.body<Config>()
            configLoader.save(config)
            _downloadState.value = DownloadState.Success

            return config
        } catch (e: Exception) {
            Log.e("DownloadConfigViewModel", "Failed to download config", e)
            _downloadState.value = DownloadState.Error
            return null
        }
    }
}
