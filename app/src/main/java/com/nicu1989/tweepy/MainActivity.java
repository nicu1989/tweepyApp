package com.nicu1989.tweepy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.JsonReader;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LongSummaryStatistics;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Mainact";
    ProgressDialog pd;
    Button btnHit;
    TextView txtJson;
    String url;
    boolean jobScheduled = false;
    //String jsonData;
    LinearLayout lLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lLayout = (LinearLayout) findViewById(R.id.linear);
    }

    private void parseJson(String jsonData) {
        if (true){
            Button startMonitor = findViewById(R.id.button);
            //startMonitor.setClickable(false);
            JSONArray jsonArr1 = null;
            JSONArray jsonArr2 = null;
            JSONObject jsonObj1 = null;
            JSONObject jsonObj2 = null;

            try {
                jsonArr1 = new JSONArray(jsonData);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                jsonObj1 = jsonArr1.getJSONObject(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                jsonArr2 = jsonObj1.getJSONArray("tweets");
                //jsonObj3 = jsonObj1.getJSONObject("newTweets");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            lLayout.removeAllViews();
            for(int n = 0; (n < (jsonArr2.length()) && (n < 10)); n++)
            {
                try {
                    jsonObj2 = jsonArr2.getJSONObject(n);
                    String jsonText = jsonObj2.getString("text");
                    String jsonLink = jsonObj2.getString("id");
                    int color;
                    if(n%2==0) {
                        color = 0xFFa2f1f5;
                    }
                    else{
                        color = 0xFFedf2b6;
                    }

                    setTextView(lLayout, jsonArr2, n, jsonText, color, false);
                    setTextView(lLayout, jsonArr2, n, "https://twitter.com/i/web/status/"+jsonLink, color, true);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            try {
                if(jsonObj1.getBoolean("newTweets")){
                    show_Notification();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d("MyApp", jsonObj2.toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
           Toast.makeText(context, "Message is: "+ intent.getStringExtra("message"), Toast.LENGTH_LONG)
                    .show();
                String action = intent.getAction();
                switch (action) {
                    case "msg":
                        String mess = intent.getStringExtra("message");
                        parseJson(mess);
                        break;
                }
            }

        };

        IntentFilter filter = new IntentFilter("msg");
        registerReceiver(mBroadcastReceiver,filter);
    }

    public void show_Notification(){

        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
        String CHANNEL_ID="MYCHANNEL";
        NotificationChannel notificationChannel=new NotificationChannel(CHANNEL_ID,"name", NotificationManager.IMPORTANCE_LOW);
        PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(),1,intent,0);
        Notification notification=new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentText("TweepyApp")
                .setContentTitle("You have new tweets!")
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.sym_action_chat,"TitleX",pendingIntent)
                .setChannelId(CHANNEL_ID)
                .setSmallIcon(android.R.drawable.sym_action_chat)
                .build();

        NotificationManager notificationManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
        notificationManager.notify(1,notification);
    }

    private void setTextView(LinearLayout lLayout, JSONArray jsonArr2, int n, String text, int color, boolean tweet) throws JSONException {
        String jsonText = text;
        TextView tv = new TextView(this); // Prepare textview object programmatically
        if(tweet) {
            tv.setText(Html.fromHtml("<a href=\""+jsonText+"\">Open in Twitter</a> "));
        }else{
            tv.setText(jsonText);
        }
        tv.setId(n + jsonArr2.length());
        tv.setGravity(Gravity.CENTER);
        tv.setBackgroundColor(color);
        tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        tv.setLinksClickable(true);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        lLayout.addView(tv); // Add to your ViewGroup using this method
    }

    public void startMonitor(View v){
        url = "http://192.168.0.145:3000/tweeter";

        scheduleJob();
    }

    private void scheduleJob() {
        ComponentName componentName = new ComponentName(this, Scheduler.class);

        JobInfo info = new JobInfo.Builder(123, componentName)
                .setPersisted(true)
                .setPeriodic(15 * 60 * 1000)
                .build();

        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(info);

        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Job scheduled");
        } else {
            Log.d(TAG, "Job scheduling failed");
        }
    }
}





