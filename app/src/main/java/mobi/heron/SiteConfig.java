package mobi.heron;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import mobi.heron.databinding.ActivityMainBinding;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;


public class SiteConfig extends AppCompatActivity {
    private ActivityMainBinding binding;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Context mContext = getApplicationContext();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(R.layout.config);

        Button saveDomain = findViewById(R.id.saveDomain);
        Button login = findViewById(R.id.login);
        Button back = findViewById(R.id.back);
        EditText email = findViewById(R.id.emailAddress);
        EditText password = findViewById(R.id.password);
        EditText domain = findViewById(R.id.configDomain);
        SharedPreferences prefs = mContext.getSharedPreferences("cognitouser", MODE_PRIVATE);
        if (prefs.contains("name")) {
            email.setText(prefs.getString("name", ""));
        }

        saveDomain.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String configurl = "https://dashboard." + domain.getText().toString() + "/config.json";
                Log.i("url", configurl);
                new DownloadFileFromURL().execute(configurl);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences pref = mContext.getSharedPreferences("cognitouser", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("name",email.getText().toString());
                editor.apply();
                ConfigLoader config = new ConfigLoader();
                try {
                    config.load(mContext);
                } catch (FileNotFoundException e) {
                    Toast.makeText(mContext,"Config file does not exist. Save your config domain.", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                Cognito cognito = new Cognito(mContext, config);
                cognito.userLogin(
                        password.getText().toString()
                );
                cognito.getCredentials();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent mainActivityIntent = new Intent(
                        mContext, MainActivity.class
                );
                startActivity(mainActivityIntent);
            }
        });



    }

    /**
     * Background Async Task to download file
     * */
    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = connection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                // Output stream
                Context mContext = getApplicationContext();

                OutputStream output = new FileOutputStream(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString()
                        + "/config.json");

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }


    }



}



