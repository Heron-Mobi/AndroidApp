package mobi.heron

import android.content.Context
import android.os.StrictMode
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
import com.amazonaws.regions.Regions

class CognitoConfig(context: Context, private val config: Config) {
    private val regions = Regions.fromName(config.Region)
    private val userPool =
        CognitoUserPool(context, config.UserPoolId, config.ClientId, "", regions)
    private val credentialsProvider =
        CognitoCachingCredentialsProvider(context, config.IdentityPoolId, regions)

    init {
        val threadPolicy = StrictMode.ThreadPolicy.Builder()
            .permitAll()
            .build()
        StrictMode.setThreadPolicy(threadPolicy)
    }

    fun userLogin(email: String, password: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        val cognitoUser = userPool.getUser(email)
        cognitoUser.getSession(object : AuthenticationHandler {
            override fun onSuccess(userSession: CognitoUserSession, newDevice: CognitoDevice) {
                val login = "cognito-idp.${config.Region}.amazonaws.com/${config.UserPoolId}"
                val logins = mapOf(login to userSession.idToken.jwtToken)
                credentialsProvider.setLogins(logins)

                onSuccess()
            }

            override fun getAuthenticationDetails(
                authenticationContinuation: AuthenticationContinuation,
                userId: String?
            ) {
                val authenticationDetails = AuthenticationDetails(userId, password, null)
                authenticationContinuation.setAuthenticationDetails(authenticationDetails)
                authenticationContinuation.continueTask()
            }

            override fun getMFACode(continuation: MultiFactorAuthenticationContinuation?) {
                TODO("Not yet implemented")
            }

            override fun authenticationChallenge(continuation: ChallengeContinuation?) {
            }

            override fun onFailure(exception: Exception?) {
                onFailure()
            }
        })
    }
}