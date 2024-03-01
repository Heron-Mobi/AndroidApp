package mobi.heron;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import com.amazonaws.regions.Regions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ConfigLoader {
    public String videobucket;
    public String clientID;
    public String configbucket;
    public String identityPoolID;
    public String clientSecret = "";
    public Regions awsRegions;
    public String region;
    public String userPoolID;
    public String queue;
    public String cognitouser;

    public void load(Context mContext) throws FileNotFoundException {
        SharedPreferences prefs = mContext.getSharedPreferences("cognitouser", MODE_PRIVATE);
        if (prefs.contains("name")) {
            this.cognitouser = prefs.getString("name", "");
        }
        File file = new File(
                mContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "config.json"
        );
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(e.toString());
        }
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        try {
            line = bufferedReader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (line != null){
            stringBuilder.append(line).append("\n");
            try {
                line = bufferedReader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            bufferedReader.close();// This response will have Json Format String
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String response = stringBuilder.toString();
        JSONObject jsonObject = null;
        try {
            jsonObject  = new JSONObject(response);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
            try {
            this.userPoolID = jsonObject.get("UserPoolId").toString();
            this.region = jsonObject.get("Region").toString();
            this.clientID = jsonObject.get("ClientId").toString();
            this.identityPoolID = jsonObject.get("IdentityPoolId").toString();
            this.configbucket = jsonObject.get("ConfigBucket").toString();
            this.videobucket = jsonObject.get("VideoBucket").toString();
            this.queue = jsonObject.get("SignalQueueURL").toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}