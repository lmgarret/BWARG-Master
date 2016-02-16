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

import com.google.gson.Gson;

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
            streamPrefLeft= loadExtras(extras, 1);
            streamPrefRight = loadExtras(extras, 2);
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
    private StreamPreferences loadExtras(Bundle extras, int camNum) {
        Gson gson = new Gson();
        return gson.fromJson(extras.getString("stream_prefs"+camNum), StreamPreferences.class);
    }
    private void putExtras(Intent intent, StreamPreferences prefs, int camNum){
        Gson gson = new Gson();
        String gsonStreamPrefs = gson.toJson(prefs);
        intent.putExtra("stream_prefs"+camNum,gsonStreamPrefs);
    }
    public void openSettingsLeft(View v){
        Intent intent = new Intent(this, CamSettingsActivity.class);
        Intent settings_intent = new Intent(GeneralSettingsActivity.this, CamSettingsActivity.class);

        Gson gson = new Gson();
        String gsonStreamPrefs = gson.toJson(streamPrefLeft);
        settings_intent.putExtra("stream_prefs", gsonStreamPrefs);
        settings_intent.putExtra("cam_number", 1);

        Log.d("MJPEG_Cam" + 1, "sent to settings : ip_command=" + streamPrefLeft.getCommand());

        startActivityForResult(settings_intent, REQUEST_SETTINGS);

    }
    public void openSettingsRight(View v){
        Intent intent = new Intent(this, CamSettingsActivity.class);
        Intent settings_intent = new Intent(GeneralSettingsActivity.this, CamSettingsActivity.class);

        Gson gson = new Gson();
        String gsonStreamPrefs = gson.toJson(streamPrefRight);
        settings_intent.putExtra("stream_prefs", gsonStreamPrefs);
        settings_intent.putExtra("cam_number", 2);

        Log.d("MJPEG_Cam" + 2, "sent to settings : ip_command=" + streamPrefRight.getCommand());

        startActivityForResult(settings_intent, REQUEST_SETTINGS);

    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SETTINGS:
                if (resultCode == Activity.RESULT_OK) {
                    int cam_number = data.getIntExtra("cam_number", 1);
                    if(cam_number == 1){ //left cam
                        Gson gson = new Gson();
                        streamPrefLeft = gson.fromJson(data.getStringExtra("stream_prefs"), StreamPreferences.class);
                    }else if (cam_number == 2){ //right_cam
                        Gson gson = new Gson();
                        streamPrefRight = gson.fromJson(data.getStringExtra("stream_prefs"), StreamPreferences.class);;
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
