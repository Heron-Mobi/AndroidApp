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
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

import android.os.StrictMode;


public class Cognito {
    // ############################################################# Information about Cognito Pool


    private String identityPoolID;
    private String clientSecret = "";

    private Regions awsRegions;

    private String region;

    // ############################################################# End of Information about Cognito Pool
    private CognitoUserPool userPool;

    private String userPoolID;
    private Context appContext;
    private String userPassword;                        // Used for Login

    public CognitoCachingCredentialsProvider credentialsProvider;                        // Used for Login

    public String identityId;

    public Cognito(Context context, String region, String userPoolID, String clientID, String identityPoolID){
        this.identityPoolID = identityPoolID;
        this.region = region;
        this.userPoolID = userPoolID;
        appContext = context;
        awsRegions = Regions.fromName(region);         // Place your Region
        //awsRegion = Region.getRegion(awsRegions);
        userPool = new CognitoUserPool(context, userPoolID, clientID, clientSecret, awsRegions);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
    }



    public void confirmUser(String userId, String code){
        CognitoUser cognitoUser =  userPool.getUser(userId);
        cognitoUser.confirmSignUpInBackground(code,false, confirmationCallback);
        //cognitoUser.confirmSignUp(code,false, confirmationCallback);
    }
    // Callback handler for confirmSignUp API
    GenericHandler confirmationCallback = new GenericHandler() {

        @Override
        public void onSuccess() {
            // User was successfully confirmed
            Toast.makeText(appContext,"User Confirmed", Toast.LENGTH_LONG).show();

        }

        @Override
        public void onFailure(Exception exception) {
            // User confirmation failed. Check exception for the cause.

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
            Toast.makeText(appContext,"Sign in success", Toast.LENGTH_LONG).show();
            String i = userSession.toString();
            Log.i("user session", i);

            credentialsProvider = new CognitoCachingCredentialsProvider(appContext, identityPoolID, awsRegions);

            Map<String, String> logins = new HashMap<String, String>();
            String login = "cognito-idp."+ region +".amazonaws.com/"+ userPoolID;
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
            Toast.makeText(appContext,"Sign in Failure", Toast.LENGTH_LONG).show();
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