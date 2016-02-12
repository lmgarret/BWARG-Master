package com.camera.simplemjpeg;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class GeneralSettingsActivity extends ActionBarActivity {
    StreamPreferences streamPrefLeft = new StreamPreferences();
    StreamPreferences streamPrefRight = new StreamPreferences();
    private static final int REQUEST_SETTINGS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getResources().getString(R.string.title_settings_general));
        setContentView(R.layout.activity_general_settings);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            loadExtras(extras, streamPrefLeft, 1);
            loadExtras(extras, streamPrefRight, 2);
        }else{
            Log.d("GeneralSettings", " null args received");
        }
        Button settings_done = (Button) findViewById(R.id.general_settings_done);
        settings_done.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {

                        Intent intent = new Intent();
                        putExtras(intent, streamPrefLeft, 1);
                        putExtras(intent, streamPrefRight, 2);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
        );
    }
    private void loadExtras(Bundle extras, StreamPreferences prefs, int camNum) {
        prefs.setWidth(extras.getInt("width" + camNum, prefs.getWidth()));
        prefs.setHeight(extras.getInt("height" + camNum, prefs.getHeight()));

        prefs.setIp_ad1(extras.getInt("ip_ad1" + camNum, prefs.getIp_ad1()));
        prefs.setIp_ad2(extras.getInt("ip_ad2" + camNum, prefs.getIp_ad2()));
        prefs.setIp_ad3(extras.getInt("ip_ad3" + camNum, prefs.getIp_ad3()));
        prefs.setIp_ad4(extras.getInt("ip_ad4" + camNum, prefs.getIp_ad4()));
        prefs.setIp_port(extras.getInt("ip_port" + camNum, prefs.getIp_port()));
    }
    private void putExtras(Intent intent, StreamPreferences prefs, int camNum){
        intent.putExtra("width"+camNum, prefs.getWidth());
        intent.putExtra("height"+camNum, prefs.getHeight());
        intent.putExtra("ip_ad1"+camNum, prefs.getIp_ad1());
        intent.putExtra("ip_ad2"+camNum, prefs.getIp_ad2());
        intent.putExtra("ip_ad3"+camNum, prefs.getIp_ad3());
        intent.putExtra("ip_ad4"+camNum, prefs.getIp_ad4());
        intent.putExtra("ip_port"+camNum, prefs.getIp_port());
    }
    public void openSettingsLeft(View v){
        Intent intent = new Intent(this, CamSettingsActivity.class);
        Intent settings_intent = new Intent(GeneralSettingsActivity.this, CamSettingsActivity.class);
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
    public void openSettingsRight(View v){
        Intent intent = new Intent(this, CamSettingsActivity.class);
        Intent settings_intent = new Intent(GeneralSettingsActivity.this, CamSettingsActivity.class);
        settings_intent.putExtra("width", streamPrefRight.getWidth());
        settings_intent.putExtra("height", streamPrefRight.getHeight());
        settings_intent.putExtra("ip_ad1", streamPrefRight.getIp_ad1());
        settings_intent.putExtra("ip_ad2", streamPrefRight.getIp_ad2());
        settings_intent.putExtra("ip_ad3", streamPrefRight.getIp_ad3());
        settings_intent.putExtra("ip_ad4", streamPrefRight.getIp_ad4());
        settings_intent.putExtra("ip_port", streamPrefRight.getIp_port());
        Log.d("MJPEG_Cam" + 2, "sent to settings : ip_port=" + streamPrefRight.getIp_port());
        settings_intent.putExtra("cam_number", 2);
        startActivityForResult(settings_intent, REQUEST_SETTINGS);

    }
}
