package com.example.heron;

import android.os.FileObserver;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.sqs.AmazonSQSClient;

import java.io.File;


public class S3Files {
    private AmazonS3Client s3client;
    private AmazonSQSClient sqsclient;
    private FileObserver observer;

    private String identityID;
    private String date;


    public S3Files(CognitoCachingCredentialsProvider credentialsProvider, String awsRegion, String videobucket, String directory) {
        Region region = Region.getRegion(Regions.fromName(awsRegion));
        s3client = new AmazonS3Client(credentialsProvider, region );
        sqsclient = new AmazonSQSClient(credentialsProvider);
        Log.d("watching:", directory);
        observer = new FileObserver(directory) {
            @Override
            public void onEvent(int event, String file) {

                    if (!file.endsWith(".tmp"))
                        if (event == FileObserver.CLOSE_WRITE ) {
                            Log.d("videowritten " + event + ":", "File created [" + directory + "/" + file + "]");
                            File fileVideo = new File(directory + "/" + file);

                            String keyprefix = identityID + "/" + date + "/";
                            Log.d("The S3 key prefix:", keyprefix);

                            PutObjectRequest put = new PutObjectRequest(videobucket, keyprefix + file, fileVideo);
                            s3client.putObject(put);
                            File frontFileList = new File(directory + "/front-out.m3u8");
                            PutObjectRequest putFrontList = new PutObjectRequest(videobucket, keyprefix + "front-out.m3u8", frontFileList);
                            s3client.putObject(putFrontList);
                            File backFileList = new File(directory + "/back-out.m3u8");
                            PutObjectRequest putBackList = new PutObjectRequest(videobucket, keyprefix + "back-out.m3u8", backFileList);
                            s3client.putObject(putBackList);
                            fileVideo.delete();

                        }
            }
        };
    }

    public void WatchAndUpload(String identityID, String date, String queue, String uuid, String configbucket) {
        this.identityID = identityID;
        this.date = date;

        s3client.putObject(configbucket, identityID +"/uuid",  uuid);
        sqsclient.sendMessage(queue, "{\"date\":\"" +date+"\",\"identityID\":\""+identityID+"\", \"uuid\":\""+uuid+"\"}");
        observer.startWatching();
    }
    public void Signal() {

    }
}

