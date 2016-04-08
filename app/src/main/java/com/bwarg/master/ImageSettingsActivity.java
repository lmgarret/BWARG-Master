package com.bwarg.master;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LM on 03.03.2016.
 */
public class ImageSettingsActivity extends ActionBarActivity {
    private static final String TAG = "SETTINGS";

    private Spinner resolution_spinner;
    private SeekBar quality_seekbar;
    private TextView quality_text;
    private CheckBox auto_white_lock_checkBox;
    private Spinner white_balance_spinner;
    private CheckBox auto_exposure_lock_checkBoc;
    private Spinner iso_spinner;
    private Spinner focus_mode_spinner;
    private CheckBox stabilize_image_checkBox;
    private CheckBox fast_fps_mode_checkBox;

    private SlaveStreamPreferences streamPrefs = new SlaveStreamPreferences();

    Button settings_done;


    private static final int REQUEST_SETTINGS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_settings);

        Bundle extras = getIntent().getExtras();
        Gson gson = new Gson();
        streamPrefs = gson.fromJson(extras.getString("stream_prefs"), SlaveStreamPreferences.class);

        resolution_spinner = (Spinner) findViewById(R.id.resolution_spinner);

        quality_seekbar = (SeekBar) findViewById(R.id.quality_seekbar);
        quality_text = (TextView) findViewById(R.id.quality_text);

        auto_white_lock_checkBox = (CheckBox) findViewById(R.id.auto_white_lock);
        white_balance_spinner = (Spinner) findViewById(R.id.white_balance_spinner);
        auto_exposure_lock_checkBoc = (CheckBox) findViewById(R.id.auto_exposure_lock);
        iso_spinner = (Spinner) findViewById(R.id.iso_spinner);
        focus_mode_spinner = (Spinner) findViewById(R.id.focus_spinner);
        stabilize_image_checkBox = (CheckBox) findViewById(R.id.image_stabilization);
        fast_fps_mode_checkBox = (CheckBox) findViewById(R.id.fast_fps_checkbox);

        fillUI(streamPrefs);

        resolution_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View viw, int arg2, long arg3) {
                Spinner spinner = (Spinner) parent;
                if (parent.equals(spinner))
                    streamPrefs.setSizeIndex(spinner.getSelectedItemPosition());
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        quality_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                quality_text.setText(String.valueOf(progress + 1) + "%");
                streamPrefs.setQuality(progress + 1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        auto_white_lock_checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (auto_white_lock_checkBox.equals((CheckBox) buttonView))
                    streamPrefs.setAutoWhiteBalanceLock(isChecked);
            }
        });
        white_balance_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View viw, int arg2, long arg3) {
                Spinner spinner = (Spinner) parent;
                if (white_balance_spinner.equals(spinner))
                    streamPrefs.setWhiteBalance((String) spinner.getSelectedItem());
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        auto_exposure_lock_checkBoc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (auto_exposure_lock_checkBoc.equals((CheckBox) buttonView))
                    streamPrefs.setAutoExposureLock(isChecked);
            }
        });
        iso_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View viw, int arg2, long arg3) {
                Spinner spinner = (Spinner) parent;
                if (iso_spinner.equals(spinner))
                    streamPrefs.setIso((String) spinner.getSelectedItem());
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        focus_mode_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View viw, int arg2, long arg3) {
                Spinner spinner = (Spinner) parent;
                if (focus_mode_spinner.equals(spinner))
                    streamPrefs.setFocusMode((String) spinner.getSelectedItem());
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        stabilize_image_checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (stabilize_image_checkBox.equals((CheckBox) buttonView))
                    streamPrefs.setImageStabilization(isChecked);
            }
        });
        fast_fps_mode_checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (fast_fps_mode_checkBox.equals((CheckBox) buttonView)) {
                    int val = 0;
                    if (isChecked)
                        val = 1;
                    streamPrefs.setFastFpsMode(val);
                }
            }
        });
        settings_done = (Button) findViewById(R.id.settings_done);
        settings_done.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        Gson gson = new Gson();
                        String stringPref = gson.toJson(streamPrefs);
                        intent.putExtra("stream_prefs", stringPref);

                        Log.d(TAG, stringPref);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
        );
    }
    private void fillUI(SlaveStreamPreferences prefs){
        /*final Camera camera = Camera.open(prefs.getCamIndex());
        final Camera.Parameters params = camera.getParameters();
        camera.release();*/

        quality_seekbar.setProgress(prefs.getQuality() - 1);
        quality_text.setText(String.valueOf(prefs.getQuality()) + "%");

        auto_white_lock_checkBox.setEnabled(prefs.isAutoWhiteBalanceLockSupported());
        if(auto_white_lock_checkBox.isEnabled()) {
            auto_white_lock_checkBox.setChecked(prefs.getAutoWhiteBalanceLock());
        }else{
            prefs.setAutoWhiteBalanceLock(false);
        }
        auto_exposure_lock_checkBoc.setEnabled(prefs.isAutoExposureLockSupported());
       /* if((Build.MANUFACTURER+Build.MODEL).equals("samsungGT-I9300")){
            auto_exposure_lock_checkBoc.setEnabled(false);
            Log.i(TAG, "Desactivated auto_exposure_lock on Galaxy S3, use main screen button or network command instead.");
        }*/
        if(auto_exposure_lock_checkBoc.isEnabled()) {
            auto_exposure_lock_checkBoc.setChecked(prefs.getAutoExposureLock());
        }else{
            prefs.setAutoExposureLock(false);
        }
        stabilize_image_checkBox.setEnabled(prefs.isImageStabilizationSupported());
        if(stabilize_image_checkBox.isEnabled()) {
            stabilize_image_checkBox.setChecked(prefs.getImageStabilization());
        }else{
            prefs.setImageStabilization(false);
        }
        fast_fps_mode_checkBox.setEnabled(prefs.isFastFpsModeSupported());
        if(fast_fps_mode_checkBox.isEnabled()) {
            fast_fps_mode_checkBox.setChecked(streamPrefs.getFastFpsMode() == 1);
        }else{
            prefs.setFastFpsMode(0);
        }
        initSpinners(prefs);
    }
    private void initSpinners(SlaveStreamPreferences prefs){
        //PREVIEW SIZES
        ArrayAdapter<String> sizeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, prefs.getResolutionsSupported());
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        resolution_spinner.setAdapter(sizeAdapter);
        resolution_spinner.setSelection(prefs.getSizeIndex());

        //WHITE BALANCES MODE
        /*final List<String> supportedWhiteModes = params.getSupportedWhiteBalance();
        ArrayAdapter<String> whiteAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, prefs.getW);
        whiteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        white_balance_spinner.setAdapter(whiteAdapter);
        white_balance_spinner.setSelection(supportedWhiteModes.indexOf(prefs.getWhiteBalance()));*/
        //TODO add white balances list as param of slavestreamprefs
        white_balance_spinner.setEnabled(false);

        //ISO MODE
        /*String isoModesString = params.get("iso-values");
        if(isoModesString == null){
            //Looks like Acer did implemented it as iso-speed and not iso
            isoModesString = params.get("iso-speed-values");
        }
        if(isoModesString == null){
            iso_spinner.setEnabled(false);
        }else{
            iso_spinner.setEnabled(true);
            final List<String> supportedISOModes = new ArrayList<>();
            String temp = "";
            for(int i=0; i< isoModesString.length(); i++){
                char c = isoModesString.charAt(i);
                if(c != ','){
                    temp+=c;
                }else{
                    supportedISOModes.add(temp);
                    temp="";
                }
            }
            if(!temp.equals("")){
                supportedISOModes.add(temp);
            }
            ArrayAdapter<String> isoAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, supportedISOModes);
            isoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            iso_spinner.setAdapter(isoAdapter);
            iso_spinner.setSelection(supportedISOModes.indexOf(prefs.getIso()));
        }*/
        //TODO add iso list as param of slavestreamprefs
        iso_spinner.setEnabled(false);


        //FOCUS MODE
        /*final List<String> supportedFocusModes = params.getSupportedFocusModes();
        ArrayAdapter<String> focusAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, supportedFocusModes);
        focusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        focus_mode_spinner.setAdapter(focusAdapter);
        focus_mode_spinner.setSelection(supportedFocusModes.indexOf(prefs.getFocusMode()));*/
        //TODO add focus mode list as param of slavestreamprefs
        focus_mode_spinner.setEnabled(false);


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                fillUI(streamPrefs);
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
