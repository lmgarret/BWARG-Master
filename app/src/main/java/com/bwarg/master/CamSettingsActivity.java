package com.bwarg.master;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView.BufferType;

import com.google.gson.Gson;

public class CamSettingsActivity extends ActionBarActivity {

    Button settings_done;

    Spinner resolution_spinner;
    EditText width_input;
    EditText height_input;

    EditText address1_input;
    EditText address2_input;
    EditText address3_input;
    EditText address4_input;
    EditText port_input;
    EditText command_input;

    Button address1_increment;
    Button address2_increment;
    Button address3_increment;
    Button address4_increment;

    Button address1_decrement;
    Button address2_decrement;
    Button address3_decrement;
    Button address4_decrement;

    RadioGroup port_group;
    RadioGroup command_group;

    StreamPreferences streamPrefs = new StreamPreferences();
    StreamPreferences origStreamPrefs = new StreamPreferences(); //original stream prefs received when creating activity
    int cam_number = 1;

    public final static int REQUEST_SETTINGS_UDP = 1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getResources().getString(R.string.title_settings_general));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.cam_settings);

        Bundle extras = getIntent().getExtras();

        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(this, R.array.resolution_array,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        resolution_spinner = (Spinner) findViewById(R.id.resolution_spinner);
        resolution_spinner.setAdapter(adapter);

        width_input = (EditText) findViewById(R.id.width_input);
        height_input = (EditText) findViewById(R.id.height_input);

        address1_input = (EditText) findViewById(R.id.address1_input);
        address2_input = (EditText) findViewById(R.id.address2_input);
        address3_input = (EditText) findViewById(R.id.address3_input);
        address4_input = (EditText) findViewById(R.id.address4_input);
        port_input = (EditText) findViewById(R.id.port_input);

        command_input = (EditText) findViewById(R.id.command_input);

        port_group = (RadioGroup) findViewById(R.id.port_radiogroup);
        command_group = (RadioGroup) findViewById(R.id.command_radiogroup);

        if (extras != null) {
            Gson gson = new Gson();
            streamPrefs = gson.fromJson(extras.getString("stream_prefs"), StreamPreferences.class);
            origStreamPrefs = streamPrefs;

            cam_number = extras.getInt("cam_number", cam_number);

            Log.d("MJPEG_Cam" + cam_number, " received URL " + streamPrefs.getURL() + streamPrefs.getCommand() + " in CamSettingsActivity.");
            resolution_spinner.setSelection(adapter.getCount() - 1);

            fillUI(streamPrefs);

            String title = "Stream";
            if(cam_number == 1){
                title = getResources().getString(R.string.title_settings_left);
            }else if (cam_number == 2){
                title = getResources().getString(R.string.title_settings_right);
            }
            if(!streamPrefs.getName().equals(StreamPreferences.UNKNOWN_NAME)){
                title+=": "+streamPrefs.getName();
            }
            setTitle(title);
        }else{
            fillUI(streamPrefs);
            Log.d("MJPEG_Cam"+cam_number, " null args received");
        }

        resolution_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View viw, int arg2, long arg3) {
                Spinner spinner = (Spinner) parent;
                String item = (String) spinner.getSelectedItem();
                if(!item.equals("Custom")){
                    int xIndex = item.indexOf('x');
                    streamPrefs.setWidth(Integer.parseInt(item.substring(0, xIndex)));
                    streamPrefs.setHeight(Integer.parseInt(item.substring(xIndex+1, item.length())));
                    width_input.setText(String.valueOf(streamPrefs.getWidth()));
                    height_input.setText(String.valueOf(streamPrefs.getHeight()));
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        address1_increment = (Button) findViewById(R.id.address1_increment);
        address1_increment.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        addToAddress(address1_input, 1, 1);
                    }
                }
        );
        address2_increment = (Button) findViewById(R.id.address2_increment);
        address2_increment.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        addToAddress(address2_input, 2, 1);
                    }
                }
        );
        address3_increment = (Button) findViewById(R.id.address3_increment);
        address3_increment.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        addToAddress(address3_input,3,1);
                    }
                }
        );
        address4_increment = (Button) findViewById(R.id.address4_increment);
        address4_increment.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        addToAddress(address4_input, 4, 1);

                    }
                }
        );

        address1_decrement = (Button) findViewById(R.id.address1_decrement);
        address1_decrement.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        addToAddress(address1_input, 1, -1);
                    }
                }
        );

        address2_decrement = (Button) findViewById(R.id.address2_decrement);
        address2_decrement.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        addToAddress(address2_input,2,-1);
                    }
                }
        );
        address3_decrement = (Button) findViewById(R.id.address3_decrement);
        address3_decrement.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        addToAddress(address3_input, 3, -1);
                    }
                }
        );
        address4_decrement = (Button) findViewById(R.id.address4_decrement);
        address4_decrement.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        addToAddress(address4_input, 4, -1);
                    }
                }
        );

        port_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.port_8080) {
                    port_input.setText(getString(R.string.port_8080));
                }
            }
        });

        settings_done = (Button) findViewById(R.id.settings_done);
        settings_done.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {

                        String s;

                        s = width_input.getText().toString();
                        if (!"".equals(s)) {
                            streamPrefs.setWidth(Integer.parseInt(s));
                        }
                        s = height_input.getText().toString();
                        if (!"".equals(s)) {
                            streamPrefs.setHeight(Integer.parseInt(s));
                        }
                        s = address1_input.getText().toString();
                        if (!"".equals(s)) {
                            streamPrefs.setIp_ad1(Integer.parseInt(s));
                        }
                        s = address2_input.getText().toString();
                        if (!"".equals(s)) {
                            streamPrefs.setIp_ad2(Integer.parseInt(s));
                        }
                        s = address3_input.getText().toString();
                        if (!"".equals(s)) {
                            streamPrefs.setIp_ad3(Integer.parseInt(s));
                        }
                        s = address4_input.getText().toString();
                        if (!"".equals(s)) {
                            streamPrefs.setIp_ad4(Integer.parseInt(s));
                        }

                        s = port_input.getText().toString();
                        if (!"".equals(s)) {
                            streamPrefs.setIp_port(Integer.parseInt(s));
                        }
                        streamPrefs.setCommand(command_input.getText().toString());

                        Intent intent = new Intent();
                        Gson gson = new Gson();
                        String stringPref = gson.toJson(streamPrefs);
                        intent.putExtra("stream_prefs", stringPref);
                        intent.putExtra("cam_number",cam_number);
                        Log.d("MJPEG_CAM" + cam_number, "Sending URL " + streamPrefs.getURL() + streamPrefs.getCommand() +" back to GeneralSettingsActivity");
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
        );
        command_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.command_streaming){
                    command_input.setText(getString(R.string.command_streaming));
                }else if(checkedId == R.id.command_videofeed){
                    command_input.setText(getString(R.string.command_videofeed));
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SETTINGS_UDP:
                if (resultCode == Activity.RESULT_OK) {
                    Gson gson = new Gson();
                    streamPrefs = gson.fromJson(data.getStringExtra("stream_prefs"), StreamPreferences.class);
                    fillUI(streamPrefs);

                }else if(resultCode == Activity.RESULT_CANCELED){
                    fillUI(origStreamPrefs);
                }
                break;
        }
    }
    public void openNetworkDiscovery(View v){
        Intent intent = new Intent(this, DiscoverNetworkActivity.class);
        startActivityForResult(intent, REQUEST_SETTINGS_UDP);
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
    //Fills all buttons and texts with the given stream preferences
    private void fillUI(StreamPreferences streamPrefs){
        width_input.setText(String.valueOf(streamPrefs.getWidth()), BufferType.NORMAL);
        height_input.setText(String.valueOf(streamPrefs.getHeight()), BufferType.NORMAL);

        address1_input.setText(String.valueOf(streamPrefs.getIp_ad1()), BufferType.NORMAL);
        address2_input.setText(String.valueOf(streamPrefs.getIp_ad2()), BufferType.NORMAL);
        address3_input.setText(String.valueOf(streamPrefs.getIp_ad3()), BufferType.NORMAL);
        address4_input.setText(String.valueOf(streamPrefs.getIp_ad4()), BufferType.NORMAL);
        port_input.setText(String.valueOf(streamPrefs.getIp_port()), BufferType.NORMAL);

        command_input.setText(streamPrefs.getCommand(), BufferType.NORMAL);
    }
    private void addToAddress(EditText address_input, int address_num, int toAdd){
        String s = address_input.getText().toString();
        int val = 0;
        if(address_num ==1)
            val = streamPrefs.getIp_ad1();
        if(address_num==2)
            val = streamPrefs.getIp_ad2();
        if(address_num==3)
            val = streamPrefs.getIp_ad3();
        if(address_num==4)
            val = streamPrefs.getIp_ad4();

        if (!"".equals(s)) {
            val = Integer.parseInt(s);
        }
        if (val >= 0 && val <= 255) {
            val += toAdd;
        }
        if (val < 0) {
            val = 255;
        } else if (val > 255) {
            val = 0;
        }

        if(address_num ==1)
            streamPrefs.setIp_ad1(val);
        if(address_num==2)
            streamPrefs.setIp_ad2(val);
        if(address_num==3)
            streamPrefs.setIp_ad3(val);
        if(address_num==4)
            streamPrefs.setIp_ad4(val);

        address_input.setText(String.valueOf(val), BufferType.NORMAL);
    }
}
