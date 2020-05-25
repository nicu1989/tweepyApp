package com.nicu1989.tweepy;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Scheduler extends JobService {
    String requestUrl = "http://192.168.0.145:3000/tweeter";
    private static final String TAG = "Scheduler";
    BufferedReader reader = null;
    StringBuffer buffer;
    @Override
    public boolean onStartJob(JobParameters params) {
        doBackgroundWork(params);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private void doBackgroundWork(final JobParameters params) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;

                try {
                    URL url = new URL(requestUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();


                    InputStream stream = connection.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    buffer = new StringBuffer();
                    String line = "";

                    while ((line = reader.readLine()) != null) {
                        buffer.append(line+"\n");
                        Log.d("Response: ", "> " + line);
                    }

                    Intent activ = new Intent(getBaseContext().getApplicationContext(), MainActivity.class);
                    activ.putExtra("jobScheduled", true);
                    activ.putExtra("json", buffer.toString());
                    activ.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(activ);


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "Job finished");
                jobFinished(params, false);
            }
        }).start();
    }

    public String getJson(){
        return buffer.toString();
    }

}
