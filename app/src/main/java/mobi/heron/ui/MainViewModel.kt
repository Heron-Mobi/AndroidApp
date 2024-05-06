package mobi.heron.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mobi.heron.Config
import mobi.heron.ConfigLoader
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val configLoader: ConfigLoader) : ViewModel() {
    sealed class ConfigLoadingState {
        data object Loading: ConfigLoadingState()
        data class Exists(val config: Config): ConfigLoadingState()
        data object DoesNotExist: ConfigLoadingState()
    }

    private val _configLoadingState: MutableStateFlow<ConfigLoadingState> = MutableStateFlow(ConfigLoadingState.Loading)

    val configLoadingState: StateFlow<ConfigLoadingState>
        get() = _configLoadingState

    fun init() {
        val config = configLoader.load()

        _configLoadingState.value =
            if (config != null) {
                ConfigLoadingState.Exists(config)
            } else {
                ConfigLoadingState.DoesNotExist
            }
    }

    fun onConfigDownloaded(config: Config) {
        _configLoadingState.value = ConfigLoadingState.Exists(config)
    }
}
