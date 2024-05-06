package mobi.heron.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mobi.heron.ui.MainViewModel.ConfigLoadingState.DoesNotExist
import mobi.heron.ui.MainViewModel.ConfigLoadingState.Exists
import mobi.heron.ui.MainViewModel.ConfigLoadingState.Loading
import mobi.heron.ui.config.DownloadConfigView
import mobi.heron.ui.home.HomeView
import mobi.heron.ui.login.LoginView
import mobi.heron.ui.theme.HeronTheme

@Composable
fun MainView(viewModel: MainViewModel = hiltViewModel()) {
    val configLoadingState by viewModel.configLoadingState.collectAsStateWithLifecycle()

    SideEffect {
        viewModel.init()
    }

    when (val state = configLoadingState) {
        is Loading -> SplashScreenView()
        is Exists -> LoginView(config = state.config)
        is DoesNotExist -> DownloadConfigView { config ->
            viewModel.onConfigDownloaded(config)
        }
    }
}

@Preview
@Composable
fun MainViewPreview() {
    HeronTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainView()
        }
    }
}
