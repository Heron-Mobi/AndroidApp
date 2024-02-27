package com.example.heron;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.regions.Regions;
import android.os.StrictMode;


public class Cognito {
    // ############################################################# Information about Cognito Pool

    private Context mContext;
    private String userPassword;                        // Used for Login
    private CognitoUserPool userPool;
    public CognitoCachingCredentialsProvider credentialsProvider;                        // Used for Login
    private ConfigLoader config;

    private Regions awsRegions;
    public String identityId;

    public Cognito(Context mContext, ConfigLoader config){
        this.mContext = mContext;
        awsRegions = Regions.fromName(config.region);
        this.config = config;
        userPool = new CognitoUserPool(mContext, config.userPoolID, config.clientID, config.clientSecret, awsRegions);
        this.credentialsProvider = new CognitoCachingCredentialsProvider(mContext, config.identityPoolID, awsRegions);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public void getCredentials(){
        this.credentialsProvider.getCredentials();
        identityId = this.credentialsProvider.getIdentityId();
    }
    public void confirmUser(String userId, String code){
        CognitoUser cognitoUser =  userPool.getUser(userId);
        cognitoUser.confirmSignUpInBackground(code,false, confirmationCallback);
        //cognitoUser.confirmSignUp(code,false, confirmationCallback);
    }
    GenericHandler confirmationCallback = new GenericHandler() {
        @Override
        public void onSuccess() {
            // User was successfully confirmed
            Toast.makeText(mContext,"User Confirmed", Toast.LENGTH_LONG).show();

        }
        @Override
        public void onFailure(Exception exception) {
            // User confirmation failed. Check exception for the cause.
            Toast.makeText(mContext,"User Login Failed", Toast.LENGTH_LONG).show();

        }
    };

    public void userLogin(String userId, String password){
        CognitoUser cognitoUser =  userPool.getUser(userId);
        this.userPassword = password;
        cognitoUser.getSession(authenticationHandler);
    }

    // Callback handler for the sign-in process
    AuthenticationHandler authenticationHandler = new AuthenticationHandler() {

        public void authenticationChallenge(ChallengeContinuation continuation) {

        }
        @Override
        public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
            Toast.makeText(mContext,"Sign in success", Toast.LENGTH_LONG).show();
            String i = userSession.toString();
            Map<String, String> logins = new HashMap<String, String>();
            String login = "cognito-idp."+ config.region +".amazonaws.com/"+ config.userPoolID;
            Log.i("login endpoint", login);
            logins.put(login, userSession.getIdToken().getJWTToken());
            credentialsProvider.setLogins(logins);
            try {
                identityId = new getID().execute(credentialsProvider).get();

            } catch (InterruptedException e) {
                Log.e("I exception", "exception", e);
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                Log.e("e exception", "exception", e);
                throw new RuntimeException(e);
            } catch (CancellationException e) {
                Log.e("c exception", "exception", e);
                throw new RuntimeException(e);
            }

            Log.d("LogTag", "my ID is " + identityId);



        }
        @Override
        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
            // The API needs user sign-in credentials to continue
            AuthenticationDetails authenticationDetails = new AuthenticationDetails(userId, userPassword, null);
            // Pass the user sign-in credentials to the continuation
            authenticationContinuation.setAuthenticationDetails(authenticationDetails);
            // Allow the sign-in to continue
            authenticationContinuation.continueTask();
        }
        @Override
        public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {
            // Multi-factor authentication is required; get the verification code from user
            //multiFactorAuthenticationContinuation.setMfaCode(mfaVerificationCode);
            // Allow the sign-in process to continue
            //multiFactorAuthenticationContinuation.continueTask();
        }
        @Override
        public void onFailure(Exception exception) {
            // Sign-in failed, check exception for the cause
            Toast.makeText(mContext,"Sign in Failure", Toast.LENGTH_LONG).show();
        }
    };
    public class getID extends AsyncTask<CognitoCachingCredentialsProvider, Void, String> {
        protected String doInBackground(CognitoCachingCredentialsProvider... credentialsProvider) {
            try {

                identityId = credentialsProvider[0].getIdentityId();

        } catch (Exception e) {
                Log.e("Exec exception", "exception", e);

            }
            return identityId;
        }

    }
}