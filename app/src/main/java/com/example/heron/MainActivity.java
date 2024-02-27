package com.example.heron;

import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.SessionState;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {
    private String rootDataDir;
    private ConfigLoader config;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Context mContext = getApplicationContext();
        config = new ConfigLoader();
        try {
            config.load(mContext);
        } catch (FileNotFoundException e) {
            Intent secondActivityIntent = new Intent(
                    mContext, SiteConfig.class
            );
            startActivity(secondActivityIntent);
            Toast.makeText(mContext,"Config file does not exist. Save your config domain.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        super.onCreate(savedInstanceState);
        rootDataDir = String.valueOf(mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS));
        setContentView(R.layout.activity_main);

        Button startButton = findViewById(R.id.startbutton);
        startButton.setOnClickListener(new View.OnClickListener() {
           //@RequiresApi(api = Build.VERSION_CODES.O)
           @Override
           public void onClick(View view) {

               Log.i("Info log", "Start Button Clicked");
               String outputVideoPath = rootDataDir;
               Log.i("Info log", outputVideoPath);
               Cognito cognito = new Cognito(mContext, config);
               cognito.getCredentials();

               S3Files s3files = new S3Files(cognito.credentialsProvider, config.region, config.videobucket, outputVideoPath);
               UUID uuid = UUID.randomUUID();
               SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
               String date = s.format(new Date());

               s3files.WatchAndUpload(cognito.identityId, date, config.queue, uuid.toString(), config.configbucket);

               FFmpegKit.executeAsync("-y -thread_queue_size 512 -f android_camera -camera_index 0 -video_size 640x480 -input_queue_size 10 -i nothing" +
                       " -c:v libx264 -strftime 1 -framerate 20" +
                       " -hls_time 2 -hls_list_size 10 -hls_segment_filename '" + outputVideoPath +
                       "/back-%Y%m%d-%s.mp4' " + outputVideoPath + "/back-out.m3u8", new FFmpegSessionCompleteCallback() {
                   @Override
                   public void apply(FFmpegSession session) {
                       SessionState state = session.getState();
                       ReturnCode returnCode = session.getReturnCode();
                   }
               });
               FFmpegKit.executeAsync("-y -thread_queue_size 512 -f android_camera -camera_index 1 -video_size 640x480 -input_queue_size 10  -i nothing " +
                       " -c:v libx264 -strftime 1 -framerate 20" +
                       " -hls_time 2 -hls_list_size 10 -hls_segment_filename '" + outputVideoPath +
                       "/front-%Y%m%d-%s.mp4' " + outputVideoPath + "/front-out.m3u8", new FFmpegSessionCompleteCallback() {
                   @Override
                   public void apply(FFmpegSession session) {
                       SessionState state = session.getState();
                       ReturnCode returnCode = session.getReturnCode();
                   }
               });
           }
        });

    Button stopButton = findViewById(R.id.stopbutton);
    stopButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view)
        {
            Log.i("Info log","Stop Button Clicked" );
            FFmpegKit.cancel();
        }
    });

    Button siteConfigButton = findViewById(R.id.siteconfig);
    siteConfigButton.setOnClickListener(view -> {
        Intent secondActivityIntent = new Intent(
                mContext, SiteConfig.class
        );
        startActivity(secondActivityIntent);
    });


}




}



