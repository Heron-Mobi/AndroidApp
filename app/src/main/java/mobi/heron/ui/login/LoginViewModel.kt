package mobi.heron.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mobi.heron.CognitoConfig
import mobi.heron.Config
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {
    enum class LoginState {
        Idle,
        Loading,
        Success,
        Error
    }

    private val _loginState = MutableStateFlow(LoginState.Idle)

    val loginState: StateFlow<LoginState>
        get() = _loginState

    fun login(context: Context, config: Config, email: String, password: String) {
        val cognitoConfig = CognitoConfig(context, config)

        _loginState.value = LoginState.Loading

        cognitoConfig.userLogin(email, password, {
            _loginState.value = LoginState.Success
        }, {
            _loginState.value = LoginState.Error
        })
    }
}
