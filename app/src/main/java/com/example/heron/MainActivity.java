package com.example.heron;



import static android.system.Os.mkfifo;
import static android.system.OsConstants.S_IRWXO;
import static androidx.core.content.ContentProviderCompat.requireContext;
import static com.google.android.material.internal.ContextUtils.getActivity;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.media.MediaRecorder;
import android.net.LocalSocketAddress;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback;
import com.arthenica.ffmpegkit.LogCallback;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.SessionState;
import com.arthenica.ffmpegkit.Statistics;
import com.arthenica.ffmpegkit.StatisticsCallback;
import com.example.heron.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import android.net.LocalSocket;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {
    private String region;
    private String userPoolID;
    private String clientID;
    private String identityPoolID;

    private String videobucket;
    private String configbucket;

    private String queue;

    private UUID uuid;
    private ActivityMainBinding binding;
    private MediaRecorder recorder;
    private File audiofile;
    private String rootDataDir;

    private FileDescriptor outputPipe;
    private FileDescriptor inputPipe;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Context mContext = getApplicationContext();
        File file = new File(
                mContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "heronconfig.json"
        );
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
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
            bufferedReader.close();// This responce will have Json Format String
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
            queue = jsonObject.get("SignalQueueURL").toString();
            userPoolID = jsonObject.get("UserPoolId").toString();
            region = jsonObject.get("Region").toString();
            clientID = jsonObject.get("ClientId").toString();
            identityPoolID = jsonObject.get("IdentityPoolId").toString();
            configbucket = jsonObject.get("ConfigBucket").toString();
            videobucket = jsonObject.get("VideoBucket").toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        Log.i("config", "queue " + queue);
        Log.i("config", "userpool " + userPoolID);
        Log.i("config", "region " + region);
        Log.i("config", "client " + clientID);
        Log.i("config", "identitypool " + identityPoolID);
        Log.i("config", "config bucket " + configbucket);
        Log.i("config", "videobucket " + videobucket);

        super.onCreate(savedInstanceState);
        rootDataDir = String.valueOf(mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS));
        //File externalCacheFile = new File(mContext.getExternalCacheDir(), "audio.socket");


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        View startButton = findViewById(R.id.startbutton);
        startButton.setOnClickListener(new View.OnClickListener() {
                                           @RequiresApi(api = Build.VERSION_CODES.O)
                                           @Override
                                           public void onClick(View view) {



                                               Log.i("Info log", "Start Button Clicked");
                                               String outputVideoPath = rootDataDir;
                                               Log.i("Info log", outputVideoPath);
                                               Cognito cognito = new Cognito(mContext, region, userPoolID, clientID, identityPoolID);
                                               cognito.userLogin("TODO","TODO");

                                               S3Files s3files = new S3Files(cognito.credentialsProvider, region, videobucket, outputVideoPath);
                                               UUID uuid = UUID.randomUUID();
                                               SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
                                               String date = s.format(new Date());

                                               s3files.WatchAndUpload(cognito.identityId, date, queue, uuid.toString(), configbucket);

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

        View stopButton = findViewById(R.id.stopbutton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Log.i("Info log","Stop Button Clicked" );
                FFmpegKit.cancel();
            }
        });


    }




}



