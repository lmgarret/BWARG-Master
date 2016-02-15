package com.bwarg.master;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.text.Layout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class GeneralSettingsActivity extends ActionBarActivity {
    StreamPreferences streamPrefLeft = new StreamPreferences();
    StreamPreferences streamPrefRight = new StreamPreferences();
    private static final int REQUEST_SETTINGS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getResources().getString(R.string.title_settings_general));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        CheckBox checkBoxFPS = (CheckBox) findViewById(R.id.checkBox_fps);
        checkBoxFPS.setChecked(MjpegActivity.SHOW_FPS);

        CheckBox checkBoxStatus = (CheckBox) findViewById(R.id.checkBox_log);
        checkBoxStatus.setChecked(MjpegActivity.SHOW_CAMERA_STATUS);
    }
    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.checkBox_fps:
                MjpegActivity.SHOW_FPS=checked;
                break;
            case R.id.checkBox_log:
                MjpegActivity.SHOW_CAMERA_STATUS = checked;
                break;
            default:
                break;
        }
    }
    private void loadExtras(Bundle extras, StreamPreferences prefs, int camNum) {
        prefs.setWidth(extras.getInt("width" + camNum, prefs.getWidth()));
        prefs.setHeight(extras.getInt("height" + camNum, prefs.getHeight()));

        prefs.setIp_ad1(extras.getInt("ip_ad1" + camNum, prefs.getIp_ad1()));
        prefs.setIp_ad2(extras.getInt("ip_ad2" + camNum, prefs.getIp_ad2()));
        prefs.setIp_ad3(extras.getInt("ip_ad3" + camNum, prefs.getIp_ad3()));
        prefs.setIp_ad4(extras.getInt("ip_ad4" + camNum, prefs.getIp_ad4()));
        prefs.setIp_port(extras.getInt("ip_port" + camNum, prefs.getIp_port()));
        prefs.setName(extras.getString("device_name" + camNum));
        prefs.setCommand(extras.getString("ip_command" + camNum));
    }
    private void putExtras(Intent intent, StreamPreferences prefs, int camNum){
        intent.putExtra("width" + camNum, prefs.getWidth());
        intent.putExtra("height" + camNum, prefs.getHeight());
        intent.putExtra("ip_ad1" + camNum, prefs.getIp_ad1());
        intent.putExtra("ip_ad2" + camNum, prefs.getIp_ad2());
        intent.putExtra("ip_ad3" + camNum, prefs.getIp_ad3());
        intent.putExtra("ip_ad4" + camNum, prefs.getIp_ad4());
        intent.putExtra("ip_port" + camNum, prefs.getIp_port());
        intent.putExtra("device_name" + camNum, prefs.getName());
        intent.putExtra("ip_command"+camNum, prefs.getCommand());
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
        settings_intent.putExtra("device_name", streamPrefLeft.getName());
        settings_intent.putExtra("cam_number", 1);
        settings_intent.putExtra("ip_command", streamPrefLeft.getCommand());
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
        settings_intent.putExtra("device_name", streamPrefRight.getName());
        settings_intent.putExtra("ip_command", streamPrefRight.getCommand());

        Log.d("MJPEG_Cam" + 2, "sent to settings : ip_command=" + streamPrefRight.getCommand());
        settings_intent.putExtra("cam_number", 2);
        startActivityForResult(settings_intent, REQUEST_SETTINGS);

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
                        streamPrefLeft.setName(data.getStringExtra("device_name"));
                        streamPrefLeft.setCommand(data.getStringExtra("ip_command"));
                    }else if (cam_number == 2){ //right_cam
                        streamPrefRight.setWidth(data.getIntExtra("width", streamPrefRight.getWidth()));
                        streamPrefRight.setHeight(data.getIntExtra("height", streamPrefRight.getHeight()));
                        streamPrefRight.setIp_ad1(data.getIntExtra("ip_ad1", streamPrefRight.getIp_ad1()));
                        streamPrefRight.setIp_ad2(data.getIntExtra("ip_ad2", streamPrefRight.getIp_ad2()));
                        streamPrefRight.setIp_ad3(data.getIntExtra("ip_ad3", streamPrefRight.getIp_ad3()));
                        streamPrefRight.setIp_ad4(data.getIntExtra("ip_ad4", streamPrefRight.getIp_ad4()));
                        streamPrefRight.setIp_port(data.getIntExtra("ip_port", streamPrefRight.getIp_port()));
                        streamPrefRight.setName(data.getStringExtra("device_name"));
                        streamPrefRight.setCommand(data.getStringExtra("ip_command"));

                    }
                }
                break;
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
