package mobi.heron.ui.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mobi.heron.Config

@Composable
fun DownloadConfigView(
    viewModel: DownloadConfigViewModel = hiltViewModel(),
    onDownloadSuccess: (config: Config) -> Unit
) {
    var domain by remember { mutableStateOf("") }

    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()

    val downloadButton = @Composable {
        Button(modifier = Modifier.padding(top = 10.dp), onClick = {
            viewModel.downloadConfig(domain) { config ->
                onDownloadSuccess(config)
            }
        }) {
            Text("Download config")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        TextField(value = domain, onValueChange = { domain = it }, label = { Text("Domain") })


        when (downloadState) {
            DownloadConfigViewModel.DownloadState.Idle -> {
                downloadButton()
            }

            DownloadConfigViewModel.DownloadState.Loading -> {
                Text("Downloading config...")
                CircularProgressIndicator(progress = { progress.toFloat() })
            }

            DownloadConfigViewModel.DownloadState.Success -> {
                Text("Config downloaded successfully")
            }

            DownloadConfigViewModel.DownloadState.Error -> {
                downloadButton()

                Text("Error downloading config")
            }
        }
    }
}
