package com.bwarg.master;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

public class MjpegActivity extends ActionBarActivity {
    private static final boolean DEBUG = false;
    private static final String TAG = "MJPEG";

    private MjpegView mvLeft = null;
    private MjpegView mvRight = null;

    private LinearLayout statusLayout = null;

    private TextView tvLeft = null;
    private TextView tvRight = null;

    // for cam_settings (network and resolution)
    private static final int REQUEST_SETTINGS = 0;

    private boolean suspending = false;
    public static boolean SHOW_FPS = true;
    //upper right log on view
    public static boolean SHOW_CAMERA_STATUS = true;

    final Handler handler = new Handler();
    private StreamPreferences streamPrefLeft = new StreamPreferences();
    private StreamPreferences streamPrefRight = new StreamPreferences();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getActionBar().hide();
        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        //getActionBar().hide();
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Gson gson = new Gson();

        SharedPreferences sharedPrefs = getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
        SHOW_FPS = sharedPrefs.getBoolean("show_fps", true);
        SHOW_CAMERA_STATUS = sharedPrefs.getBoolean("show_status", true);

        streamPrefLeft = loadPreferences(sharedPrefs, 1);
        streamPrefRight = loadPreferences(sharedPrefs, 2);
        //URLLeft = new String(getURL(shPrefLeft));

        setContentView(R.layout.main);
        mvLeft = (MjpegView) findViewById(R.id.mvLeft);
        mvLeft.setCamNum(1);
        if (mvLeft != null) {
            mvLeft.setResolution(streamPrefLeft.getWidth(), streamPrefLeft.getHeight());
        }
        tvLeft = (TextView) findViewById(R.id.tvLeft);
        setTv(1, getResources().getString(R.string.title_connecting));

        mvRight = (MjpegView) findViewById(R.id.mvRight);
        mvRight.setCamNum(2);
        if (mvRight != null) {
            mvRight.setResolution(streamPrefRight.getWidth(), streamPrefRight.getHeight());
        }
        tvRight = (TextView) findViewById(R.id.tvRight);
        setTv(2, getResources().getString(R.string.title_connecting));

        mvLeft.showFps(SHOW_FPS);
        mvRight.showFps(SHOW_FPS);

        statusLayout = (LinearLayout) findViewById(R.id.camera_status_layout);
        if (SHOW_CAMERA_STATUS) {
            statusLayout.setVisibility(View.VISIBLE);
        }
        else {
            statusLayout.setVisibility(View.INVISIBLE);
        }

        new DoRead(mvLeft, 1, streamPrefLeft).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, streamPrefLeft.getURL()+streamPrefLeft.getCommand());
        new DoRead(mvRight, 2, streamPrefRight).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, streamPrefRight.getURL()+streamPrefRight.getCommand());

    }


    public void onResume() {
        if (DEBUG) Log.d(TAG, "onResume()");
        super.onResume();

        if (mvLeft != null) {
            if (suspending) {
                new DoRead(mvLeft, 1, streamPrefLeft).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, streamPrefLeft.getURL() + streamPrefLeft.getCommand());
                suspending = false;
            }
        }
        if (mvRight != null) {
            if (suspending) {
                new DoRead(mvRight, 2, streamPrefRight).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, streamPrefRight.getURL() + streamPrefRight.getCommand());
                suspending = false;
            }
        }

    }

    public void onStart() {
        if (DEBUG) Log.d(TAG, "onStart()");
        super.onStart();
    }

    public void onPause() {
        if (DEBUG) Log.d(TAG, "onPause()");
        super.onPause();
        if (mvLeft != null) {
            if (mvLeft.isStreaming()) {
                mvLeft.stopPlayback();
                suspending = true;
            }
        }
        if (mvRight != null) {
            if (mvRight.isStreaming()) {
                mvRight.stopPlayback();
                suspending = true;
            }
        }
    }

    public void onStop() {
        if (DEBUG) Log.d(TAG, "onStop()");
        super.onStop();
    }

    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "onDestroy()");

        /*if (mvLeft != null) {
            mvLeft.freeCameraMemory();
        }
        if (mvRight != null) {
            mvRight.freeCameraMemory();
        }*/

        super.onDestroy();
    }

    public void restartApp(View v){
        SharedPreferences preferences = getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        savePreferences(editor, streamPrefLeft, 1);
        savePreferences(editor, streamPrefRight, 2);
        editor.putBoolean("show_fps", SHOW_FPS);
        editor.putBoolean("show_status", SHOW_CAMERA_STATUS);
        editor.commit();

        new RestartApp().execute();
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SETTINGS:
                if (resultCode == Activity.RESULT_OK) {
                    //left cam
                    Gson gson = new Gson();
                    streamPrefLeft = gson.fromJson(data.getStringExtra("stream_prefs1"), StreamPreferences.class);

                    if (mvLeft != null) {
                        mvLeft.setResolution(streamPrefLeft.getWidth(), streamPrefLeft.getHeight());
                    }
                    //right_cam
                    gson = new Gson();
                    streamPrefRight = gson.fromJson(data.getStringExtra("stream_prefs2"), StreamPreferences.class);


                    if (mvRight != null) {
                        mvRight.setResolution(streamPrefRight.getWidth(), streamPrefRight.getHeight());
                    }

                    restartApp(null);
                }
                break;
        }
    }
    public void setImageError(final int camNum, final int string_ressource_id){
        handler.post(new Runnable() {
            @Override
            public void run() {
                    setTv(camNum,getResources().getString(string_ressource_id));
                return;
            }
        });
    }
    public void setImageError(final int camNum) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                    setTv(camNum,getResources().getString(R.string.title_imageerror));
                return;
            }
        });
    }

    public class DoRead extends AsyncTask<String, Void, MjpegInputStream> {
        protected  MjpegView mView = null;
        protected int camNum = 1;
        protected StreamPreferences streamPrefs = null;

        protected  DoRead(MjpegView mView, int camNum, StreamPreferences streamPrefs){
            this.mView=mView;
            this.camNum = camNum;
            this.streamPrefs = streamPrefs;
        }
        protected MjpegInputStream doInBackground(String... url) {
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection)(new URL(url[0])).openConnection();
                return new MjpegInputStream(urlConnection.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(MjpegInputStream result) {
            mView.setSource(result);
            if (result != null) {
                result.setSkip(1);
                setTv(camNum, getResources().getString(R.string.connected_to)+" ["+streamPrefs.getName()+"] @"+streamPrefs.getURL()+streamPrefs.getCommand());
            } else {
                setTv(camNum, getResources().getString(R.string.title_disconnected));

            }
            mView.setDisplayMode(MjpegView.SIZE_BEST_FIT);
            mView.showFps(SHOW_FPS);
        }
    }

    public class RestartApp extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... v) {
            MjpegActivity.this.finish();
            return null;
        }

        protected void onPostExecute(Void v) {
            startActivity((new Intent(MjpegActivity.this, MjpegActivity.class)));
        }
    }
    private StreamPreferences loadPreferences(SharedPreferences prefs, int num){
        Gson gson = new Gson();
        StreamPreferences temp = gson.fromJson(prefs.getString("stream_prefs"+num, StreamPreferences.defaultGsonString()), StreamPreferences.class);

        Log.d("MJPEG_Cam" + num, "URL " + temp.getURL()+temp.getCommand() + " loaded at startup.");
        return temp;
    }
    private void savePreferences(SharedPreferences.Editor editor, StreamPreferences streamPrefs, int num){
        Gson gson = new Gson();
        editor.putString("stream_prefs"+num, gson.toJson(streamPrefs));
    }
    public void openSettings(View v){
        Intent intent = new Intent(this, CamSettingsActivity.class);
        Intent settings_intent = new Intent(MjpegActivity.this, GeneralSettingsActivity.class);
        Gson gson = new Gson();
        settings_intent.putExtra("stream_prefs1",gson.toJson(streamPrefLeft));
        settings_intent.putExtra("stream_prefs2",gson.toJson(streamPrefRight));

        startActivityForResult(settings_intent, REQUEST_SETTINGS);

    }
    public void setTv(int camNum, String s){
        if(camNum == 1){
            tvLeft.setText(s);
        }else if (camNum ==2){
            tvRight.setText(s);
        }
    }
    public void setTv(int camNum, int string_ressource_id){
        setTv(camNum, getResources().getString(string_ressource_id));
    }
}
