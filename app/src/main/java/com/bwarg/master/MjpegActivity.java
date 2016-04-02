package com.bwarg.master;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.UUID;

import eu.hgross.blaubot.android.BlaubotAndroid;
import eu.hgross.blaubot.android.BlaubotAndroidFactory;
import eu.hgross.blaubot.android.wifi.BlaubotWifiAdapter;
import eu.hgross.blaubot.core.BlaubotDevice;
import eu.hgross.blaubot.core.IBlaubotAdapter;
import eu.hgross.blaubot.core.IBlaubotDevice;
import eu.hgross.blaubot.core.ILifecycleListener;
import eu.hgross.blaubot.core.acceptor.discovery.IBlaubotBeacon;
import eu.hgross.blaubot.ethernet.BlaubotBonjourBeacon;
import eu.hgross.blaubot.ethernet.BlaubotEthernetAdapter;
import eu.hgross.blaubot.messaging.BlaubotMessage;
import eu.hgross.blaubot.messaging.IBlaubotChannel;
import eu.hgross.blaubot.messaging.IBlaubotMessageListener;

public class MjpegActivity extends ActionBarActivity {
    private static final boolean DEBUG = false;
    private static final String TAG = "MJPEG";
    private static final String TAG_BLAUBOT = TAG + "-BLAUBOT";

    private static final String APP_UUID_STRING = "52260110-f8f0-11e5-a837-0800200c9a66";
    private static final int APP_PORT = 5606;
    private BlaubotAndroid blaubot;
    private IBlaubotChannel mainChannel;

    private MjpegView mvLeft = null;
    private MjpegView mvRight = null;
    private ImageButton exposure_lock_button;

    private LinearLayout statusLayout = null;

    private TextView tvLeft = null;
    private TextView tvRight = null;

    // for cam_settings (network and resolution)
    private static final int REQUEST_SETTINGS = 0;

    private boolean suspending = false;
    //public static boolean SHOW_FPS = true;
    //upper right log on view
    //public static boolean SHOW_CAMERA_STATUS = true;
    public static boolean VERBOSE_MODE = false;
    public static boolean AUTO_EXPOSURE_LOCK = false;

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
        //SHOW_FPS = sharedPrefs.getBoolean("show_fps", true);
        //SHOW_CAMERA_STATUS = sharedPrefs.getBoolean("show_status", true);
        VERBOSE_MODE = sharedPrefs.getBoolean("hide_mode", false);
        exposure_lock_button = (ImageButton) findViewById(R.id.auto_exposure_lock_button);

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

        mvLeft.showFps(VERBOSE_MODE);
        mvRight.showFps(VERBOSE_MODE);

        statusLayout = (LinearLayout) findViewById(R.id.camera_status_layout);
        if (VERBOSE_MODE) {
            statusLayout.setVisibility(View.VISIBLE);
        }
        else {
            statusLayout.setVisibility(View.INVISIBLE);
        }
        final UUID APP_UUID = UUID.fromString(APP_UUID_STRING);
        //blaubot = BlaubotAndroidFactory.createEthernetBlaubotWithBluetoothBeacon(APP_UUID, APP_PORT, tryGetIpV4Address());
        IBlaubotBeacon beacon = new BlaubotBonjourBeacon(tryGetIpV4Address(), APP_PORT);
        IBlaubotAdapter adapter = new BlaubotEthernetAdapter(new BlaubotDevice(),APP_PORT+1,tryGetIpV4Address());
        //blaubot = BlaubotAndroidFactory.createBlaubot(APP_UUID_STRING,adapter, beacon);
        blaubot = BlaubotAndroidFactory.createEthernetBlaubotWithBluetoothBeacon(APP_UUID, APP_PORT, tryGetIpV4Address());
        blaubot.startBlaubot();
        blaubot.registerReceivers(this);
        blaubot.setContext(this);
        //blaubot.onResume(this);

        mainChannel = blaubot.createChannel((short)1);
        mainChannel.publish("Channel started".getBytes());
        mainChannel.subscribe(new IBlaubotMessageListener() {
            @Override
            public void onMessage(BlaubotMessage message) {
                // we got a message - our payload is a byte array
                // deserialize
                String msg = new String(message.getPayload());
                //Structure of messages : HEADER_[fromID]XXX_[toID]XXX_DATA
                String header = msg.substring(0,2);
                int fromIDIndex = msg.indexOf("_[fromID]")+9;
                int toIDIndex = msg.indexOf("_[toID]")+7;
                int dataIndex = msg.indexOf("_[data]")+7;

                String fromID = msg.substring(fromIDIndex, toIDIndex-7);
                String toID = msg.substring(toIDIndex, dataIndex-7);
                String data = msg.substring(dataIndex);

                switch(header){
                    case "SSP" : //manage complete settings rewrite here
                        if(toID.equals(blaubot.getOwnDevice().getUniqueDeviceID())) {
                            SlaveStreamPreferences prefs = SlaveStreamPreferences.fromGson(data);
                            if(prefs.getPreferredSide() == SlaveStreamPreferences.SIDE_LEFT){
                                streamPrefLeft.copyFrom(prefs);
                                streamPrefLeft.setDeviceUniqueId(fromID);
                            }else if(prefs.getPreferredSide() == SlaveStreamPreferences.SIDE_RIGHT){
                                streamPrefRight.copyFrom(prefs);
                                streamPrefRight.setDeviceUniqueId(fromID);
                            }
                        }
                        break;
                    default : break;
                }
                Log.i(TAG_BLAUBOT, "Received from channel : " + msg);
            }
        });
        blaubot.addLifecycleListener(new ILifecycleListener() {
            @Override
            public void onDisconnected() {
                // THIS device disconnected from the network
                Log.i(TAG_BLAUBOT, "Disconnected.");
            }

            @Override
            public void onDeviceLeft(IBlaubotDevice blaubotDevice) {
                // ANOTHER device disconnected from the network
            }

            @Override
            public void onDeviceJoined(IBlaubotDevice blaubotDevice) {
                // ANOTHER device connected to the network THIS device is on
            }

            @Override
            public void onConnected() {
                // THIS device connected to a network
                // you can now subscribe to channels and use them:
                mainChannel.subscribe();
                mainChannel.publish("Master is online".getBytes());
                // onDeviceJoined(...) calls will follow for each OTHER device that was already connected
            }

            @Override
            public void onPrinceDeviceChanged(IBlaubotDevice oldPrince, IBlaubotDevice newPrince) {
                // if the network's king goes down, the prince will rule over the remaining peasants
            }

            @Override
            public void onKingDeviceChanged(IBlaubotDevice oldKing, IBlaubotDevice newKing) {

            }
        });

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
        blaubot.startBlaubot();
        blaubot.registerReceivers(this);
        blaubot.setContext(this);
        //blaubot.onResume(this);

    }

    public void onStart() {
        if (DEBUG) Log.d(TAG, "onStart()");
        super.onStart();
        blaubot.startBlaubot();
        blaubot.registerReceivers(this);
        blaubot.setContext(this);
        //blaubot.onResume(this);
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
        blaubot.unregisterReceivers(this);
        blaubot.onPause(this);
    }

    public void onStop() {
        if (DEBUG) Log.d(TAG, "onStop()");
        super.onStop();
        blaubot.stopBlaubot();
    }

    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "onDestroy()");

        /*if (mvLeft != null) {
            mvLeft.freeCameraMemory();
        }
        if (mvRight != null) {
            mvRight.freeCameraMemory();
        }*/
        blaubot.stopBlaubot();
        SharedPreferences preferences = getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        savePreferences(editor, streamPrefLeft, 1);
        savePreferences(editor, streamPrefRight, 2);
        //editor.putBoolean("show_fps", SHOW_FPS);
        //editor.putBoolean("show_status", SHOW_CAMERA_STATUS);
        editor.putBoolean("hide_mode", VERBOSE_MODE);
        editor.commit();

        super.onDestroy();
    }

    public void restartApp(View v){
        SharedPreferences preferences = getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        savePreferences(editor, streamPrefLeft, 1);
        savePreferences(editor, streamPrefRight, 2);
        //editor.putBoolean("show_fps", SHOW_FPS);
        //editor.putBoolean("show_status", SHOW_CAMERA_STATUS);
        editor.putBoolean("hide_mode", VERBOSE_MODE);
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
            mView.showFps(VERBOSE_MODE);
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
        editor.putString("stream_prefs" + num, gson.toJson(streamPrefs));
    }
    public void openSettings(View v){
        Intent intent = new Intent(this, CamSettingsActivity.class);
        Intent settings_intent = new Intent(MjpegActivity.this, GeneralSettingsActivity.class);
        Gson gson = new Gson();
        settings_intent.putExtra("stream_prefs1",gson.toJson(streamPrefLeft));
        settings_intent.putExtra("stream_prefs2",gson.toJson(streamPrefRight));

        startActivityForResult(settings_intent, REQUEST_SETTINGS);

    }
    public void toggleExposureLock(View v) {
        AUTO_EXPOSURE_LOCK = !AUTO_EXPOSURE_LOCK;
        exposure_lock_button.setImageResource(AUTO_EXPOSURE_LOCK ? R.drawable.exposure_unlocked : R.drawable.exposure_locked);
        mainChannel.publish(("AEL_[fromID]"+blaubot.getOwnDevice().getUniqueDeviceID()+"_[toID]"+streamPrefLeft.getDeviceUniqueId()+AUTO_EXPOSURE_LOCK).getBytes());
        mainChannel.publish(("AEL_[fromID]"+blaubot.getOwnDevice().getUniqueDeviceID()+"_[toID]"+streamPrefRight.getDeviceUniqueId()+AUTO_EXPOSURE_LOCK).getBytes());

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
    private static InetAddress tryGetIpV4Address()
    {
        try
        {
            final Enumeration<NetworkInterface> en =
                    NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements())
            {
                final NetworkInterface intf = en.nextElement();
                final Enumeration<InetAddress> enumIpAddr =
                        intf.getInetAddresses();
                while (enumIpAddr.hasMoreElements())
                {
                    final  InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress())
                    {
                        final String addr = inetAddress.getHostAddress().toUpperCase();
                        if (InetAddressUtils.isIPv4Address(addr))
                        {
                            return inetAddress;
                        }
                    } // if
                } // while
            } // for
        } // try
        catch (final Exception e)
        {
            // Ignore
        } // catch
        return null;
    } // tryGetIpV4Address()
}
