package com.camera.simplemjpeg;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.net.URI;

public class MjpegActivity extends Activity {
    private static final boolean DEBUG = false;
    private static final String TAG = "MJPEG";

    private MjpegView mvLeft = null;
    private MjpegView mvRight = null;

    private TextView tvLeft = null;
    private TextView tvRight = null;

    // for settings (network and resolution)
    private static final int REQUEST_SETTINGS = 0;

    private boolean suspending = false;

    final Handler handler = new Handler();
    private StreamPreferences streamPrefLeft = new StreamPreferences();
    private StreamPreferences streamPrefRight = new StreamPreferences();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getActionBar().hide();
        SharedPreferences sharedPrefs = getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);

        streamPrefLeft = loadPreferences(sharedPrefs, 1);
        streamPrefRight = loadPreferences(sharedPrefs, 2);
        //URLLeft = new String(getURL(shPrefLeft));

        setContentView(R.layout.main);
        mvLeft = (MjpegView) findViewById(R.id.mvLeft);
        if (mvLeft != null) {
            mvLeft.setResolution(streamPrefLeft.getWidth(), streamPrefLeft.getHeight());
        }
        tvLeft = (TextView) findViewById(R.id.tvLeft);
        setTvLeft(getResources().getString(R.string.title_connecting));

        mvRight = (MjpegView) findViewById(R.id.mvRight);
        if (mvRight != null) {
            mvRight.setResolution(streamPrefRight.getWidth(), streamPrefRight.getHeight());
        }
        tvRight = (TextView) findViewById(R.id.tvRight);
        setTvRight(getResources().getString(R.string.title_connecting));
        //new DoReadLeft().execute(streamPrefLeft.getURL());
        //new DoReadRight().execute(streamPrefRight.getURL());
        new DoReadLeft().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, streamPrefLeft.getURL());
        new DoReadRight().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, streamPrefRight.getURL());

    }


    public void onResume() {
        if (DEBUG) Log.d(TAG, "onResume()");
        super.onResume();
        if (mvLeft != null) {
            if (suspending) {
                new DoReadLeft().execute(streamPrefLeft.getURL());
                suspending = false;
            }
        }
        if (mvRight != null) {
            if (suspending) {
                new DoReadRight().execute(streamPrefRight.getURL());
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

        if (mvLeft != null) {
            mvLeft.freeCameraMemory();
        }
        if (mvRight != null) {
            mvRight.freeCameraMemory();
        }

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.layout.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*switch (item.getItemId()) {
            case R.id.settings:
                Intent settings_intent = new Intent(MjpegActivity.this, SettingsActivity.class);
                settings_intent.putExtra("width", width);
                settings_intent.putExtra("height", height);
                settings_intent.putExtra("ip_ad1", ip_ad1);
                settings_intent.putExtra("ip_ad2", ip_ad2);
                settings_intent.putExtra("ip_ad3", ip_ad3);
                settings_intent.putExtra("ip_ad4", ip_ad4);
                settings_intent.putExtra("ip_port", ip_port);
                startActivityForResult(settings_intent, REQUEST_SETTINGS);
                return true;
        }*/
        return false;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SETTINGS:
                if (resultCode == Activity.RESULT_OK) {
                    int cam_number = data.getIntExtra("cam_number", 1);
                    if(cam_number == 1){ //left cam
                        streamPrefLeft.setWidth(data.getIntExtra("width", streamPrefLeft.getWidth()));
                        streamPrefLeft.setHeight(data.getIntExtra("height", streamPrefLeft.getHeight()));
                        streamPrefLeft.setIp_ad1(data.getIntExtra("ip_ad1", streamPrefLeft.getIp_ad1()));
                        streamPrefLeft.setIp_ad2(data.getIntExtra("ip_ad2", streamPrefLeft.getIp_ad2()));
                        streamPrefLeft.setIp_ad3(data.getIntExtra("ip_ad3", streamPrefLeft.getIp_ad3()));
                        streamPrefLeft.setIp_ad4(data.getIntExtra("ip_ad4", streamPrefLeft.getIp_ad4()));
                        streamPrefLeft.setIp_port(data.getIntExtra("ip_port", streamPrefLeft.getIp_port()));

                        if (mvLeft != null) {
                            mvLeft.setResolution(streamPrefLeft.getWidth(), streamPrefLeft.getHeight());
                        }
                    }else if (cam_number == 2){ //right_cam
                        streamPrefRight.setWidth(data.getIntExtra("width", streamPrefRight.getWidth()));
                        streamPrefRight.setHeight(data.getIntExtra("height", streamPrefRight.getHeight()));
                        streamPrefRight.setIp_ad1(data.getIntExtra("ip_ad1", streamPrefRight.getIp_ad1()));
                        streamPrefRight.setIp_ad2(data.getIntExtra("ip_ad2", streamPrefRight.getIp_ad2()));
                        streamPrefRight.setIp_ad3(data.getIntExtra("ip_ad3", streamPrefRight.getIp_ad3()));
                        streamPrefRight.setIp_ad4(data.getIntExtra("ip_ad4", streamPrefRight.getIp_ad4()));
                        streamPrefRight.setIp_port(data.getIntExtra("ip_port", streamPrefRight.getIp_port()));

                        if (mvRight != null) {
                            mvRight.setResolution(streamPrefRight.getWidth(), streamPrefRight.getHeight());
                        }
                    }
                    SharedPreferences preferences = getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    savePreferences(editor, streamPrefLeft, 1);
                    savePreferences(editor, streamPrefRight, 2);
                    editor.commit();

                    new RestartApp().execute();
                }
                break;
        }
    }

    public void setImageError() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                //setTitle(R.string.title_imageerror);
                setTvLeft(getResources().getString(R.string.title_imageerror));
                setTvRight(getResources().getString(R.string.title_imageerror));
                return;
            }
        });
    }

    public class DoReadLeft extends AsyncTask<String, Void, MjpegInputStream> {
        protected MjpegInputStream doInBackground(String... url) {
            //TODO: if camera has authentication deal with it and don't just not work
            HttpResponse res = null;
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpParams httpParams = httpclient.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 5 * 1000);
            HttpConnectionParams.setSoTimeout(httpParams, 5 * 1000);
            if (DEBUG) Log.d(TAG, "1. Sending http request");
            try {
                res = httpclient.execute(new HttpGet(URI.create(url[0])));
                if (DEBUG)
                    Log.d(TAG, "2. Request finished, status = " + res.getStatusLine().getStatusCode());
                if (res.getStatusLine().getStatusCode() == 401) {
                    //You must turn off camera User Access Control before this will work
                    return null;
                }
                return new MjpegInputStream(res.getEntity().getContent());
            } catch (ClientProtocolException e) {
                if (DEBUG) {
                    e.printStackTrace();
                    Log.d(TAG, "Request failed-ClientProtocolException", e);
                }
                //Error connecting to camera
            } catch (IOException e) {
                if (DEBUG) {
                    e.printStackTrace();
                    Log.d(TAG, "Request failed-IOException", e);
                }
                //Error connecting to camera
            }
            return null;
        }

        protected void onPostExecute(MjpegInputStream result) {
            mvLeft.setSource(result);
            if (result != null) {
                result.setSkip(1);
                //setTitle(R.string.app_name);
                setTvLeft("Connected : "+streamPrefLeft.getURL());
            } else {
                //setTitle(R.string.title_disconnected);
                setTvLeft(getResources().getString(R.string.title_disconnected));

            }
            mvLeft.setDisplayMode(MjpegView.SIZE_BEST_FIT);
            mvLeft.showFps(false);
        }
    }
    public class DoReadRight extends AsyncTask<String, Void, MjpegInputStream> {
        protected MjpegInputStream doInBackground(String... url) {
            //TODO: if camera has authentication deal with it and don't just not work
            HttpResponse res = null;
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpParams httpParams = httpclient.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 5 * 1000);
            HttpConnectionParams.setSoTimeout(httpParams, 5 * 1000);
            if (DEBUG) Log.d(TAG, "1. Sending http request");
            try {
                res = httpclient.execute(new HttpGet(URI.create(url[0])));
                if (DEBUG)
                    Log.d(TAG, "2. Request finished, status = " + res.getStatusLine().getStatusCode());
                if (res.getStatusLine().getStatusCode() == 401) {
                    //You must turn off camera User Access Control before this will work
                    return null;
                }
                return new MjpegInputStream(res.getEntity().getContent());
            } catch (ClientProtocolException e) {
                if (DEBUG) {
                    e.printStackTrace();
                    Log.d(TAG, "Request failed-ClientProtocolException", e);
                }
                //Error connecting to camera
            } catch (IOException e) {
                if (DEBUG) {
                    e.printStackTrace();
                    Log.d(TAG, "Request failed-IOException", e);
                }
                //Error connecting to camera
            }
            return null;
        }

        protected void onPostExecute(MjpegInputStream result) {
            mvRight.setSource(result);
            if (result != null) {
                result.setSkip(1);
                //setTitle(R.string.app_name);
                setTvRight("Connected : "+streamPrefLeft.getURL());
            } else {
                //setTitle(R.string.title_disconnected);
                setTvRight(getResources().getString(R.string.title_disconnected));

            }
            mvRight.setDisplayMode(MjpegView.SIZE_BEST_FIT);
            mvRight.showFps(false);
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
        int[] ip = {prefs.getInt("ip_ad1"+num, 192),prefs.getInt("ip_ad2"+num, 168),prefs.getInt("ip_ad3"+num, 2),prefs.getInt("ip_ad4"+num, 1), prefs.getInt("ip_port"+num, 8080)};
        for (int i = 0; i< ip.length; i++){
            Log.d("MJPEG_Cam"+num, "arg"+i+" : "+ip[i]);
        }
        StreamPreferences temp = new StreamPreferences(ip,prefs.getInt("width"+num, 640),prefs.getInt("height"+num, 480));
        Log.d("MJPEG_Cam"+num, "URL "+temp.getURL()+" loaded at startup.");
        return temp;
    }
    private void savePreferences(SharedPreferences.Editor editor, StreamPreferences streamPrefs, int num){
        editor.putInt("width"+num, streamPrefs.getWidth());
        editor.putInt("height"+num, streamPrefs.getHeight());
        editor.putInt("ip_ad1"+num, streamPrefs.getIp_ad1());
        editor.putInt("ip_ad2"+num, streamPrefs.getIp_ad2());
        editor.putInt("ip_ad3"+num, streamPrefs.getIp_ad3());
        editor.putInt("ip_ad4"+num, streamPrefs.getIp_ad4());
        editor.putInt("ip_port"+num, streamPrefs.getIp_port());
    }
    public void openSettingsLeft(View v){
        Intent intent = new Intent(this, SettingsActivity.class);
        Intent settings_intent = new Intent(MjpegActivity.this, SettingsActivity.class);
        settings_intent.putExtra("width", streamPrefLeft.getWidth());
        settings_intent.putExtra("height", streamPrefLeft.getHeight());
        settings_intent.putExtra("ip_ad1", streamPrefLeft.getIp_ad1());
        settings_intent.putExtra("ip_ad2", streamPrefLeft.getIp_ad2());
        settings_intent.putExtra("ip_ad3", streamPrefLeft.getIp_ad3());
        settings_intent.putExtra("ip_ad4", streamPrefLeft.getIp_ad4());
        settings_intent.putExtra("ip_port", streamPrefLeft.getIp_port());
        settings_intent.putExtra("cam_number", 1);
        startActivityForResult(settings_intent, REQUEST_SETTINGS);

    }
    public void setTvLeft(String text){
        tvLeft.setText(text);
    }
    public void setTvRight(String text){
        tvRight.setText(text);
    }
    public void openSettingsRight(View v){
        Intent intent = new Intent(this, SettingsActivity.class);
        Intent settings_intent = new Intent(MjpegActivity.this, SettingsActivity.class);
        settings_intent.putExtra("width", streamPrefRight.getWidth());
        settings_intent.putExtra("height", streamPrefRight.getHeight());
        settings_intent.putExtra("ip_ad1", streamPrefRight.getIp_ad1());
        settings_intent.putExtra("ip_ad2", streamPrefRight.getIp_ad2());
        settings_intent.putExtra("ip_ad3", streamPrefRight.getIp_ad3());
        settings_intent.putExtra("ip_ad4", streamPrefRight.getIp_ad4());
        settings_intent.putExtra("ip_port", streamPrefRight.getIp_port());
        Log.d("MJPEG_Cam"+2, "sent to settings : ip_port="+streamPrefRight.getIp_port());
        settings_intent.putExtra("cam_number", 2);
        startActivityForResult(settings_intent, REQUEST_SETTINGS);

    }
}
