package com.nicu1989.tweepy;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
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
    String jsonData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout lLayout = (LinearLayout) findViewById(R.id.linear);

        String requestUrl = "http://192.168.0.145:3000/tweeter";

        try{
            Intent intent = getIntent();
            jobScheduled = intent.getBooleanExtra("jobScheduled", false);
            jsonData = intent.getStringExtra("json");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (jobScheduled){
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
            } catch (JSONException e) {
                e.printStackTrace();
            }

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
                // do some stuff....
            }

            Log.d("MyApp", jsonObj2.toString());
        }

        /*final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 6000);
                new JsonTask().execute(url);
                Toast.makeText(getApplicationContext(), "AsyncTask runs every 6 seconds", Toast.LENGTH_SHORT).show();
            }
        });*/

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

    /*private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


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
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }
            txtJson = findViewById(R.id.tv1);
            txtJson.setText(result);
        }
    }*/
}



