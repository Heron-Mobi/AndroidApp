package mobi.heron.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mobi.heron.Config

@Composable
fun LoginView(config: Config, viewModel: LoginViewModel = hiltViewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loginState by viewModel.loginState.collectAsStateWithLifecycle()
    val isLoading = loginState == LoginViewModel.LoginState.Loading
    val isError = loginState == LoginViewModel.LoginState.Error

    val context = LocalContext.current

    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            enabled = !isLoading,
            label = { Text("Email") },
        )
        TextField(
            modifier = Modifier.padding(top = 10.dp),
            value = password,
            enabled = !isLoading,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        if (isError) {
            Text("Error logging in", color = Color.Red, modifier = Modifier.padding(top = 10.dp))
        }

        Button(onClick = {
            viewModel.login(context, config, email, password)
        }, modifier = Modifier.padding(top = 20.dp), enabled = !isLoading) {
            Text("Login")
        }
    }
}
